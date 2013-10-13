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

import play.api.mvc.{Action, RequestHeader, Controller}
import play.api.Routes
import scrupal.views.html
import scrupal.api.Module

/**
 * A controller to provide the Introduction To Scrupal content
 * Further description here.
 */
object Home extends ScrupalController {

  /** The home page */
	def index = Action { implicit request =>
		Ok(html.index("Welcome To Scrupal"))
	}

  /** The admin application */
  def admin = Action { implicit request =>
    Ok(html.admin(Module.all))
  }

  /** The apidoc application */
  def apidoc = Action { implicit request =>
    Ok(html.apidoc(Module.all))
  }

  def jsRoutes(varName: String = "jsRoutes") = Action { implicit request =>
    Ok(
      Routes.javascriptRouter(varName)(
        routes.javascript.Home.index,

        routes.javascript.Assets.js,
        routes.javascript.Assets.css,
        routes.javascript.Assets.misc,
        routes.javascript.Assets.js_s,
        routes.javascript.Assets.css_s,
        routes.javascript.Assets.img,
        routes.javascript.Assets.theme,

        routes.javascript.API.createAll,
        routes.javascript.API.fetchAll,
        routes.javascript.API.updateAll,
        routes.javascript.API.deleteAll,
        routes.javascript.API.summarizeAll,
        routes.javascript.API.optionsOfAll,

        routes.javascript.API.create,
        routes.javascript.API.fetch,
        routes.javascript.API.update,
        routes.javascript.API.delete,
        routes.javascript.API.summarize,
        routes.javascript.API.optionsOf
      )
    ).as(JAVASCRIPT)
  }
}
