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

package scrupal.core.nodes

import org.joda.time.DateTime
import reactivemongo.bson.{Macros, BSONDocument, BSONHandler, BSONObjectID}
import scrupal.core.api.Html.ContentsArgs
import scrupal.core.api._
import scrupal.db.VariantReaderWriter

import scala.concurrent.{ExecutionContext, Future}

/** Html Node based on Scalatags
  * This is a node that simply contains a static blob of data that it produces faithly. The data can be any type
  * but is typically html which is why its mediaType
  * @param description
  * @param modified
  * @param created
  *
  */
case class HtmlNode (
  description: String,
  template: Html.Template,
  modified: Option[DateTime] = Some(DateTime.now),
  created: Option[DateTime] = Some(DateTime.now),
  _id: BSONObjectID = BSONObjectID.generate,
  final val kind: Symbol = HtmlNode.kind
) extends AbstractHtmlNode {
  def args: ContentsArgs = Html.EmptyContentsArgs
  def results(context: Context) : Html.Contents = template(context,args)
  def content(context: Context)(implicit ec: ExecutionContext) : Future[Html.Contents] = {
    Future.successful(results(context))
  }
}

object HtmlNode {
  import BSONHandlers._
  final val kind = 'Html
  object HtmlNodeVRW extends VariantReaderWriter[Node,HtmlNode] {
    implicit val HtmlNodeHandler : BSONHandler[BSONDocument,HtmlNode] = Macros.handler[HtmlNode]
    override def fromDoc(doc: BSONDocument): HtmlNode = HtmlNodeHandler.read(doc)
    override def toDoc(obj: Node): BSONDocument = HtmlNodeHandler.write(obj.asInstanceOf[HtmlNode])
  }
  Node.variants.register(kind, HtmlNodeVRW)
}

