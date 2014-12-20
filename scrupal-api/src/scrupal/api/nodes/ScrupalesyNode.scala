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

package scrupal.api.nodes

import org.joda.time.DateTime
import reactivemongo.bson.{BSONDocument, BSONHandler, BSONObjectID, Macros}
import scalatags.Text.all._
import scrupal.api._
import scrupal.db.VariantReaderWriter
import spray.http.{MediaType, MediaTypes}

import scala.concurrent.Future

/** Generate Content With Scrupalesy
  *
  * This gets to be fun. Scrupaleasy is a Scala DSL designed to be a very simple language to help generate some content
  * Unlike the SubstitutionNode, this is not a template but an actual program to generate content. Scrupaleasy should be
  * dead simple: designed for non-programmers. For compatibility with SubstitutionNode it can access any of the same
  * values
  * with @{...} syntax but these can then be processed as UTF-8 characters, e.g. such as filtering. Setting up a Unix
  * like piepeline should be its top construction, essentially { block1 } | { block2 } would compute output from
  * block1 which is given as input to block2 which then yields the result. All Scrupaleasy scripts are either consumers,
  * producers or filters. Consumers eat input but produce no output. Instead they can "throw" an error. Consumers are
  * used to stop content from happening. They usually come at the end of a pipe to validate content. Producers ignore
  * their input but produce output. They are usually the first thing in a pipeline. Filters read their input and
  * convert it to some output. They are usually in the middle of a pipeline. You can compose a processing pipeline like
  * this by invoking a Scrupaleasy thing from another. The DSL should provide:
  *
  * - Basic if/else and select-from-options logic flow
  *
  * - Functions to manipulate streams of characters (e.g. length, regex match, substring, beginsWith, etc.)
  *
  * - The infamous munge function for text editing of Mac lore
  *
  * - Insertions with a SubstitutionNode element: `@{foo(...)}`
  *
  * - Require statements: like Scala require and throws an exception to stop pipeline processing
  *
  * - A lot of fun and joy for Scrupal authors :)
  *
  * @param description
  * @param scrupalesy
  * @param modified
  * @param created
  */
case class ScrupalesyNode (
  description: String,
  scrupalesy: String,
  modified: Option[DateTime] = Some(DateTime.now()),
  created: Option[DateTime] = Some(DateTime.now()),
  _id: BSONObjectID = BSONObjectID.generate,
  final val kind: Symbol = ScrupalesyNode.kind
) extends Node {
  override val mediaType: MediaType = MediaTypes.`text/html`

  def apply(ctxt: Context): Future[Result[_]] = Future.successful {
    // TODO: Implement ScrupaleasyNode
    HtmlResult(span("Not Implemented"), Unimplemented)
  }
}

object ScrupalesyNode {
  import scrupal.api.BSONHandlers._
  final val kind = 'Scrupalesy
  object ScrupalesyNodeBRW extends VariantReaderWriter[Node,ScrupalesyNode] {
    implicit val ScrupalesyNodeHandler : BSONHandler[BSONDocument,ScrupalesyNode] = Macros.handler[ScrupalesyNode]
    override def fromDoc(doc: BSONDocument): ScrupalesyNode = ScrupalesyNodeHandler.read(doc)
    override def toDoc(obj: Node): BSONDocument = ScrupalesyNodeHandler.write(obj.asInstanceOf[ScrupalesyNode])
  }
  Node.variants.register(kind, ScrupalesyNodeBRW)
}

