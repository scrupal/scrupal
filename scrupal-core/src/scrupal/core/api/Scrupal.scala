/**********************************************************************************************************************
 * Copyright © 2014 Reactific Software LLC                                                                            *
 *                                                                                                                    *
 * This file is part of Scrupal, an Opinionated Web Application Framework.                                            *
 *                                                                                                                    *
 * Scrupal is free software: you can redistribute it and/or modify it under the terms                                 *
 * of the GNU General Public License as published by the Free Software Foundation,                                    *
 * either version 3 of the License, or (at your option) any later version.                                            *
 *                                                                                                                    *
 * Scrupal is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;                               *
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                          *
 * See the GNU General Public License for more details.                                                               *
 *                                                                                                                    *
 * You should have received a copy of the GNU General Public License along with Scrupal.                              *
 * If not, see either: http://www.gnu.org/licenses or http://opensource.org/licenses/GPL-3.0.                         *
 **********************************************************************************************************************/

package scrupal.core.api

import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

import akka.actor.{ActorRef, ActorSystem}
import akka.pattern.ask
import akka.util.Timeout

import com.typesafe.config.{ConfigRenderOptions, ConfigValue}
import scrupal.core.CoreModule
import scrupal.core.apps.AdminApp
import scrupal.core.sites.WelcomeSite

import scrupal.db.DBContext
import scrupal.utils._
import spray.routing.RequestContext

import scala.collection.immutable.TreeMap
import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.matching.Regex

