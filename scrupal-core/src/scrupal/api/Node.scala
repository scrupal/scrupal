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

import java.io.File
import java.net.URL

import org.joda.time.DateTime
import play.twirl.api.{Html, Txt}
import reactivemongo.api.DefaultDB
import reactivemongo.api.indexes.{Index, IndexType}
import reactivemongo.bson._
import scrupal.api.BSONHandlers._
import scrupal.db.{VariantDataAccessObject, VariantStorable}
import spray.http.{MediaType, MediaTypes}

import scala.concurrent.Future
import scala.xml.{Elem, NodeSeq}

/** A function that generates content
  *
  * This is the basic characteristics of a Node. It is simply a function that receives a Context
  * and produces content as a Byte array. The Context provides the setting in which it is
  * generating the content. All dynamic content in Scrupal is generated through a Generator.
  */
trait Generator extends ((Context) => Future[Result[_]])

/** Content Generating Node
  *
  * This is the fundamental type of a node. It has some housekeeping information and can generate
  * some content in the form of an Array of Bytes. Modules define their own kinds of nodes by deriving
  * subclasses from this trait. As such, the Node class hierarchy defines the types of Nodes that
  * are possible to use with Scrupal. Note that Node instances are stored in the database and can be
  * very numerous. For that reason, they are not registered in an object registry.
  */
abstract class Node extends VariantStorable[BSONObjectID]
                            with Describable with Modifiable with Enablable with Generator with Bootstrappable
{
  val _id = BSONObjectID.generate
  def mediaType : MediaType
}


object HtmlHelpers {

  implicit class NodeSeqToHtml(nodes: NodeSeq) {def toHtml: Html = { Html(nodes.toString()) }}

  implicit class NodeToHtml(node: Node) {def toHtml: Html = { Html(node.toString()) }}

  implicit class ElemToHtml(elem: Elem) {def toHtml: Html = { Html(elem.toString()) }}

  implicit class StringToHtml(str: String) { def toHtml: Html = { Html(str) }}

}

import scrupal.api.HtmlHelpers._

/** Message Node
  *
  * This is a very simple node that simply renders a standard Boostrap message. It is used for generating error messages
  * during node substitution so the result is visible to the end user.
  */
case class MessageNode(
  description: String,
  css_class: String,
  message: Html
  ) extends Node {
  final val kind : Symbol = 'Message
  val mediaType: MediaType = MediaTypes.`text/html`
  var enabled: Boolean = true
  val modified  = None
  val created = None
  def apply(ctxt: Context): Future[Result[_]] = Future.successful {
    val text = <div class={css_class}>{message.body}</div>
    HtmlResult(text toHtml, Successful)
  }
}

object MessageNode {
  implicit val MessageNodeHandler : BSONHandler[BSONDocument,MessageNode] = Macros.handler[MessageNode]
}

/** Basic Node
  * This is a node that simply contains a static blob of data that it produces faithly. The data can be any type
  * but is typically html which is why its mediaType
  * @param description
  * @param enabled
  * @param modified
  * @param created
  *
  */
case class HtmlNode (
  description: String,
  template: TwirlHtmlTemplate,
  args: Map[String, Html],
  var enabled: Boolean = true,
  modified: Option[DateTime] = None,
  created: Option[DateTime] = None
  ) extends Node {
  final val mediaType: MediaType = MediaTypes.`text/html`
  final val kind : Symbol = 'Html
  def apply(ctxt: Context): Future[Result[_]] = {
    val html = template(args,ctxt)
    Future.successful(HtmlResult(html, Successful))
  }
}

object HtmlNode {
  implicit val HtmlNodeHandler = Macros.handler[HtmlNode]
}

