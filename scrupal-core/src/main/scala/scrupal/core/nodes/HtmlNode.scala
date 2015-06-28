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

package scrupal.core.nodes

import akka.http.scaladsl.model.{MediaTypes, MediaType}
import org.joda.time.DateTime
import scrupal.api.Html
import scrupal.api.Html.ContentsArgs
import scrupal.api._

import scala.concurrent.{ ExecutionContext, Future }

abstract class AbstractHtmlNode extends Node {
  final val mediaType : MediaType = MediaTypes.`text/html`
  def content(context : Context)(implicit ec : ExecutionContext) : Future[Html.Contents]
  def apply(context: Context) : Future[Response] = {
    context.withExecutionContext { implicit ec : ExecutionContext ⇒
      content(context)(ec).map { html ⇒ HtmlResponse(Html.renderContents(html), Successful) }
    }
  }
}


/** Html Node based on Scalatags
  * This is a node that simply contains a static blob of data that it produces faithly. The data can be any type
  * but is typically html which is why its mediaType
  * @param description
  * @param modified
  * @param created
  *
  */
case class HtmlNode(
  name : String,
  description : String,
  template : Html.Template,
  modified : Option[DateTime] = Some(DateTime.now),
  created : Option[DateTime] = Some(DateTime.now)
) extends AbstractHtmlNode {
  def args : ContentsArgs = Html.EmptyContentsArgs
  def results(context : Context) : Html.Contents = template(context, args)
  def content(context : Context)(implicit ec : ExecutionContext) : Future[Html.Contents] = {
    Future.successful(results(context))
  }
}

object HtmlNode {
  /* TODO: Move HTMLNode database code to ReactiveMongo Store
  import BSONHandlers._
  final val kind = 'Html
  object HtmlNodeVRW extends VariantReaderWriter[Node, HtmlNode] {
    implicit val HtmlNodeHandler : BSONHandler[BSONDocument, HtmlNode] = Macros.handler[HtmlNode]
    override def fromDoc(doc : BSONDocument) : HtmlNode = HtmlNodeHandler.read(doc)
    override def toDoc(obj : Node) : BSONDocument = HtmlNodeHandler.write(obj.asInstanceOf[HtmlNode])
  }
  Node.variants.register(kind, HtmlNodeVRW)
  */
}

