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

import org.joda.time.DateTime
import reactivemongo.bson.{ BSONDocument, BSONHandler, BSONObjectID, Macros }
import scrupal.core.api._
import scrupal.db.VariantReaderWriter
import spray.http.{ MediaType, MediaTypes }

/** Generate Content by substituting values in a template
  * This allows users to create template type content in their browser. It is simply
  * a bunch of bytes to generate but with @{...} substitutions. What goes in the ... is essentially a function call.
  * You can substitute a node (@{node('mynode}), values from the [[scrupal.core.api.Context]] (@{context.`var_name`}),
  * predefined variables/functions (@{datetime}), etc.
  * case class SubstitutionNode (
  * description: String,
  * script: String,
  * subordinates: Map[String, Either[NodeRef,Node]] = Map.empty[String, Either[NodeRef,Node]],
  * modified: Option[DateTime] = Some(DateTime.now()),
  * created: Option[DateTime] = Some(DateTime.now()),
  * _id: BSONObjectID = BSONObjectID.generate,
  * final val kind: Symbol = SubstitutionNode.kind
  * ) extends CompoundNode {
  * final val mediaType: MediaType = MediaTypes.`text/html`
  * def resolve(ctxt: Context, tags: Map[String,(Node,EnumeratorResult)]) : EnumeratorResult = {
  * // val layout = Layout(layoutId).getOrElse(Layout.default)
  * val template: Array[Byte] = script.getBytes(utf8)
  * EnumeratorResult(LayoutProducer(template, tags).buildEnumerator, mediaType)
  * }
  * }
  *
  * object SubstitutionNode {
  * import scrupal.core.api.BSONHandlers._
  * final val kind = 'Substitution
  * object SubstitutionNodeNodeBRW extends VariantReaderWriter[Node,SubstitutionNode] {
  * implicit val SubstitutionNodeHandler: BSONHandler[BSONDocument,SubstitutionNode] = Macros.handler[SubstitutionNode]
  * override def fromDoc(doc: BSONDocument): SubstitutionNode = SubstitutionNodeHandler.read(doc)
  * override def toDoc(obj: Node): BSONDocument = SubstitutionNodeHandler.write(obj.asInstanceOf[SubstitutionNode])
  * }
  * Node.variants.register(kind, SubstitutionNodeNodeBRW)
  * }
  */
