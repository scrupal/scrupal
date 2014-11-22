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
import scrupal.api._
import spray.http.MediaTypes

/** Documentation Application For Marked
  *
  * This application allows packaged documentation formatted in marked (like markdown) notation to be presented
  * within a context. This allows module vendors or site owners to easily maintain documentation in plain marked
  * format and have it added easily to their site. Since the marked documents need no processing, they should be
  * placed somewhere under the "public" directory in your module. All this application needs is a name and a
  * relative path to the top level directory of your documentation and it will do the rest.
  */
case class MarkedDocApp(
  id: Symbol,
  name: String,
  description: String,
  document_root: String,
  modified: Option[DateTime] = Some(DateTime.now()),
  created: Option[DateTime] = Some(DateTime.now())
) extends Application {
  def kind : Symbol = 'MarkedDoc

  object mdentity extends MarkedDocEntity(document_root)
  mdentity.enable(this)
  CoreModule.enable(mdentity)
}

class MarkedDocEntity(val root: String) extends Entity {

  def id: Symbol = 'Echo

  def kind: Symbol = 'Echo

  def instanceType: BundleType = BundleType.Empty

  override def moduleOf = Some(CoreModule)

  def description: String = "An entity that stores nothing and merely echos its requests"

  override def retrieve(context: ApplicationContext, id: String): Retrieve = new Retrieve(context, id) {

    override def apply(): Result[_] = {
      val locator = context.scrupal.assetsLocator
      locator.fetchDirectory(root) match {
        case None ⇒ ErrorResult(s"Root directory at $root was not found", NotFound)
        case Some(dir) ⇒
          val doc : String = {
            if (id.nonEmpty)
              id
            else dir.index match {
              case None ⇒ return ErrorResult(s"Document at $root/ was not found", NotFound)
              case Some(index) ⇒ index
            }
          }
          if (dir.files.contains(doc)) {
            locator.resourceOf(root + "/" + doc) match {
              case None ⇒ ErrorResult(s"Document at $root/$doc was not found", NotFound)
              case Some(url) ⇒
                val stream = url.openStream()
                val length = stream.available
                StreamResult(stream, MediaTypes.`text/html`)
                url.openStream()
            }
          } else {
            ErrorResult(s"Document at $root/$doc was not listed in directory", NotFound)
          }
      }
      val path = root + "/" + id
      context.scrupal.assetsLocator.fetchDirectory(path) match {
        case Some(dir) ⇒

        case None ⇒ ErrorResult(s"Document at $path was not found", NotFound)
      }
      /*        case class HtmlNode (
                description: String,
                template: TwirlHtmlTemplate,
                args: Map[String, Html],
                var enabled: Boolean = true,
                modified: Option[DateTime] = None,
                created: Option[DateTime] = None
                ) extends Node { }
      */
      HtmlResult(scrupal.core.views.html.echo.retrieve(id)(context))
    }
  }

  override def retrieveFacet(context: ApplicationContext, id: String, what: Seq[String]): RetrieveFacet = {
    new RetrieveFacet(context, id, what) {
      def apply(): Result[_] = {
        HtmlResult(scrupal.core.views.html.echo.retrieve(id)(context))
      }
    }
  }
}

