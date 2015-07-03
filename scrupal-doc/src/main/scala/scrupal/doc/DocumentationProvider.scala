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

package scrupal.doc

import play.api.mvc.RequestHeader
import play.api.routing.sird._
import scrupal.api.{NodeProvider, Node}
import scrupal.core.nodes.MarkedDocNode
import scrupal.utils.ScrupalUtilsInfo

object DocumentationProvider {
  val docPathToDocsPF: PartialFunction[RequestHeader, Node] = {
    case GET(p"/doc$rest*") ⇒
      val path = rest.split("/").toIterable
      MarkedDocNode(s"Scrupal ${ScrupalUtilsInfo.version} Documentation", "doc", "public/docs", path)
  }
}

/** Provider Of Documentation
  * This is the essential thing provided bt the scrupal-doc project. It is a NodeReactorProvider that provides
  * things on the /doc sub-path from the docs assets. Documentation for scrupal is written in MarkDown format and
  * this provider uses the MarkedDocNode to translate between the two.
  */
case class DocumentationProvider() extends NodeProvider(DocumentationProvider.docPathToDocsPF)