case class TxtNode (
  description: String,
  template: TwirlTxtTemplate,
  args: Map[String, Txt],
  var enabled: Boolean = true,
  modified: Option[DateTime] = None,
  created: Option[DateTime] = None
  ) extends Node {
  final val mediaType: MediaType = MediaTypes.`text/html`
  final val kind : Symbol = 'Text
  def apply(context: Context): Future[Result[_]] = {
    val txt : Txt = template(args,context)
    Future.successful(TxtResult(txt, Successful))
  }
}

object TxtNode {
  implicit val TxtNodeHandler = Macros.handler[TxtNode]
}

/** Asset Node
  * This node type generates the content of an asset from a file typically bundled with the module. This is not
  * intended for use with delivering files uploaded to the server. Those should be handled by generating a link
  * and allowing CDN to deliver the content. You want to use a LinkNode for that.
  * @param description
  * @param file
  * @param enabled
  * @param modified
  * @param created
  */
case class FileNode (
  description: String,
  file: File,
  override val mediaType: MediaType = MediaTypes.`text/html`,
  var enabled: Boolean = true,
  modified: Option[DateTime] = None,
  created: Option[DateTime] = None
  ) extends Node {
  final val kind : Symbol = 'Asset
  def apply(ctxt: Context): Future[Result[_]] = ctxt.scrupal.withExecutionContext { implicit ec =>
    Future {
      // FIXME: make this file I/O non-blocking and resource managed !
      val source = scala.io.Source.fromFile(file)
      val content = source.getLines() mkString "\n"
      val extension = {
        val name = file.getName
        name.lastIndexOf(".") match {
          case i: Int if i >= 0 ⇒ file.getName.substring(i + 1)
          case _ ⇒ ""
        }
      }
      MediaTypes.forExtension(extension) match {
        case Some(mt) ⇒ OctetsResult(content.getBytes(utf8), mt)
        case _ ⇒ toss(s"Unsupported file type, $extension, for FileNode, ${file.getPath}")
      }
    }
  }
}

object FileNode {
  implicit val FileNodeHandler = Macros.handler[FileNode]
}

/** Link Node
  * This node type contains a URL to a resource and generates a link to it.
  */
case class LinkNode (
  description: String,
  url: URL,
  var enabled: Boolean = true,
  modified: Option[DateTime] = None,
  created: Option[DateTime] = None
  ) extends Node {
  final val kind : Symbol = 'Link
  override val mediaType: MediaType = MediaTypes.`text/html`
  def apply(ctxt: Context): Future[Result[_]] = Future.successful {
    HtmlResult(<a href={url.toString}>{description}</a> toHtml,Successful)
  }
}

object LinkNode {
  implicit val LinkNodeHandler : BSONHandler[BSONDocument,LinkNode] = Macros.handler[LinkNode]
}

trait HtmlSubstituter extends ((Map[String, Node], Context) => Html)

/** A Node That Is A Layout.
  *
  * A Layout in Scrupal is an object that can generate a result, such as a page, with certain areas substituted by
  * node content. The substitutions are tracked by a mapping from tag name to a node reference. The node is generated
  * and substituted wherever the tag's name appears in the layout. The layout used by this node is also a reference
  * to a memory object. Scrupal defines a core set of layouts but other modules can define more.
  * @param description
  * @param tags
  * @param layoutId
  * @param mediaType
  * @param enabled
  * @param modified
  * @param created
  */
case class LayoutNode (
  description: String,
  tags: Map[String, BSONObjectID],
  layoutId: Identifier,
  override val mediaType: MediaType,
  var enabled: Boolean = true,
  modified: Option[DateTime] = None,
  created: Option[DateTime] = None
  ) extends Node {
  final val kind : Symbol = 'Layout
  def apply(ctxt: Context): Future[Result[_]] = ctxt.withScrupalStuff { (_,_,schema, ec) =>
    implicit val execCtxt = ec
    val layout = Layout(layoutId).getOrElse(Layout.default)
    val pairsF = for ( (tag,id) <- tags ) yield {
      schema.nodes.fetch(id) map {
        case Some(node) => tag -> (node(ctxt) map { r ⇒ node → r } )
        case None => tag -> {
          val node = MessageNode("Missing Node", "alert-warning", s"Could not find node '$tag".toHtml)
          node(ctxt) map { r ⇒ node → r }
        }
      }
    }
    (Future sequence pairsF) flatMap { x =>
      val x2 = Future.sequence { x.map { entry ⇒ entry._2.map(i ⇒ entry._1 → i) } }
      x2
    } map { args ⇒
      layout(args.toMap,ctxt)
    }
  }
}