class Scrupal(
  name: String = "Scrupal",
  config: Option[Configuration] = None,
  ec: Option[ExecutionContext] = None,
  disp: Option[ActorRef] = None,
  dbc: Option[DBContext] = None,
  actSys: Option[ActorSystem] = None
)
extends ScrupalComponent with AutoCloseable with Enablement[Scrupal] with Registrable[Scrupal]
{

  LoggingHelpers.initializeLogging(forDebug = true)

  val key = ""
  def registry = Scrupal

  val Copyright = "© 2013-2015 Reactific Software LLC. All Rights Reserved."

  def id : Symbol = Symbol(name)

  val _configuration  = config.getOrElse(Configuration.default)

  implicit val _actorSystem = actSys.getOrElse(ActorSystem("Scrupal", _configuration.underlying))

  implicit val _executionContext = ec.getOrElse(getExecutionContext(_configuration))

  implicit val _timeout = Timeout(
    _configuration.getMilliseconds("scrupal.response.timeout").getOrElse(8000L), TimeUnit.MILLISECONDS
  )

  private[this] def getExecutionContext(config: Configuration) : ExecutionContext = {
    // FIXME: This should be obtained from configuration instead
    scala.concurrent.ExecutionContext.Implicits.global
  }

  lazy val _dispatcher = disp.getOrElse(ActionProcessor.makeSingletonRef(_actorSystem))


  val _dbContext = new AtomicReference[DBContext](dbc.getOrElse(null))

  val assetsLocator = new ConfiguredAssetsLocator(_configuration)

  def withConfiguration[T](f: (Configuration) ⇒ T) : T = {
    f(_configuration)
  }

  def withDBContext[T](f: (DBContext) ⇒ T) : T = {
    val dbc = _dbContext.get()
    require(dbc != null)
    f(dbc)
  }

  def withSchema[T](f: (DBContext, Schema) => T) : T = {
    withDBContext { dbc =>
      f(dbc, new Schema(dbc, name))
    }
  }

  def withExecutionContext[T](f: (ExecutionContext) => T) : T = {
    f(_executionContext)
  }

  def withActorSystem[T](f : (ActorSystem) ⇒ T) : T = {
    f(_actorSystem)
  }

  def withActorExec[T](f: (ActorSystem, ExecutionContext, Timeout) ⇒ T) : T = {
    f(_actorSystem, _executionContext, _timeout)
  }

  /** Simple utility to determine if we are considered "ready" or not. Basically, if we have a non empty Site
    * Registry then we have had to found a database and loaded the sites. So that is our indicator of whether we
    * are configured yet or not.
    * @return True iff there are sites loaded
    */
  def isReady : Boolean = _configuration.getConfig("scrupal").nonEmpty && Site.nonEmpty

  def isChildScope(e: Enablement[_]) : Boolean = e match {
    case s: Site ⇒  Site.containsValue(s)
    case _ ⇒ false
  }

  def authenticate(rc: RequestContext) : Option[Principal] = None

  /**
	 * Called before the application starts.
	 *
	 * Resources managed by plugins, such as database connections, are likely not available at this point.
	 *
	 */
	def open() = {
    // We do a lot of stuff in API objects and they need to be instantiated in the right order,
    // so "touch" them now because they are otherwise initialized randomly as used
    require(Type.registryName == "Types")
    require(Module.registryName == "Modules")
    require(Site.registryName == "Sites")
    require(Entity.registryName == "Entities")
    require(Template.registryName == "Templates")

    val config = onLoadConfig(Configuration.default)

    // Get the database started up
    DBContext.startup()

    val dbc = DBContext.fromConfiguration(Symbol(name+"-DB"), Some(config))
    _dbContext.set(dbc)

    // TODO: scan classpath for additional modules
    val configured_modules = Seq.empty[String]

    // Now we go through the configured modules and bootstrap them
    for (class_name ← configured_modules) {
      Scrupal.findModuleOnClasspath(class_name) match {
        case Some(module) ⇒ module.bootstrap(config)
        case None ⇒ log.warn("Could not locate module with class name: " + class_name)
      }
    }

    // Load the configuration and wait at most 10 seconds for it
    val load_result = Await.result(load(config, dbc), 10.seconds)
    /* TODO: Implement this so it doesn't thwart startup and test failures
    if (load_result.isEmpty)
      toss("Refusing to start because of load errors. Check logs for details.")
    else {
      log.info("Loaded Sites:\n" + load_result.map { case(x,y) ⇒ s"$x:${y.host}"})
    }*/


    config -> dbc
  }

  def close() = {
    DBContext.shutdown()
    _actorSystem.shutdown()
  }

  override def finalize() = {
    close()
  }

  type FlatConfig =  TreeMap[String,ConfigValue]

  def interestingConfig(config: Configuration) : FlatConfig = {
    val elide : Regex = "^(akka|java|sun|user|awt|os|path|line).*".r
    val entries = config.entrySet.toSeq
    val filtered = entries filter { case (x,y) =>  !elide.findPrefixOf(x).isDefined }
    TreeMap[String,ConfigValue](filtered.toSeq:_*)
  }

  /** Load the Sites from configuration
    * Site loading is based on the database configuration. Whatever databases are loaded, they are scanned and any
    * sites in them are fetched and instantiated into the memory registry. Note that multiple sites may utilize the
    * same database information. We utilize this to open the database and load the site objects they contain
    * @param config The Scrupal Configuration to use to determine the initial loading
    * @param context The database context from which to load the
    */
  protected def load(config: Configuration, context: DBContext) : Future[Map[String, Site]] = {
    withSchema { (dbc, schema) =>
      val result = schema.validateSchema(_executionContext).map {
        strings: Seq[String] ⇒ {
          for (s <- schema.sites.fetchAllSync(5.seconds)) yield {
            log.debug(s"Loading site '${s._id.name}' for host ${s.host}, enabled=${s.isEnabled(this)}")
            s.enable(this)
            s.host → s
          }
        }.toMap
      }.recover {
        case x: Throwable ⇒
          log.warn(s"Attempt to validate API Schema failed.", x)
          Map.empty[String, Site]
      }
      result.map { sites ⇒
        if (sites.isEmpty) {
          val ws = new WelcomeSite(Symbol(name + "-Welcome"))
          ws.enable(this)
          DataCache.update(this, schema)
          AdminApp.enable(ws)
          CoreModule.enable(AdminApp)
          Map(ws.host → ws )
        } else {
          DataCache.update(this, schema)
          sites
        }
      }
    }
  }

  /**
   * Called once the application is started.
   */
  def onStart() {
  }


  /** Handle An Action
    * This is the main entry point into Scrupal for processing actions. It very simply forwards the action to
    * the dispatcher for processing and (quickly) returns a future that will be completed when the dispatcher gets
    * around to it. The point of this is to hide the use of actors within Scrupal and have a nice, simple, quickly
    * responding synchronous call in order to obtain the Future to the eventual result of the action.
    * @param action The action to act upon (a Request => Result[P] function).
    * @return A Future to the eventual Result[P]
    */
  def dispatch(action: Action) : Future[Result[_]] = {
    _dispatcher.ask(action)(_timeout) flatMap { any ⇒ any.asInstanceOf[Future[Result[_]]] }
  }

  /**
   * Called on application stop.
   */
  def onStop() {
  }


  /** Provide handling of configuration loading
    *
    * This method can be overridden by subclasses to refine the configuration read from default sources or do anything
    * else that might be interesting. This default version just prints the configuration to the log at TRACE level.
	  *
	  * @param config the loaded configuration
	  * @return The configuration that Scrupal should use
    */
	def onLoadConfig(config: Configuration): Configuration = {
    // Trace log the configuration
    log.trace("STARTUP CONFIGURATION VALUES")
    interestingConfig(config) foreach { case (key: String, value: ConfigValue) =>
      log.trace ( "    " + key + " = " + value.render(ConfigRenderOptions.defaults))
    }

    // return the configuration
    config
 	}

	/**
	 * Called Just before the action is used.
	 *
	 */
	def doFilter(a: Action): Action = {
		a
	}


  /**
	 * Called when an exception occurred.
	 *
	 * The default is to send the default error page.
	 *
	 * @param ex The exception
	 * @return The result to send to the client
	 */
	def onError(action: Action, ex: Throwable) = {

	/*
		try {
			InternalServerError(Play.maybeApplication.map {
				case app if app.mode != Mode.Prod => views.html.defaultpages.devError.f
				case app => views.html.defaultpages.error.f
			}.getOrElse(views.html.defaultpages.devError.f) {
				ex match {
					case e: UsefulException => e
					case NonFatal(e) => UnexpectedException(unexpected = Some(e))
				}
			})
		} catch {
			case e: Throwable => {
				Logger.error("Error while rendering default error page", e)
				InternalServerError
			}
		}
		*/
	}

	/**
	 * Called when no action was found to serve a request.
	 *
	 * The default is to send the framework default 404 page.
	 *
	 * @param request the HTTP request header
	 * @return the result to send to the client
	 */
  def onHandlerNotFound(request: Action)  = {
		/*
		NotFound(Play.maybeApplication.map {
			case app if app.mode != Mode.Prod => views.html.defaultpages.devNotFound.f
			case app => views.html.defaultpages.notFound.f
		}.getOrElse(views.html.defaultpages.devNotFound.f)(request, Play.maybeApplication.flatMap(_.routes)))
		*/
	}

	/**
	 * Called when an action has been found, but the request parsing has failed.
	 *
	 * The default is to send the framework default 400 page.
	 *
	 * @param request the HTTP request header
	 * @return the result to send to the client
	 */
	def onBadRequest(request: Action, error: String)  = {
		/*
		BadRequest(views.html.defaultpages.badRequest(request, error))
		*/
	}

	def onActionCompletion(request: Action) = {
	}
}

object Scrupal extends Registry[Scrupal] {
  def registryName = "Scrupalz"
  def registrantsName = "scrupali"

  private[scrupal] def findModuleOnClasspath(name: String) : Option[Module] = {
    None // TODO: Write ClassLoader code to load foreign modules on the classpath - maybe use OSGi ?
  }

}
