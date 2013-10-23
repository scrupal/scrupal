/**********************************************************************************************************************
 * This file is part of Scrupal a Web Application Framework.                                                          *
 *                                                                                                                    *
 * Copyright (c) 2013, Reid Spencer and viritude llc. All Rights Reserved.                                            *
 *                                                                                                                    *
 * Scrupal is free software: you can redistribute it and/or modify it under the terms                                 *
 * of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License,   *
 * or (at your option) any later version.                                                                             *
 *                                                                                                                    *
 * Scrupal is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied      *
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more      *
 * details.                                                                                                           *
 *                                                                                                                    *
 * You should have received a copy of the GNU General Public License along with Scrupal. If not, see either:          *
 * http://www.gnu.org/licenses or http://opensource.org/licenses/GPL-3.0.                                             *
 **********************************************************************************************************************/

package scrupal.controllers

import play.api._
import play.api.Mode
import java.io.File
import play.api.mvc._
import scrupal.models.CoreModule
import scrupal.api._
import scrupal.api.Module
import scrupal.api.Site
import com.typesafe.config.{ConfigRenderOptions, ConfigValue}

import scala.util.matching.Regex
import scala.collection.immutable.TreeMap

object Global extends GlobalSettings
{
  /** The only global data cache in memory that Scrupal has!
    * While most of the data that is needed to service a request is constructed into a Context object for each request
    * and thereby encouraging a share-none philosophy, sometimes that's just not the right thing to do for performance
    * reasons. Some data is so pervasively used and immutable that packaging it into the Context is just a waste. So,
    * those few bits of global data that Scrupal might need live here, everything else you get through the Context
    * object which is implicitly passed down to the controllers by mixing in ContextProvider and onward down to the
    * templates.
    * - Rule1: when placing something here, always say WHY its a good idea to have it here.
    * - Rule2: REALLY, REALLY document why it might need to be a var rather than a val!
    * - Rule3: Never create global data elsewhere, only here.
    */
  object DataYouShouldNotModify
  {
    /** The Set of sites that we serve is loaded once from the database at startup. These change very infrequently and
      * are needed for request processing on every request in order to construct the Context for the request.
      * Consequently we do not want to put them in a Cache nor query them from teh DB. They just need to be here in
      * memory available for use on each request. The sites reference is a val, nobody should ever change it,
      * but the content of the MutableSet may change as sites are added/removed. Again,
      * that's an administration operation that happens infrequently so we don't expect mutation churn on this data.
      * Note: the objects themselves are not ever mutated. When we want to make a change,
      * the new on is added and the old one is removed. So, our only vulnerability is a brief moment when the list
      * contains a duplicate.
      * Note that the type is a HashMap[Short,Site] because we want to index it quickly by the port number we are
      * serving so as to avoid a scan of this data structure on every request.
      */
    var sites : Map[Short,Site] = Map[Short,Site]()
  }

  val Copyright = "2013 viritude llc"


  /** Simple utility to determine if we are considered configured or not.
    * Some first-time logic depends on this in order to determine whether to do the "unconfigured" action or to
    * proceed normally.
    * @return
    */
  def ScrupalIsConfigured : Boolean = !DataYouShouldNotModify.sites.isEmpty

  /**
	 * Called before the application starts.
	 *
	 * Resources managed by plugins, such as database connections, are likely not available at this point.
	 *
	 * @param app the application
	 */
	override def beforeStart(app: Application) {
    DefaultGlobal.beforeStart(app)
    oneTimeInitialization(app)
	}

