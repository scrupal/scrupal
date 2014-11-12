package scrupal.core

import java.io.File
import java.net.URL

import org.joda.time.DateTime
import play.twirl.api.Html
import reactivemongo.api.DefaultDB
import reactivemongo.api.indexes.{IndexType, Index}
import reactivemongo.bson._
import resource._
import scrupal.core.api._
import scrupal.db.VariantIdentifierDAO
import spray.http.{MediaTypes, MediaType}

import scala.concurrent.Future

/** Message Node
  *
  * This is a very simple node that simply renders a standard Boostrap message. It is used for generating error messages
  * during node substitution so the result is visible to the end user.
  */
case class MessageNode(
  _id : Identifier,
  description: String,
  css_class: String,
  message: Html,
  final val kind : Symbol = 'Message
) extends Node {
  val mediaType: MediaType = MediaTypes.`text/html`
  var enabled: Boolean = true
  val modified  = None
  val created = None
  def apply(ctxt: Context): Future[Array[Byte]] = Future.successful {
    <div class={css_class}>{message.body}</div>.text.getBytes(utf8)
  }
}

object MessageNode {
  implicit val MessageNodeHandler : BSONHandler[BSONDocument,MessageNode] = Macros.handler[MessageNode]
}

/** Basic Node
  * This is a node that simply contains a static blob of data that it produces faithly. The data can be any type
  * but is typically html which is why its mediaType
  * @param _id
  * @param description
  * @param enabled
  * @param modified
  * @param created
  *
  */
case class BasicNode (
  _id : Identifier,
  description: String,
  payload: Array[Byte],
  mediaType: MediaType = MediaTypes.`text/html`,
  var enabled: Boolean = true,
  modified: Option[DateTime] = None,
  created: Option[DateTime] = None,
  final val kind : Symbol = 'Basic
) extends Node {
  def apply(ctxt: Context): Future[Array[Byte]] = Future.successful(payload)
}

object BasicNode {
  implicit val BasicNodeHandler = Macros.handler[BasicNode]
}

/** Asset Node
  * This node type generates the content of an asset from a file typically bundled with the module. This is not
  * intended for use with delivering files uploaded to the server. Those should be handled by generating a link
  * and allowing CDN to deliver the content. You want to use a LinkNode for that.
  * @param _id
  * @param description
  * @param file
  * @param enabled
  * @param modified
  * @param created
  */
case class AssetNode (
  _id : Identifier,
  description: String,
  file: File,
  override val mediaType: MediaType = MediaTypes.`text/html`,
  var enabled: Boolean = true,
  modified: Option[DateTime] = None,
  created: Option[DateTime] = None,
  final val kind : Symbol = 'Asset
) extends Node {
  def apply(ctxt: Context): Future[Array[Byte]] = ctxt.scrupal.withExecutionContext { implicit ec =>
    Future {
      // FIXME: make this file I/O non-blocking!
      val result = managed(scala.io.Source.fromFile(file)) map { source =>
        val body = source.getLines() mkString "\n"
        body.getBytes(utf8)
      }
      result.now
    }
  }
}

object AssetNode {
  implicit val AssetNodeHandler = Macros.handler[AssetNode]
}

/** Link Node
  * This node type contains a URL to a resource and generates a link to it.
  */
case class LinkNode (
  _id : Identifier,
  description: String,
  url: URL,
  var enabled: Boolean = true,
  modified: Option[DateTime] = None,
  created: Option[DateTime] = None,
  final val kind : Symbol = 'Link
) extends Node {
  override val mediaType: MediaType = MediaTypes.`text/html`
  def apply(ctxt: Context): Future[Array[Byte]] = Future.successful {
    <a href={url.toString}>{description}</a>.text.getBytes(utf8)
  }
}

object LinkNode {
  implicit val LinkNodeHandler : BSONHandler[BSONDocument,LinkNode] = Macros.handler[LinkNode]
}

trait HtmlSubstituter extends ((Map[String, Node], Context) => Html)

