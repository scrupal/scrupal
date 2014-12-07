/**********************************************************************************************************************
 * Copyright © 2014 Reactific Software, Inc.                                                                          *
 *                                                                                                                    *
 * This file is part of Scrupal, an Opinionated Web Application Framework.                                            *
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

package scrupal.api

/** Simple Reverse Routing For Scrupal.
  *
  * Reverse Routing is the process of generating a URI path for a certain resource. In Scrupal, paths are not arbitrary
  * but highly structured and regular. Because of this regularity, the corresponding path can be constructed quite
  * easily especially from a given context. Every operation in Scrupal has a context in which that operation occurs.
  * The context provides the site, application, request context, and other details of the operation that is
  * requesting a path. Because the context is present, the path can be generated easily by simply substituting context
  * and requested elements. Some level of validation can occur too.
  *
 */
object PathOf {
  // TODO: Add validation and context awareness, customization by applications, entities, etc.

  def favicon()(implicit context: Context) = "/assets/favicon"

  def theme(provider: String, name: String)(implicit context: Context) = s"/assets/themes/$provider/$name.css"

  def css(name: String)(implicit context: Context) = s"/assets/stylesheets/$name.css"

  def js(name: String)(implicit context: Context) = s"/assets/javascripts/$name.js"

  def font(provider: String, name: String)(implicit context: Context) = s"/assets/fonts/$provider/$name.css"

  def lib(library: String, path: String)(implicit context: Context) = s"/assets/lib/$library/$path"

  def entity(kind: String, id: String)(implicit context: Context) = s"/${context.appName}/$kind/$id"

}
