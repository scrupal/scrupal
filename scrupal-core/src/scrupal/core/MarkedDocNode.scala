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

package scrupal.core

import org.joda.time.DateTime
import play.twirl.api.Html
import reactivemongo.bson.{BSONObjectID, Macros, BSONDocument, BSONHandler}
import scrupal.api.AssetLocator.Directory
import scrupal.api._
import scrupal.db.VariantReaderWriter
import spray.http.{MediaTypes, MediaType}

import scala.concurrent.Future

/** Marked Document Nodes
  *
  * This type of node translates a document written in marked (like markdown) format into HTMl via the marked.js
  * javascript library. It also produces a navigation system for documents located in a directory.
  * @param contextPath The URI path context in which the documentation occurs
  * @param root The root directory in the classpath resources in which to find the documentation
  * @param path The path to the specific document to show, relative to contextPath
  * @param modified Date of modification
  * @param created Date of creation
 */
case class MarkedDocNode(
  contextPath: String,
  root: String,
  path: List[String],
  modified: Option[DateTime] = Some(DateTime.now()),
  created: Option[DateTime] = Some(DateTime.now()),
  _id: BSONObjectID = BSONObjectID.generate,
  final val kind: Symbol = MarkedDocNode.kind
) extends Node {
  override def mediaType: MediaType = MediaTypes.`text/html`
  override def description: String = "A node that provides a marked document from a resource as html."

  /** Traverse the directories and generate the menu structure
    *
    * @param dirPath The url path to the directory
    * @param dir The directory to generate the menu from
    * @return A pair of maps, one for files, one for directories
    */
  def menuItems(dirPath: String, dir: Directory) : (Map[String,String], Map[String,Map[String,String]]) = {
      val fileMap = for ((filename,(title,url)) ← dir.files) yield {
        title → (dirPath + "/" + filename)
      }
      val dirMap = for ((dirName, optdir) ← dir.dirs if optdir.isDefined) yield {
        val subdirPath = dirPath + "/" + dirName
        val subfilesMap = optdir match {
          case Some(d2) ⇒
            for ((filename, (title, url)) ← d2.files) yield {
              title → (subdirPath + "/" + filename)
            }
          case None ⇒ Map.empty[String,String]
        }
        dirName → subfilesMap
      }
      fileMap → dirMap
  }

  def apply(context: Context) : Future[Result[_]] = {
    val pathStr = path.mkString("/")
    val relPath = path.dropRight(1).mkString("/")
    val page = path.takeRight(1).headOption.getOrElse("")
    val dirPath = if (path.isEmpty) root else root + "/" + relPath
    val locator = context.scrupal.assetsLocator
    val directory = locator.fetchDirectory(dirPath, recurse=true)
    directory match {
      case None ⇒ Future.successful(ErrorResult(s"Directory at $dirPath was not found", NotFound))
      case Some(dir) ⇒
        val doc : String = {
          if (page.nonEmpty)
            page
          else dir.index match {
            case None ⇒ return Future.successful(ErrorResult(s"Document at $dirPath was not found", NotFound))
            case Some(index) ⇒ index
          }
        }
        dir.files.get(doc) match {
          case None ⇒ Future.successful(ErrorResult(s"Document at $dirPath/$doc was not found", NotFound))
          case Some((docTitle,urlOpt)) ⇒
            urlOpt match {
              case Some(url) ⇒
                val title = StringNode("docTitle", docTitle + " &mdash; " + dir.title.getOrElse("Documentation"))
                val content = URLNode("docUrl", url)
                val linkPath = if (relPath.isEmpty) "/" + contextPath else "/" + contextPath + "/" + relPath
                val parentPath = path.dropRight(2).mkString("/")
                val parentDir = if (parentPath.isEmpty)
                  locator.fetchDirectory(root, recurse=false)
                else
                  locator.fetchDirectory(parentPath, recurse=false)
                val upLink = "/" + contextPath + {
                  (parentDir, parentPath) match {
                    case (Some(pd), pp) if pp.isEmpty ⇒ "/" + pd.index.getOrElse("index.md")
                    case (Some(pd), pp) ⇒ "/" + parentPath + "/" + pd.index.getOrElse("index.md")
                    case (None, pp) ⇒ "/" + parentPath + "/index.md"
                  }
                }
                val (files,dirs) = menuItems(linkPath, dir)
                val nav = scrupal.core.views.html.pages.docNav(upLink, files, dirs)
                val menuNode = StaticNode("Menu", nav)
                val footer = HtmlNode(doc, MarkedDocNode.docFooter, Map("footer" → Html(dir.copyright.getOrElse
                  ("Footer"))))
                val subordinates: Map[String, Either[NodeRef, Node]] = Map(
                  "title" → Right(title),
                  "menu" → Right(menuNode),
                  "content" → Right(content),
                  "footer" → Right(footer)
                )
                val layout = LayoutNode("Doc", subordinates, MarkedDocNode.docLayout)
                layout.apply(context)
              case None ⇒ Future.successful(
                ErrorResult(s"Document at $root/$doc was not listed in directory",NotFound)
              )
            }
        }
    }
  }
}

object MarkedDocNode {
  import scrupal.api.BSONHandlers._
  final val kind = 'MarkedDoc
  object MarkedDocNodeBRW extends VariantReaderWriter[Node,MarkedDocNode] {
    implicit val MarkedDocNodeHandler : BSONHandler[BSONDocument,MarkedDocNode] = Macros.handler[MarkedDocNode]
    override def fromDoc(doc: BSONDocument): MarkedDocNode = MarkedDocNodeHandler.read(doc)
    override def toDoc(obj: Node): BSONDocument = MarkedDocNodeHandler.write(obj.asInstanceOf[MarkedDocNode])
  }
  Node.variants.register(kind, MarkedDocNodeBRW)

  val docLayout = TwirlHtmlLayout('docLayout, "Layout for Marked Documentation", scrupal.core.views.html.pages.docPage)
  val docFooter = TwirlHtmlTemplate('docFooter, "Footer for Marked Documentation", scrupal.core.views.html.docFooter)
}
