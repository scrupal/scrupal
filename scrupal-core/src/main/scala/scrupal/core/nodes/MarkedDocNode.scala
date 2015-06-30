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

import akka.http.scaladsl.model.{MediaType, MediaTypes}
import org.joda.time.DateTime
import scrupal.api.AssetsLocator.Directory
import scrupal.api.Html.{Contents, ContentsArgs}
import scrupal.api._
import scrupal.api.html.MarkedPageGenerator

import scala.concurrent.{ExecutionContext, Future}
import scala.io.Source
import scalatags.Text.all._

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
  name : String,
  contextPath : String,
  root : String,
  path : Iterable[String],
  modified : Option[DateTime] = Some(DateTime.now()),
  created : Option[DateTime] = Some(DateTime.now()),
  final val kind : Symbol = MarkedDocNode.kind) extends Node {
  override def mediaType : MediaType = MediaTypes.`text/html`
  def description : String = "A node that provides a marked document from a resource as html."

  def apply(context: Context) : Future[Response] = {
    val pathStr = path.mkString("/")
    val relPath = path.dropRight(1).mkString("/")
    val page = path.takeRight(1).headOption.getOrElse("")
    val dirPath = if (relPath.isEmpty) root else root + "/" + relPath
    val locator = context.scrupal._assetsLocator
    val directory = locator.fetchDirectory(dirPath, recurse = true)
    directory match {
      case None ⇒ Future.successful(ErrorResponse(s"Directory at $dirPath was not found", NotFound))
      case Some(dir) ⇒
        val doc : String = {
          if (page.nonEmpty)
            page
          else dir.index match {
            case None ⇒ return Future.successful(ErrorResponse(s"Document at $dirPath was not found", NotFound))
            case Some(index) ⇒ index
          }
        }
        dir.files.get(doc) match {
          case None ⇒ Future.successful(ErrorResponse(s"Document at $dirPath/$doc was not found", NotFound))
          case Some((docTitle, urlOpt)) ⇒ context.scrupal.withExecutionContext { implicit ec : ExecutionContext ⇒
            Future {
              urlOpt match {
                case Some(url) ⇒
                  val title = docTitle + " - " + dir.title.getOrElse("Documentation")
                  val content = URLNode("docURL", "docUrl", url)
                  val linkPath = if (relPath.isEmpty) "/" + contextPath else "/" + contextPath + "/" + relPath
                  val parentPath = path.dropRight(2).mkString("/")
                  val parentDir = if (parentPath.isEmpty)
                    locator.fetchDirectory(root, recurse = false)
                  else
                    locator.fetchDirectory(parentPath, recurse = false)
                  val upLink = "/" + contextPath + {
                    (parentDir, parentPath) match {
                      case (Some(pd), pp) if pp.isEmpty ⇒ "/" + pd.index.getOrElse("index.md")
                      case (Some(pd), pp) ⇒ "/" + parentPath + "/" + pd.index.getOrElse("index.md")
                      case (None, pp) ⇒ "/" + parentPath + "/index.md"
                    }
                  }
                  val (files, dirs) = menuItems(linkPath, dir)
                  val nav = Seq(MarkedDocNode.docNav(upLink, files, dirs))
                  val footerText : String = dir.copyright.getOrElse("Footer")
                  val footer = Seq(div(cls := "footer", sub(sub(footerText))))
                  val contents = Source.fromInputStream(url.openStream()).mkString
                  val page = MarkedDocNode.DocPage(title, "Description", nav, contents, footer)
                  HtmlResponse(page.render(context), Successful)
                case None ⇒
                  ErrorResponse(s"Document at $root/$doc was not listed in directory", NotFound)
              }
            }
          }
        }
    }
  }

  /** Traverse the directories and generate the menu structure
    *
    * @param dirPath The url path to the directory
    * @param dir The directory to generate the menu from
    * @return A pair of maps, one for files, one for directories
    */
  def menuItems(dirPath : String, dir : Directory) : (Map[String, String], Map[String, Map[String, String]]) = {
    val fileMap = for ((filename, (title, url)) ← dir.files) yield {
      title → (dirPath + "/" + filename)
    }
    val dirMap = for ((dirName, optdir) ← dir.dirs if optdir.isDefined) yield {
      val subdirPath = dirPath + "/" + dirName
      val subfilesMap = optdir match {
        case Some(d2) ⇒
          for ((filename, (title, url)) ← d2.files) yield {
            title → (subdirPath + "/" + filename)
          }
        case None ⇒ Map.empty[String, String]
      }
      dirName → subfilesMap
    }
    fileMap → dirMap
  }
}

object MarkedDocNode {
  final val kind = 'MarkedDoc

  def docNav(uplink : String, files : Map[String, String], dirs : Map[String, Map[String, String]]) = {
    div(cls := "well-small col-md-2",
      div(cls := "btn-toolbar-vertical", role := "group",
        a(href := uplink, cls := "btn btn-xs btn-primary active", role := "button", "Up"), {
          for ((title, link) ← files) yield { a(href := link, cls := "btn btn-xs btn-primary", role := "button", title) }
        }.toSeq,
        {
          for ((title, map) ← dirs) yield {
            div(cls := "btn-group-xs btn-group-vertical", role := "group",
              button(`type` := "button", cls := "btn btn-xs btn-primary dropdown-toggle", data("toggle") := "dropdown",
                aria.expanded := "false", title, span(cls := "caret")),
              ul(cls := "dropdown-menu", role := "menu", {
                for ((label, link) ← map) yield { li(a(data("target") := "#", href := link, role := "button", label)) }
              }.toSeq)
            )
          }
        }.toSeq
      )
    )
  }

  case class DocPage(title : String, description : String, menu : Html.Contents, content : String, footer : Html.Contents)
    extends MarkedPageGenerator {
    override def bodyMain(context : Context, args : ContentsArgs) : Contents = {
      Seq(
        div(cls := "container",
          div(cls := "panel panel-primary",
            div(cls := "panel-heading", h1(cls := "panel-title", title)),
            div(cls := "panel-body",
              div(cls := "col-md-2", menu),
              div(cls := "col-md-10",
                div(cls := "well",
                  div(scalatags.Text.attrs.id := "marked", raw(content))
                ),
                footer
              )
            )
          )
        )
      )
    }
  }

}
