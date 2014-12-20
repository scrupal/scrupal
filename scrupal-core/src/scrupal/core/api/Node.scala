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

package scrupal.core.api

import org.joda.time.DateTime
import play.api.libs.iteratee.Enumerator
import reactivemongo.api.DefaultDB
import reactivemongo.api.indexes.{Index, IndexType}
import reactivemongo.bson._
import scrupal.core.api.BSONHandlers._
import scrupal.core.api.Node.NodeDAO
import scrupal.core.nodes._
import scrupal.db._
import spray.http.{MediaType, MediaTypes}

import scala.concurrent.{ExecutionContext, Future}
import scala.language.existentials

/** A function that generates content
  *
  * This is the basic characteristics of a Node. It is simply a function that receives a Context
  * and produces content as a Byte array. The Context provides the setting in which it is
  * generating the content. All dynamic content in Scrupal is generated through a Generator.
  */
trait Generator extends ((Context) => Future[Result[_]])

case class NodeRef(kind: String, ref: DBRef)

object NodeRef {
  lazy val nodeRefHandler : BSONHandler[BSONDocument, NodeRef] = Macros.handler[NodeRef]
}

/** Content Generating Node
  *
  * This is the fundamental type of a node. It has some housekeeping information and can generate
  * some content in the form of an Array of Bytes. Modules define their own kinds of nodes by deriving
  * subclasses from this trait. As such, the Node class hierarchy defines the types of Nodes that
  * are possible to use with Scrupal. Note that Node instances are stored in the database and can be
  * very numerous. For that reason, they are not registered in an object registry.
  */
trait Node extends VariantStorable[BSONObjectID]
           with Describable with Modifiable with Generator with Bootstrappable
{
  def mediaType : MediaType
  def reference(collection: String, db: Option[String] = None) : NodeRef = {
    NodeRef(kind.name, DBRef(collection, _id, db))
  }
}

object Node {

  lazy val Empty = MessageNode("Empty Node", "text-danger", "This node is completely empty.")

  object variants extends VariantRegistry[Node]("Node")

  implicit lazy val NodeReader : VariantBSONDocumentReader[Node] = new VariantBSONDocumentReader[Node] {
    def read(doc: BSONDocument) : Node = variants.read(doc)
  }

  implicit val NodeWriter : VariantBSONDocumentWriter[Node] = new VariantBSONDocumentWriter[Node] {
    def write(node: Node) : BSONDocument = variants.write(node)
  }

  case class NodeDAO(db: DefaultDB) extends VariantDataAccessObject[Node,BSONObjectID] {
    final def collectionName: String = "nodes"
    implicit val writer = new Writer(variants)
    implicit val reader = new Reader(variants)
    implicit val converter = BSONObjectIDConverter

    override def indices : Traversable[Index] = super.indices ++ Seq(
      Index(key = Seq("mediaType" -> IndexType.Ascending), name = Some("mediaType")),
      Index(key = Seq("kind" -> IndexType.Ascending), name = Some("kind"))
    )
  }
}



abstract class CompoundNode extends Node {
  def subordinates : Map[String, Either[NodeRef,Node]]
  def resolve(ctxt: Context, tagged_data: Map[String, (Node,EnumeratorResult)]) : EnumeratorResult
  def apply(ctxt: Context) : Future[Result[_]] = {
    ctxt.withExecutionContext { implicit ec: ExecutionContext ⇒
      ctxt.withSchema { case (dbc,schema) ⇒
        schema.withDB { db: ScrupalDB ⇒
          val futures_nested: Iterable[Future[(String, (Node,EnumeratorResult))]] = {
            for ((name, nr) ← subordinates) yield {
              if (nr.isLeft) {
                val dao = NodeDAO(db)
                val nodeRef = nr.left.get
                val future = dao.fetch(nodeRef.ref.id).map {
                  case Some(node) ⇒ name → node(ctxt).map { r ⇒ node → r() }
                  case None ⇒ throw new Exception(s"$nodeRef not found.")
                }
                val f = future.flatMap { case (key, value) ⇒ value.map { er ⇒ key → er } }
                f
              } else {
                val node = nr.right.get
                val nested = node(ctxt).map { r => node → r() }
                val f = nested.map { x ⇒ name → x }
                f
              }
            }
          }
          val futures_mapped = (Future sequence futures_nested).map { pairs ⇒ pairs.toMap }
          futures_mapped.map { x ⇒ resolve(ctxt, x) }
        }
      }
    }
  }
}

abstract class AbstractHtmlNode extends Node {
  final val mediaType: MediaType = MediaTypes.`text/html`
  def content(context: Context)(implicit ec: ExecutionContext) : Future[Html.Contents]
  def apply(context: Context) : Future[Result[_]] = {
    context.withExecutionContext { implicit ec: ExecutionContext ⇒
      content(context)(ec).map { html ⇒ HtmlResult(Html.renderContents(html), Successful) }
    }
  }
}

