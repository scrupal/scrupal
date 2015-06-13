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

package scrupal.core.http

import scrupal.core.api.Context

/** Simple Reverse Routing For Scrupal
  *
  * Reverse Routing is the process of generating a URI path for a certain resource. In Scrupal, paths are not arbitrary
  * but highly structured and regular. Because of this regularity, the corresponding path can be constructed quite
  * easily especially from a given context. Every operation in Scrupal has a context in which that operation occurs.
  * The context provides the site, application, request context, and other details of the operation that is
  * requesting a reverse route. Because the context is present, the reverse route can be generated easily by simply
  * substituting context and requested elements.
  *
  * Each controller must provide the ReverseRoute objects that it serves. These ReverseRoute objects are really
  * just patterns for constructing the path. When needed,
  */
trait ReverseRoute {
  def kind : Symbol
  def path(implicit context: Context) : String
}

class ApplicationRoute(app: Symbol) {
  def kind = 'Application
  val path = s"/${app.name}"
}