object LayoutNode {
  implicit val LayoutNodeHandler: BSONHandler[BSONDocument, LayoutNode] = Macros.handler[LayoutNode]
}

/** Generate content with Scala code
  *
  * For the professional programmer maintaining a site, this is essentially an Enumeratee that reads a stream of
  * bytes (potentially empty) and produces a stream as output. The output is the generated content. The full power of
  * Scala is at your fingertips with REPL like simplicity. The code is dynamically compiled and executed to produce
  * the filter. Like Scrupaleasy, these can be producers, consuemrs or filters and are intented to execute stand-alone or
  * in a pipeline.
  * @param description
  * @param code
  * @param modified
  * @param created
  * @param kind
  */
case class ScalaNode (
  description: String,
  code: String,
  modified: Option[DateTime] = None,
  created: Option[DateTime] = None,
  final val kind : Symbol = 'Scala
  ) extends Node {
  override val mediaType: MediaType = MediaTypes.`text/html`

  def apply(ctxt: Context): Future[Result[_]] = Future.successful {
    // TODO: Implement ScalaNode using Java ScriptEngine
    // import javax.script.ScriptEngineManager
    // val e = new ScriptEngineManager().getEngineByName("scala")
    // e.eval("1 to n.asInstanceOf[Int] foreach println")
    HtmlResult("Not Implemented".toHtml, Unimplemented)
  }
}

object ScalaNode {
  implicit val ScalaNodeHandler : BSONHandler[BSONDocument,ScalaNode] = Macros.handler[ScalaNode]
}

/** Generate content With an operating system command
  *
  * This will invoke a local operating system command to generate content. As this forks the VM it should be
  * restricted to administrative users. However, for those cases where python, bash, perl or a simple grep/awk/sed
  * pipeline is the best thing, this is the tool to use. It simply passes the string to the local operating system's
  * command processor for interpretation and execution. Whatever it generates is Streamed as a result to this node.
  *
  * @param description
  * @param command
  * @param modified
  * @param created
  * @param kind
  */
case class CommandNode (
  description: String,
  command: String,
  modified: Option[DateTime] = None,
  created: Option[DateTime] = None,
  final val kind : Symbol = 'Scala
  ) extends Node {
  override val mediaType: MediaType = MediaTypes.`text/html`

  def apply(ctxt: Context): Future[Result[_]] = Future.successful {
    // TODO: implement CommandNode
    HtmlResult("Not Implemented".toHtml, Unimplemented)
  }
}

object CommandNode {
  implicit val CommandNodeHandler : BSONHandler[BSONDocument,CommandNode] = Macros.handler[CommandNode]
}

/** Generate Content with TwirlScript Template
  * This allows users to create template type content in their browser. It looks a bit like twirl in that it is simply
  * a bunch of bytes to generate but with @{...} substitutions. What goes in the ... is essentially a function call.
  * You can substitute a node (@{node('mynode}), values from the [[scrupal.core.api.Context]] (@{context.`var_name`}),
  * predefined variables/functions (@{datetime}), etc.
  */
case class TwirlScriptNode (
  description: String,
  twirl_script: String,
  modified: Option[DateTime] = None,
  created: Option[DateTime] = None,
  final val kind : Symbol = 'Scala
  ) extends Node {
  override val mediaType: MediaType = MediaTypes.`text/html`

  def apply(ctxt: Context): Future[Result[_]] = Future.successful {
    // TODO: implement the TwirlScriptNode content generator
    HtmlResult("Not Implemented".toHtml, Unimplemented)
  }
}