case class LayoutNode (
  _id : Identifier,
  description: String,
  tags: Map[String, Symbol],
  layoutId: Identifier,
  override val mediaType: MediaType,
  var enabled: Boolean = true,
  modified: Option[DateTime] = None,
  created: Option[DateTime] = None,
  final val kind : Symbol = 'Layout
) extends Node {
  def apply(ctxt: Context): Future[Array[Byte]] = ctxt.withScrupalStuff { (_,_,schema, ec) =>
    implicit val execCtxt = ec
    val layout = Layout(layoutId).getOrElse(Layout.default)
    val pairsF = for ( (tag,id) <- tags ) yield {
      schema.nodes.fetch(id) map {
        case Some(node) => tag -> node
        case None => tag -> MessageNode('warning,"alert-warning", "", Html(s"Could not find node '$tag"))
      }
    }
    val argsF = Future sequence pairsF map { x => x.toMap }
    for ( args <- argsF ) yield {
      layout(ctxt, args)
    }
  }
}

object LayoutNode {

  implicit val TagsHandler = new BSONHandler[BSONDocument, Map[String,Symbol]] {
    def read(doc: BSONDocument) : Map[String,Symbol] = {
      for ((key,value) <- doc.elements) yield { key -> Symbol(value.asInstanceOf[BSONString].value) }
    }.toMap
    def write(tags: Map[String,Symbol]) : BSONDocument = {
      BSONDocument( for ((key,value) <- tags) yield { key -> BSONString(value.name)})
    }
  }

  implicit val LayoutNodeHandler: BSONHandler[BSONDocument, LayoutNode] = Macros.handler[LayoutNode]
}

/** Generate content with Scala code
  *
  * For the professional programmer maintaining a site, this is essentially an Enumeratee that reads a stream of
  * bytes (potentially empty) and produces a stream as output. The output is the generated content. The full power of
  * Scala is at your fingertips with REPL like simplicity. The code is dynamically compiled and executed to produce
  * the filter. Like Scrupaleasy, these can be producers, consuemrs or filters and are intented to execute stand-alone or
  * in a pipeline.
  * @param _id
  * @param description
  * @param coe
  * @param modified
  * @param created
  * @param kind
  */
case class ScalaNode (
  _id : Identifier,
  description: String,
  coe: String,
  modified: Option[DateTime] = None,
  created: Option[DateTime] = None,
  final val kind : Symbol = 'Scala
) extends Node {
  override val mediaType: MediaType = MediaTypes.`text/html`

  def apply(ctxt: Context): Future[Array[Byte]] = Future.successful {
    // TODO: Implement ScalaNode using Java ScriptEngine
    // import javax.script.ScriptEngineManager
    // val e = new ScriptEngineManager().getEngineByName("scala")
    // e.eval("1 to n.asInstanceOf[Int] foreach println")
    Array[Byte]()
  }
}

/** Generate content With an operating system command
  *
  * This will invoke a local operating system command to generate content. As this forks the VM it should be
  * restricted to administrative users. However, for those cases where python, bash, perl or a simple grep/awk/sed
  * pipeline is the best thing, this is the tool to use. It simply passes the string to the local operating system's
  * command processor for interpretation and execution. Whatever it generates is Streamed as a result to this node.
  *
  * @param _id
  * @param description
  * @param command
  * @param modified
  * @param created
  * @param kind
  */
case class CommandNode (
  _id : Identifier,
  description: String,
  command: String,
  modified: Option[DateTime] = None,
  created: Option[DateTime] = None,
  final val kind : Symbol = 'Scala
) extends Node {
  override val mediaType: MediaType = MediaTypes.`text/html`

  def apply(ctxt: Context): Future[Array[Byte]] = Future.successful {
    // TODO: implement CommandNode
    Array[Byte]()
  }
}

/** Generate Content with TwirlScript Template
  * This allows users to create template type content in their browser. It looks a bit like twirl in that it is simply
  * a bunch of bytes to generate but with @{...} substitutions. What goes in the ... is essentially a function call.
  * You can substitute a node (@{node('mynode}), values from the [[scrupal.core.api.Context]] (@{context.`var_name`}),
  * predefined variables/functions (@{datetime}), etc.
  */
case class TwirlScriptNode (
  _id : Identifier,
  description: String,
  twirl_script: String,
  modified: Option[DateTime] = None,
  created: Option[DateTime] = None,
  final val kind : Symbol = 'Scala
) extends Node {
  override val mediaType: MediaType = MediaTypes.`text/html`

  def apply(ctxt: Context): Future[Array[Byte]] = Future.successful {
    // TODO: implement the TwirlScriptNode content generator
    Array[Byte]()
  }
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
  * @param _id
  * @param description
  * @param scrupalesy
  * @param modified
  * @param created
  * @param kind
  */
case class ScrupalesyNode (
  _id : Identifier,
  description: String,
  scrupalesy: String,
  modified: Option[DateTime] = None,
  created: Option[DateTime] = None,
  final val kind : Symbol = 'Scala
) extends Node {
  override val mediaType: MediaType = MediaTypes.`text/html`

  def apply(ctxt: Context): Future[Array[Byte]] = Future.successful {
    // TODO: Implement ScrupaleasyNode
    Array[Byte]()
  }
}

object Node {

  lazy val Empty = MessageNode('empty, "Empty Node", "text-danger", Html("This node is completely empty."))

  implicit lazy val NodeReader = new VariantBSONDocumentReader[Node] {
    def read(doc: BSONDocument) : Node = {
      doc.getAs[BSONString]("kind") match {
        case Some(str) =>
          str.value match {
            case "Message"  => MessageNode.MessageNodeHandler.read(doc)
            case "Basic"    => BasicNode.BasicNodeHandler.read(doc)
            case "Asset"    => AssetNode.AssetNodeHandler.read(doc)
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
        case 'Basic    => BasicNode.BasicNodeHandler.write(node.asInstanceOf[BasicNode])
        case 'Asset    => AssetNode.AssetNodeHandler.write(node.asInstanceOf[AssetNode])
        case 'Link     => LinkNode.LinkNodeHandler.write(node.asInstanceOf[LinkNode])
        case 'Layout   => LayoutNode.LayoutNodeHandler.write(node.asInstanceOf[LayoutNode])
      }
    }
  }

  case class NodeDAO(db: DefaultDB) extends VariantIdentifierDAO[Node] {
    final def collectionName: String = "nodes"
    implicit val writer = new Writer(NodeWriter)
    implicit val reader = new Reader(NodeReader)

    override def indices : Traversable[Index] = super.indices ++ Seq(
      Index(key = Seq("mediaType" -> IndexType.Ascending), name = Some("mediaType")),
      Index(key = Seq("kind" -> IndexType.Ascending), name = Some("kind"))
    )
  }
}

