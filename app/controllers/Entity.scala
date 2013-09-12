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

import play.api.mvc.{Action, Controller}
import play.api.templates.Html


/**
 * The controller for handling RESTful interaction with Entities
 * This controller provides the actions necessary to securely invoke
 */
object Entity extends Controller
{
	def create(kind: String) = Action { implicit request =>
		Ok(views.html.main("Create for "+ kind)( Html("<p>TBI</p>") ) )
	}

	def read(kind: String, id: String) = Action { implicit request =>
		Ok(views.html.main("Read for "+kind)( Html("<p>TBI</p>") ) )
	}

	def update(kind: String, id: String) = Action { implicit request =>
		Ok(views.html.main("Update for "+kind)( Html("<p>TBI</p>") ) )
	}

	def delete(kind: String, id: String) = Action { implicit request =>
		Ok(views.html.main("Delete for "+ kind)( Html("<p>TBI</p>") ) )
	}

	def info(kind: String, id: String) = Action { implicit request =>
		Ok(views.html.main("Info for "+ kind)( Html("<p>TBI</p>") ) )
	}

	def options(kind: String, id: String) = Action { implicit request =>
		Ok(views.html.main("Options for " + kind)( Html("<p>TBI</p>") ) )
	}


}