object TwirlScriptNode {
  implicit val TwirlScriptNodeHandler : BSONHandler[BSONDocument,TwirlScriptNode] = Macros.handler[TwirlScriptNode]
}

/** Generate Content With Scrupalesy
  *
  * This gets to be fun. Scrupaleasy is a Scala DSL designed to be a very simple language to help generate some content
  * Unlike the TwirlScriptNode, this is not a template but an actual program to generate content. Scrupaleasy should be
  * dead simple: designed for non-programmers. For compatibility with TwirlScript it can access any of the same values
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
  * - Insertions with a TwirlScript element: `@{foo(...)}`
  *
  * - Require statements: like Scala require and throws an exception to stop pipeline processing
  *
  * - A lot of fun and joy for Scrupal authors :)
  *
  * @param description
  * @param scrupalesy
  * @param modified
  * @param created
  * @param kind
  */
case class ScrupalesyNode (
  description: String,
  scrupalesy: String,
  modified: Option[DateTime] = None,
  created: Option[DateTime] = None,
  final val kind : Symbol = 'Scala
  ) extends Node {
  override val mediaType: MediaType = MediaTypes.`text/html`

  def apply(ctxt: Context): Future[Result[_]] = Future.successful {
    // TODO: Implement ScrupaleasyNode
    HtmlResult("Not Implemented".toHtml, Unimplemented)
  }
}

object ScrupalesyNode {
  implicit val ScrupalesyNodeHandler : BSONHandler[BSONDocument,ScrupalesyNode] = Macros.handler[ScrupalesyNode]
}

object Node {

  lazy val Empty = MessageNode("Empty Node", "text-danger", Html("This node is completely empty."))

  implicit lazy val NodeReader = new VariantBSONDocumentReader[Node] {
    def read(doc: BSONDocument) : Node = {
      doc.getAs[BSONString]("kind") match {
        case Some(str) =>
          str.value match {
            case "Message"  => MessageNode.MessageNodeHandler.read(doc)
            case "Html"    => HtmlNode.HtmlNodeHandler.read(doc)
            case "Asset"    => FileNode.FileNodeHandler.read(doc)
            case "Link"     => LinkNode.LinkNodeHandler.read(doc)
            case "Layout"   => LayoutNode.LayoutNodeHandler.read(doc)
          }
        case None => toss(s"Field 'kind' is missing from Node: ${doc.toString()}")
      }
    }
  }

  implicit val NodeWriter = new VariantBSONDocumentWriter[Node] {
    def write(node: Node) : BSONDocument = {
      node.kind match {
        case 'Message  => MessageNode.MessageNodeHandler.write(node.asInstanceOf[MessageNode])
        case 'Html     => HtmlNode.HtmlNodeHandler.write(node.asInstanceOf[HtmlNode])
        case 'Asset    => FileNode.FileNodeHandler.write(node.asInstanceOf[FileNode])
        case 'Link     => LinkNode.LinkNodeHandler.write(node.asInstanceOf[LinkNode])
        case 'Layout   => LayoutNode.LayoutNodeHandler.write(node.asInstanceOf[LayoutNode])
      }
    }
  }

  case class NodeDAO(db: DefaultDB) extends VariantDataAccessObject[Node,BSONObjectID] {
    final def collectionName: String = "nodes"
    implicit val writer = new Writer(NodeWriter)
    implicit val reader = new Reader(NodeReader)
    implicit val converter = BSONObjectIDConverter

    override def indices : Traversable[Index] = super.indices ++ Seq(
      Index(key = Seq("mediaType" -> IndexType.Ascending), name = Some("mediaType")),
      Index(key = Seq("kind" -> IndexType.Ascending), name = Some("kind"))
    )
  }
}

