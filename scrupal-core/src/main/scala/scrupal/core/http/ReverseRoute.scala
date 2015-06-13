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
  def path(implicit context : Context) : String
}

class ApplicationRoute(app : Symbol) {
  def kind = 'Application
  val path = s"/${app.name}"
}
