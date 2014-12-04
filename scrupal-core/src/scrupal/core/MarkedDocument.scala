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

import play.twirl.api.Html
import scrupal.api._

import scala.concurrent.Future


/** Entity For Marked Documents
  *
  * This is an entity that fields requests for documents marked up in Marked.js style and generates a page
  * with that javascript library to convert the markup to HTML in the browser.
  * @param id Name of the entity and its path key
  * @param root Root of the documents as found in the classpath via the asset locator
  */
case class MarkedDocument(id: Symbol, root: String, roots: Seq[String] = Seq.empty[String]) extends Entity {

  def kind: Symbol = 'MarkedDoc

  val key = makeKey(id.name)

  def instanceType: BundleType = BundleType.Empty

  override def moduleOf = Some(CoreModule)

  def description: String = "An entity that stores nothing and merely echos its requests"

  def  menuItems(context: Context) : Map[String,Map[String,String]] = {
    val locator = context.scrupal.assetsLocator
    for (root ← roots) yield {
      locator.fetchDirectory(root) match {
        case Some(dir) ⇒
          val key = dir.name.getOrElse(root.substring(root.lastIndexOf('/')+1))
          val map = for ((k,(v1,v2)) ← dir.files) yield {
            v1 → k
          }
          key -> map
        case None ⇒ key → Map.empty[String,String]
      }
    }
  }.toMap

  var menuHtml : Option[Html] = None

  def menu(context: Context) : Html = {
    menuHtml match {
      case Some(m) ⇒ m
      case None ⇒ {
        val result = scrupal.core.views.html.pages.docNav(menuItems(context))
        menuHtml = Some(result)
        result
      }
    }
  }

  def findDocument(context: Context, id: String) : Future[Result[_]] = {
    val locator = context.scrupal.assetsLocator
    locator.fetchDirectory(root) match {
      case None ⇒ Future.successful(ErrorResult(s"Root directory at $root was not found", NotFound))
      case Some(dir) ⇒
        val doc : String = {
          if (id.nonEmpty)
            id
          else dir.index match {
            case None ⇒ return Future.successful(ErrorResult(s"Document at $root/ was not found", NotFound))
            case Some(index) ⇒ index
          }
        }
        import MarkedDocument._
        dir.files.get(doc) match {
          case None ⇒ Future.successful(ErrorResult(s"Document at $root/$doc was not found", NotFound))
          case Some((docTitle,urlOpt)) ⇒
            urlOpt match {
              case Some(url) ⇒
                val title = StringNode("docTitle", docTitle)
                val content = URLNode("docUrl", url)
                val menuNode = StaticNode("Menu", menu(context))
                val footer = HtmlNode(doc, docFooter, Map("footer" → Html(dir.copyright.getOrElse("Footer"))))
                val subordinates: Map[String, Either[NodeRef, Node]] = Map(
                  "title" → Right(title),
                  "menu" → Right(menuNode),
                  "content" → Right(content),
                  "footer" → Right(footer)
                )
                val layout = LayoutNode("Doc", subordinates, docLayout)
                layout.apply(context)
              case None ⇒ Future.successful(
                ErrorResult(s"Document at $root/$doc was not listed in directory",NotFound)
              )
            }
        }
    }

  }

  override def retrieve(context: Context, id: String): Retrieve = new Retrieve(context, id) {
    override def apply(): Future[Result[_]] = findDocument(context, id)
  }

  override def retrieveFacet(context: Context, id: String, what: Seq[String]): RetrieveFacet = {
    new RetrieveFacet(context, id, what) {
      def apply(): Future[Result[_]] = findDocument(context, id)
    }
  }
}

object MarkedDocument {
  object docLayout extends
  TwirlHtmlLayout('docLayout, "Layout for Marked Documentation", scrupal.core.views.html.pages.docPage)

  object docFooter extends
  TwirlHtmlTemplate('docFooter, "Footer for Marked Documentation", scrupal.core.views.html.docFooter)


}
