/**********************************************************************************************************************
 * This file is part of Scrupal, a Scalable Reactive Web Application Framework for Content Management                 *
 *                                                                                                                    *
 * Copyright (c) 2015, Reactific Software LLC. All Rights Reserved.                                                   *
 *                                                                                                                    *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance     *
 * with the License. You may obtain a copy of the License at                                                          *
 *                                                                                                                    *
 *     http://www.apache.org/licenses/LICENSE-2.0                                                                     *
 *                                                                                                                    *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed   *
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for  *
 * the specific language governing permissions and limitations under the License.                                     *
 **********************************************************************************************************************/

package scrupal.core.impl


import akka.actor.ActorSystem
import akka.http.scaladsl.server.RequestContext
import com.typesafe.config.{ ConfigRenderOptions, ConfigValue }
import play.api.Configuration
import scrupal.api._
import scrupal.core.CoreModule
import scrupal.storage.api.{Schema, Storage, StoreContext}
import scrupal.utils._

import scala.collection.immutable.TreeMap
import scala.concurrent.duration._
import scala.concurrent.{ Await, ExecutionContext, Future }
import scala.util.matching.Regex

case class Scrupal(
  override val name : String = "Scrupal",
  config : Option[Configuration] = None,
  ec : Option[ExecutionContext] = None,
  sc : Option[StoreContext] = None,
  actSys : Option[ActorSystem] = None)
  extends scrupal.api.Scrupal(name, config, ec, sc, actSys) {

  LoggingHelpers.initializeLogging(forDebug = true)


  val assetsLocator = new ConfiguredAssetsLocator(_configuration)

  /** Simple utility to determine if we are considered "ready" or not. Basically, if we have a non empty Site
    * Registry then we have had to found a database and loaded the sites. So that is our indicator of whether we
    * are configured yet or not.
    * @return True iff there are sites loaded
    */
  override def isReady: Boolean = {
    super.isReady
  }

  def authenticate(rc: RequestContext): Option[Principal] = None

  /** Called before the application starts.
    *
    * Resources managed by plugins, such as database connections, are likely not available at this point.
    *
    */
  override def open() : Configuration = {
    // We do a lot of stuff in API objects and they need to be instantiated in the right order,
    // so "touch" them now because they are otherwise initialized randomly as used
    require(Types.registryName == "Types")
    require(Modules.registryName == "Modules")
    require(Sites.registryName == "Sites")
    require(Entities.registryName == "Entities")
    require(Template.registryName == "Templates")

    val config = onLoadConfig(ConfigHelpers.default)

    // Get the database started up
    //Storage.startup()

    val sc = _storageContext


    // TODO: scan classpath for additional modules
    val configured_modules = Seq.empty[String]

    // Now we go through the configured modules and bootstrap them
    for (class_name ← configured_modules) {
      scrupal.api.Scrupal.findModuleOnClasspath(class_name) match {
        case Some(module) ⇒ module.bootstrap(config)
        case None ⇒ log.warn("Could not locate module with class name: " + class_name)
      }
    }

    // Load the configuration and wait at most 10 seconds for it
    val load_result = Await.result(load(config, _storageContext), 10.seconds)

    /* TODO: Implement this so it doesn't thwart startup and test failures
    if (load_result.isEmpty)
      toss("Refusing to start because of load errors. Check logs for details.")
    else {
      log.info("Loaded Sites:\n" + load_result.map { case(x,y) ⇒ s"$x:${y.host}"})
    }*/
    config
    // Await.result(future, 10.seconds)
  }

  def close() = {
    withExecutionContext { implicit ec : ExecutionContext ⇒
      _actorSystem.shutdown()
    }
  }

  override def finalize() = {
    close()
  }

  type FlatConfig = TreeMap[String, ConfigValue]

  def interestingConfig(config: Configuration): FlatConfig = {
    val elide: Regex = "^(akka|java|sun|user|awt|os|path|line).*".r
    val entries = config.entrySet.toSeq
    val filtered = entries filter { case (x, y) ⇒ !elide.findPrefixOf(x).isDefined }
    TreeMap[String, ConfigValue](filtered.toSeq: _*)
  }

  /** Load the Sites from configuration
    * Site loading is based on the database configuration. Whatever databases are loaded, they are scanned and any
    * sites in them are fetched and instantiated into the memory registry. Note that multiple sites may utilize the
    * same database information. We utilize this to open the database and load the site objects they contain
    * @param config The Scrupal Configuration to use to determine the initial loading
    * @param context The database context from which to load the
    */
  protected def load(config: Configuration, context: StoreContext): Future[Map[Regex, Site]] = {
    context.withSchema("core") { schema: Schema ⇒
      try {
        schema.construct
      } catch {
        case x: Throwable ⇒
          log.error("Attempt to validate core schema failed: ", x)
          throw x
      }
      schema.collectionFor[Site]("sites") match {
        case Some(sitesCollection) ⇒ {
          sitesCollection.fetchAll().map {
            sites ⇒ {
              for (site ← sites) yield {
                log.debug(s"Loading site '${site.name}' for host ${site.hostnames}, enabled=${site.isEnabled(this)}")
                site.enable(this)
                site.hostnames → site
              }
            }.toMap
          } recover {
            case x: Throwable ⇒
              log.warn(s"Attempt to validate core Schema failed: ", x)
              Map.empty[Regex, Site]
          }
        } map { sites ⇒
          if (sites.isEmpty) {
            /* FIXME: figure out how to add the WelcomeSite back in
            val ws = new WelcomeSite(Symbol(name + "-Welcome"))
            ws.enable(this)
            DataCache.update(this, schema)
            AdminApp.enable(ws)
            CoreModule.enable(AdminApp)
            Map(ws.hostnames → ws)
            */
            Map.empty[Regex,Site]
          } else {
            DataCache.update(this, schema)
            sites
          }
        }
        case None ⇒
          toss("Collection 'sites' was not found")
      }
    }
  }

  /** Called once the application is started.
    */
  def onStart() {
  }

  /** Handle An Action
    * This is the main entry point into Scrupal for processing actions. It very simply forwards the action to
    * the dispatcher for processing and (quickly) returns a future that will be completed when the dispatcher gets
    * around to it. The point of this is to hide the use of actors within Scrupal and have a nice, simple, quickly
    * responding synchronous call in order to obtain the Future to the eventual result of the action.
    * @param reaction The action to act upon (a Request ⇒ Result[P] function).
    * @return A Future to the eventual Result[P]
    */
  def dispatch(reaction: Reactor): Future[Response] = {
    reaction()
  }

  /** Called on application stop.
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
    interestingConfig(config) foreach {
      case (key: String, value: ConfigValue) ⇒
        log.trace("    " + key + " = " + value.render(ConfigRenderOptions.defaults))
    }

    // return the configuration
    config
  }

  /** Called Just before the action is used.
    *
    */
  def doFilter(a: Reactor): Reactor = {
    a
  }

  /** Called when an exception occurred.
    *
    * The default is to send the default error page.
    *
    * @param ex The exception
    * @return The result to send to the client
    */
  def onError(action: Reactor, ex: Throwable) = {

    /*
  try {
    InternalServerError(Play.maybeApplication.map {
      case app if app.mode != Mode.Prod ⇒ views.html.defaultpages.devError.f
      case app ⇒ views.html.defaultpages.error.f
    }.getOrElse(views.html.defaultpages.devError.f) {
      ex match {
        case e: UsefulException ⇒ e
        case NonFatal(e) ⇒ UnexpectedException(unexpected = Some(e))
      }
    })
  } catch {
    case e: Throwable ⇒ {
      Logger.error("Error while rendering default error page", e)
      InternalServerError
    }
  }
  */
  }

  /** Called when no action was found to serve a request.
    *
    * The default is to send the framework default 404 page.
    *
    * @param request the HTTP request header
    * @return the result to send to the client
    */
  def onHandlerNotFound(request: Reactor) = {
    /*
  NotFound(Play.maybeApplication.map {
    case app if app.mode != Mode.Prod ⇒ views.html.defaultpages.devNotFound.f
    case app ⇒ views.html.defaultpages.notFound.f
  }.getOrElse(views.html.defaultpages.devNotFound.f)(request, Play.maybeApplication.flatMap(_.routes)))
  */
  }

  /** Called when an action has been found, but the request parsing has failed.
    *
    * The default is to send the framework default 400 page.
    *
    * @param request the HTTP request header
    * @return the result to send to the client
    */
  def onBadRequest(request: Reactor, error: String) = {
    /*
  BadRequest(views.html.defaultpages.badRequest(request, error))
  */
  }

  def onActionCompletion(request: Reactor) = {
  }
}
