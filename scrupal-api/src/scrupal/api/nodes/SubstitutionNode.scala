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
import scrupal.api._
import scrupal.db.VariantReaderWriter
import spray.http.{MediaType, MediaTypes}

/** Generate Content by substituting values in a template
  * This allows users to create template type content in their browser. It is simply
  * a bunch of bytes to generate but with @{...} substitutions. What goes in the ... is essentially a function call.
  * You can substitute a node (@{node('mynode}), values from the [[scrupal.api.Context]] (@{context.`var_name`}),
  * predefined variables/functions (@{datetime}), etc.
case class SubstitutionNode (
  description: String,
  script: String,
  subordinates: Map[String, Either[NodeRef,Node]] = Map.empty[String, Either[NodeRef,Node]],
  modified: Option[DateTime] = Some(DateTime.now()),
  created: Option[DateTime] = Some(DateTime.now()),
  _id: BSONObjectID = BSONObjectID.generate,
  final val kind: Symbol = SubstitutionNode.kind
) extends CompoundNode {
  final val mediaType: MediaType = MediaTypes.`text/html`
  def resolve(ctxt: Context, tags: Map[String,(Node,EnumeratorResult)]) : EnumeratorResult = {
    // val layout = Layout(layoutId).getOrElse(Layout.default)
    val template: Array[Byte] = script.getBytes(utf8)
    EnumeratorResult(LayoutProducer(template, tags).buildEnumerator, mediaType)
  }
}

object SubstitutionNode {
  import scrupal.api.BSONHandlers._
  final val kind = 'Substitution
  object SubstitutionNodeNodeBRW extends VariantReaderWriter[Node,SubstitutionNode] {
    implicit val SubstitutionNodeHandler: BSONHandler[BSONDocument,SubstitutionNode] = Macros.handler[SubstitutionNode]
    override def fromDoc(doc: BSONDocument): SubstitutionNode = SubstitutionNodeHandler.read(doc)
    override def toDoc(obj: Node): BSONDocument = SubstitutionNodeHandler.write(obj.asInstanceOf[SubstitutionNode])
  }
  Node.variants.register(kind, SubstitutionNodeNodeBRW)
}
  */
