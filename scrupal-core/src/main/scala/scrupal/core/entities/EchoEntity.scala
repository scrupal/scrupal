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

package scrupal.core.entities

import reactivemongo.bson.BSONDocument
import scrupal.core.api.Html.ContentsArgs
import scalatags.Text.all._
import scrupal.core.api._
import scrupal.core.html._
import scrupal.core.types.BundleType
import scrupal.utils.OSSLicense

import scala.concurrent.Future


/** The Echo Entity
  *
  * An entity object that takes in an HTTP request and formats it into HTML content which is the response. As such,
  * it simply echos its input to its output. This can be be used for benchmarking the routing and dispatch and layout
  * parts of Scrupal since the response content is generated computationally without blocking.
  */
object EchoEntity extends Entity('Echo) {

  def kind: Symbol = 'Echo

  val key: String = "Echo"

  def instanceType: BundleType = BundleType.Empty

  def author: String = "Reid Spencer"

  def copyright: String = "© 2014, 2015 Reid Spencer. All Rights Reserved."

  def license: OSSLicense = OSSLicense.GPLv3

  def description: String = "An entity that stores nothing and merely echos its requests"

  case class echo_doc(kind: String, id: String, instance: reactivemongo.bson.BSONDocument)
    extends PlainPageGenerator
  {
    def title = kind + " -" + id
    def description = "Echo " + kind + " Request"
    def content(context: Context, args: ContentsArgs): Html.Contents = {
      Seq(
        h1(title),
        bson_document_panel("Instance Document", instance)(),
        display_context_table(context)
      )
    }
  }

  case class echo_request(kind: String, id: String) extends PlainPageGenerator {
    val title = kind + " - " + id
    val description = "Echo " + kind + " Request"
    def content(context: Context, args: ContentsArgs) : Html.Contents = {
      Seq(
        h1(title),
        display_context_table(context)
      )
    }
  }

  override def create(context: Context, id: String, instance: BSONDocument) : Create = {
    new Create(context, id, instance) {
      override def apply() : Future[Result[_]] = {
        Future.successful( HtmlResult(echo_doc("Create", id, instance).render(context)) )
      }
    }
  }

  override def retrieve(context: Context, id: String) : Retrieve = {
    new Retrieve(context, id) {
      override def apply : Future[Result[_]] = {
        Future.successful( HtmlResult(echo_request("Retrieve", id).render(context)) )
      }
    }
  }

  override def update(context: Context, id: String, fields: BSONDocument) : Update = {
    new Update(context, id, fields) {
      override def apply : Future[Result[_]] = {
        Future.successful( HtmlResult(echo_doc("Update", id, fields).render(context)) )
      }
    }
  }

  override  def delete(context: Context, id: String) : Delete = {
    new Delete(context, id) {
      override def apply : Future[Result[_]] = {
        Future.successful( HtmlResult(echo_request("Delete", id).render(context)) )
      }
    }
  }


  override def query(context: Context, id: String, fields: BSONDocument) : Query = {
    new Query(context, id, fields) {
      override def apply : Future[Result[_]] = {
        Future.successful( HtmlResult(echo_doc("Query", id, fields).render(context)) )
      }
    }
  }

  case class facet_doc(kind: String, what: Seq[String], instance: reactivemongo.bson.BSONDocument)
    extends PlainPageGenerator
  {
    val title = "Facet " + kind + " -" + what.head
    val description = "Echo Facet " + kind + " Request"
    def content(context: Context, args: ContentsArgs): Html.Contents = {
      Seq(
        h1(title),
        h3("Facet ID: ", what.tail.mkString("/")),
        bson_document_panel("Instance Document", instance)(),
        display_context_table(context)
      )
    }
  }

  case class facet_request(kind: String, what: Seq[String])
    extends PlainPageGenerator
  {
    val title = "Facet " + kind + " - " + what.head
    val description = "Echo Facet " + kind + " Request"
    def content(context: Context, args: ContentsArgs ) : Html.Contents = {
      Seq(
        h1(title),
        h3("Facet ID: ", what.tail.mkString("/")),
        display_context_table(context)
      )
    }
  }

  override def createFacet(context: Context,what: Seq[String], instance: BSONDocument) : CreateFacet = {
    new CreateFacet(context, what, instance) {
      override def apply : Future[Result[_]] = {
        Future.successful( HtmlResult(facet_doc("Create", what, instance).render(context)) )
      }
    }
  }

  override def retrieveFacet(context: Context, what: Seq[String]) : RetrieveFacet = {
    new RetrieveFacet(context, what) {
      override def apply : Future[Result[_]] = {
        Future.successful( HtmlResult(facet_request("Retrieve", what).render(context)) )
      }
    }
  }

  override def updateFacet(context: Context, what: Seq[String], fields: BSONDocument) : UpdateFacet = {
    new UpdateFacet(context, what, fields) {
      override def apply : Future[Result[_]] = {
        Future.successful( HtmlResult(facet_doc("Update", what, fields).render(context)) )
      }
    }
  }

  override def deleteFacet(context: Context, what: Seq[String]) : DeleteFacet = {
    new DeleteFacet(context, what) {
      override def apply : Future[Result[_]] = {
        Future.successful( HtmlResult(facet_request("Delete", what).render(context)) )
      }
    }
  }

  override def queryFacet(context: Context, what: Seq[String], args: BSONDocument) : QueryFacet = {
    new QueryFacet(context, what, args) {
      override def apply : Future[Result[_]] = {
        Future.successful( HtmlResult(facet_doc("Query", what, args).render(context)) )
      }
    }
  }
}
