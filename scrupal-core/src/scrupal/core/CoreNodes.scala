package scrupal.core

import java.io.File
import java.net.URL

import org.joda.time.DateTime
import play.twirl.api.Html
import reactivemongo.api.{FailoverStrategy, DefaultDB}
import reactivemongo.api.indexes.{IndexType, Index}
import reactivemongo.bson._
import resource._
import scrupal.core.api._
import scrupal.db.{VariantIdentifierDAO, DefaultFailoverStrategy, VariantDataAccessObject}
import spray.http.{MediaTypes, MediaType}

import scala.concurrent.Future

/** Message Node
  *
  * This is a very simple node that simply renders a standard Boostrap message. It is used for generating error messages
  * during node substitution so the result is visible to the end user.
  */
case class MessageNode(
  val _id: Identifier,
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
  _id: Identifier,
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
  _id: Identifier,
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
  _id: Identifier,
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
  _id: Identifier,
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

object Node {

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

  case class NodeDao(db: DefaultDB) extends VariantIdentifierDAO[Node] {
    final def collectionName: String = "nodes"
    implicit val writer = new Writer(NodeWriter)
    implicit val reader = new Reader(NodeReader)

    override def indices : Traversable[Index] = super.indices ++ Seq(
      Index(key = Seq("mediaType" -> IndexType.Ascending), name = Some("mediaType")),
      Index(key = Seq("kind" -> IndexType.Ascending), name = Some("kind"))
    )
  }
}