/* FIXME: Reinstate  when we want generalized Layout
case class LayoutProducer (
  template: Array[Byte],
  tags: Map[String,(Node,EnumeratorResult)]
) {
  private val end: Int = template.length
  private var index: Int = 0

  private val skip = 0
  private val found_one = 1
  private val found_two = 2
  private val found_three = 3
  private val consume_tag = 4
  private val found_tag = 5
  private val found_close_one = 6
  private val found_close_two = 7

  private def nextTag: (Option[String], Int, Int) = {
    var state: Int = 0
    var marker_start: Int = 0
    var tag_start: Int = 0
    var tag_len: Int = 0
    for (i ← index to end - 1) {
      if (template(i) == '@') {
        state match {
          case s:Int if s == skip ⇒ state = found_one; marker_start = i
          case s:Int if s == found_one ⇒ state = found_two
          case s:Int if s == found_two ⇒ state = found_three
          case s:Int if s == found_three ⇒ state = consume_tag;
          case s:Int if s == consume_tag ⇒ state = found_close_one; tag_len = i - tag_start
          case s:Int if s == found_close_one ⇒ state = found_close_two
          case s:Int if s == found_close_two ⇒
            return (Some(new String(template, tag_start, tag_len, utf8)), marker_start, i + 1)
          case _ ⇒ throw new Exception("Error in template state machine")
        }
      } else {
        state match {
          case s:Int if s == skip ⇒ /* just keep going */
          case s:Int if s == found_one ⇒ state = skip
          case s:Int if s == found_two ⇒ state = skip
          case s:Int if s == found_three ⇒ state = consume_tag; tag_start = i
          case s:Int if s == consume_tag ⇒ state = consume_tag
          case s:Int if s == found_close_one ⇒ state = skip
          case s:Int if s == found_close_two ⇒ state = skip
          case _ ⇒ throw new Exception("Error in template state machine")
        }
      }
    }
    // Didn't match any tags, don't update index
    (None, end, index)
  }

  def buildEnumerator: Enumerator[Array[Byte]] = {
    var enums = Seq.empty[Enumerator[Array[Byte]]]
    while (index < end) {
      val (tag, block_end, next_index) = nextTag
      tag match {
        case Some(tagName) ⇒
          enums :+= Enumerator(template.slice(index, block_end))
          tags.get(tagName) match {
            case Some((node, en)) ⇒ enums :+= en.payload
            case none ⇒ enums :+= Enumerator(s"@@@ Missing Tag '$tagName' @@@".getBytes(utf8))
          }
          index = next_index
        case None ⇒
          enums :+= Enumerator(template.slice(index, end))
          index = end
      }
    }
    if (enums.isEmpty)
      Enumerator(Array.empty[Byte])
    else if (enums.length == 1) {
      enums.head
    } else {
      enums.tail.foldLeft(enums.head) { case (accu, next) ⇒ accu.andThen(next)}
    }
  }
}

/** A Node That Is A Layout.
  *
  * A Layout in Scrupal is an object that can generate a result, such as a page, with certain areas substituted by
  * node content. The substitutions are tracked by a mapping from tag name to a node reference. The node is generated
  * and substituted wherever the tag's name appears in the layout. The layout used by this node is also a reference
  * to a memory object. Scrupal defines a core set of layouts but other modules can define more.
  * @param description
  * @param subordinates
  * @param layout
  * @param modified
  * @param created
  */
case class LayoutNode (
  description: String,
  subordinates: Map[String,Html.Fragment],
  layout: Layout,
  modified: Option[DateTime] = Some(DateTime.now),
  created: Option[DateTime] = Some(DateTime.now),
  _id: BSONObjectID = BSONObjectID.generate,
  final val kind: Symbol = LayoutNode.kind
) extends CompoundNode {
  final val mediaType = layout.mediaType
  def resolve(ctxt: Context, tags: Map[String,Html.Fragment]) : EnumeratorResult = {
    // val layout = Layout(layoutId).getOrElse(Layout.default)
    val template: Array[Byte] = layout(tags, ctxt)
    EnumeratorResult(LayoutProducer(template, tags).buildEnumerator, mediaType)
  }
}

object LayoutNode {
  import BSONHandlers._
  final val kind = 'Layout
  object LayoutNodeVRW extends VariantReaderWriter[Node,LayoutNode] {
    implicit val LayoutNodeHandler : BSONHandler[BSONDocument,LayoutNode] = Macros.handler[LayoutNode]
    override def fromDoc(doc: BSONDocument): LayoutNode = LayoutNodeHandler.read(doc)
    override def toDoc(obj: Node): BSONDocument = LayoutNodeHandler.write(obj.asInstanceOf[LayoutNode])
  }
  Node.variants.register(kind, LayoutNodeVRW)
}

*/