  def oneTimeInitialization(app: Application) {
    // We do a lot of stuff in API objects and they need to be instantiated in the right order,
    // so "touch" them now because they are otherwise initialized randomly as used
    require(Type.registryName == "Types")
    require(Module.registryName == "Modules")
    require(Site.registryName == "Sites")

    // Make sure that we registered the CoreModule as 'Core just to make sure it is instantiated at this point
    require(CoreModule.id == 'Core)

    // TODO: scan classpath for additional modules

    // We are now ready to process the registered modules
    Module.processModules

    // Set things from configuration
    val config = app.configuration
    // Features
    config.getBoolean("scrupal.developer.mode") map   { value => CoreModule.DevMode.enabled(value) }
    config.getBoolean("scrupal.developer.footer") map { value => CoreModule.DebugFooter.enabled(value) }
    config.getBoolean("scrupal.config.wizard") map    { value => CoreModule.ConfigWizard.enabled(value) }

    // Theoretically, at this point, Play! has already initialized and validated the db.*.url settings. Each one of
    // those is for a site configuration so we should be able to load the sites now.
    reload(app)
  }

  def reload(app: Application) {
    DataYouShouldNotModify.sites = Site.load(app.configuration)
  }

	/**
	 * Called once the application is started.
	 *
	 * @param app the application
	 */
	override def onStart(app: Application) {
		DefaultGlobal.onStart(app)
	}

	/**
	 * Called on application stop.
	 *
	 * @param app the application
	 */
	override def onStop(app: Application) {
		DefaultGlobal.onStop(app)
	}

  /** The Scrupal default Configuration
    * We provide this here to override the empty set provided by Play so that in case the user messed up the
    * configuration file or this is a first time run, we have all the configuration data that Scrupal needs,
    * at least in a default form. Note that to make sure this gets used, we call DefaultGlobal.onLoadConfig in our
    * override of onLoadConfig (below). We do this to make sure Play gets a chance to do its own processing before we
    * start overriding the configuration from the database.
   */
  override def configuration: Configuration = {
    Configuration.empty ++ Configuration.from( Map(
      "application.global"  -> "scrupal.api.Global",
      "application.home"    -> ".",
      "ehcacheplugin"       -> "disabled",
      "evolutionplugin"     -> "disabled",
      "http.port"           -> 8000,
      "https.port"          -> None,
      "logger.application"  -> "DEBUG",
      "logger.play"	        -> "INFO",
      "logger.root"	        -> "INFO",
      "redis.database"      -> 0,
      "redis.host"	        -> "localhost",
      "redis.maxIdle"	      -> 8,
      "redis.port"	        -> 6379,
      "smtp.mock"	          -> true
    ))
  }

  type FlatConfig =  TreeMap[String,ConfigValue]

  def interestingConfig(config: Configuration) : FlatConfig = {
    val elide : Regex = "^(akka|java|sun|user|awt|os|path|line).*".r
    val entries = config.entrySet.toSeq
    val filtered = entries filter { case (x,y) =>  !elide.findPrefixOf(x).isDefined }
    TreeMap[String,ConfigValue](filtered.toSeq:_*)
  }

  /** Merge the Scrupal Configuration with the Play Configuration
    * Scrupal stores all its configuration data in the database, even the configuration for Play. So,
    * we're either going to default the data (first time run, no db) or override most of it with the values from the
    * database. The Play configuration is complete by the time this returns.
    *
	 * Called just after configuration has been loaded, to give the application an opportunity to modify it.
	 *
	 * @param config the loaded configuration
	 * @param path the application path
	 * @param classloader The applications classloader
	 * @param mode The mode the application is running in
	 * @return The configuration that the application should use
	 */
	override def onLoadConfig(
    config: Configuration,
    path: File,
    classloader: ClassLoader,
    mode: Mode.Mode
  ): Configuration = {
    // Let Play do whatever it needs to do in its default implementation of this method.
		val newconf = DefaultGlobal.onLoadConfig(config, path, classloader, mode)

    Logger.trace("STARTUP CONFIGURATION VALUES")
    interestingConfig(newconf) foreach { case (key: String, value: ConfigValue) =>
      Logger.trace ( "    " + key + " = " + value.render(ConfigRenderOptions.defaults))
    }

    newconf
 	}

	/**
	 * Called Just before the action is used.
	 *
	 */
	override def doFilter(a: EssentialAction): EssentialAction = {
		a
	}

	/**
	 * Called when an HTTP request has been received.
	 *
	 * The default is to use the application router to find the appropriate action.
	 *
	 * @param request the HTTP request header (the body has not been parsed yet)
	 * @return an action to handle this request - if no action is returned, a 404 not found result will be sent to client
	 * @see onActionNotFound
	 */
	override def onRouteRequest(request: RequestHeader): Option[play.api.mvc.Handler] = {
    if (ScrupalIsConfigured || pathsOkayWhenUnconfigured.findFirstMatchIn(request.path).isDefined ) {
      Logger.trace("Standard Routing for: " + request.path)
      DefaultGlobal.onRouteRequest(request)
    } else {
      Logger.trace("Configuration Routing for: " + request.path)
      Some(scrupal.controllers.ConfigWizard.configure())
    }
	}
  private val pathsOkayWhenUnconfigured = "^/(assets/|webjars/|configure|doc|scaladoc)".r

  /**
	 * Called when an exception occurred.
	 *
	 * The default is to send the framework default error page.
	 *
	 * @param request The HTTP request header
	 * @param ex The exception
	 * @return The result to send to the client
	 */
	override def onError(request: RequestHeader, ex: Throwable)  = {
		DefaultGlobal.onError(request, ex)
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
	override def onHandlerNotFound(request: RequestHeader)  = {
		DefaultGlobal.onHandlerNotFound(request)
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
	override def onBadRequest(request: RequestHeader, error: String)  = {
		DefaultGlobal.onBadRequest(request, error)
		/*
		BadRequest(views.html.defaultpages.badRequest(request, error))
		*/
	}

	override def onRequestCompletion(request: RequestHeader) {
		DefaultGlobal.onRequestCompletion(request)
	}

}
