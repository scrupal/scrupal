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

package scrupal.utils

import play.api._
import play.api.Mode
import java.io.File
import play.api.mvc.{SimpleResult, Handler, RequestHeader, EssentialAction}

object Global extends GlobalSettings
{
	/**
	 * Called before the application starts.
	 *
	 * Resources managed by plugins, such as database connections, are likely not available at this point.
	 *
	 * @param app the application
	 */
	override def beforeStart(app: Application) {
		DefaultGlobal.beforeStart(app)
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

	/**
	 * Called just after configuration has been loaded, to give the application an opportunity to modify it.
	 *
	 * @param config the loaded configuration
	 * @param path the application path
	 * @param classloader The applications classloader
	 * @param mode The mode the application is running in
	 * @return The configuration that the application should use
	 */
	override def onLoadConfig(config: Configuration, path: File, classloader: ClassLoader, mode: Mode.Mode): Configuration = {
		DefaultGlobal.onLoadConfig(config, path, classloader, mode)
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
	override def onRouteRequest(request: RequestHeader): Option[Handler] = {
		// Play.maybeApplication.flatMap(_.routes.flatMap { router => router.handlerFor(request) })
		DefaultGlobal.onRouteRequest(request)
	}

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
