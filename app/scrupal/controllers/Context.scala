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

import scrupal.models.db.{Alert}
import play.api.mvc.RequestHeader
import scrupal.api.{Site, Module}
import scrupal.models.CoreModule
import play.api.Configuration
import play.api.Play.current


/**
 * A context for the views so they can obtain various bits of information without a large number of parameters.
 */
case class Context(
  site : Option[Site] = None,
  modules: Seq[Module] = Seq(CoreModule),
  appName : String = "Scrupal",
  themeName: String = "cyborg",
  themeProvider: String = "bootswatch",
  user: String = "nobody",
  instance: String = "instanceName",
  devMode: Boolean = Global.DataYouShouldNotModify.devMode,
  config: Configuration = current.configuration
) (implicit val request: RequestHeader)
{
  def alerts : Seq[Alert] = Seq()

  def suggestURL : String = {
    import routes.{Home => rHome}
    import routes.{APIDoc => rAPIDoc}
    request.path match {
      case s if s.startsWith("/api") => rAPIDoc.introduction().url
      case s if s.startsWith("/doc") => rAPIDoc.introduction().url
      case s if s.startsWith("/config") => rHome.configIndex().url
      case s if s.startsWith("/asset") => rHome.docPage("assets").url
    }
  }
}

/** A trait for producing a Request's context
  * This trait simply generates the request context information from what is provided by the RequestHeader. The
  * essential piece of information is the port number on which the request was made. This is the key to the site that
  * is being served by Scrupal. Once we know which site the
  */
trait ContextProvider {

  implicit def context(implicit request: RequestHeader) : Context = {
    if (Global.ScrupalIsConfigured) {
      val afterColon : Array[String] = request.host.split(":").tail
      val port : Short = if (afterColon.length != 1) 80 else afterColon(0).toShort
      val site : Option[Site] = Global.DataYouShouldNotModify.sites.get(port)
      Context(
        site
      )
    }
    else
    {
      Context()
    }
  }
}

