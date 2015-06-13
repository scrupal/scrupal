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

package scrupal.core.entities


import play.api.libs.json.JsObject
import scrupal.api.Html.ContentsArgs

import scalatags.Text.all._

import scala.concurrent.Future

import scrupal.api._
import scrupal.core.html._
import scrupal.api.types.BundleType
import scrupal.utils.OSSLicense


/** The Echo Entity
  *
  * An entity object that takes in an HTTP request and formats it into HTML content which is the response. As such,
  * it simply echos its input to its output. This can be be used for benchmarking the routing and dispatch and layout
  * parts of Scrupal since the response content is generated computationally without blocking.
  */
object EchoEntity extends Entity('Echo) {

  def kind : Symbol = 'Echo

  val key : String = "Echo"

  def instanceType : BundleType[_] = BundleType.Empty

  def author : String = "Reid Spencer"

  def copyright : String = "© 2014, 2015 Reid Spencer. All Rights Reserved."

  def license : OSSLicense = OSSLicense.GPLv3

  def description : String = "An entity that stores nothing and merely echos its requests"

  case class echo_doc(kind : String, id : String, instance : JsObject)
    extends PlainPageGenerator {
    def title = kind + " -" + id
    def description = "Echo " + kind + " Request"
    def content(context : Context, args : ContentsArgs) : Html.Contents = {
      Seq(
        h1(title),
        bson_document_panel("Instance Document", instance)(),
        display_context_table(context)
      )
    }
  }

  case class echo_request(kind : String, id : String) extends PlainPageGenerator {
    val title = kind + " - " + id
    val description = "Echo " + kind + " Request"
    def content(context : Context, args : ContentsArgs) : Html.Contents = {
      Seq(
        h1(title),
        display_context_table(context)
      )
    }
  }

  override def create(context : Context, id : String, instance : JsObject) : Create = {
    new Create(context, id, instance) {
      override def apply() : Future[Result[_]] = {
        Future.successful(HtmlResult(echo_doc("Create", id, instance).render(context)))
      }
    }
  }

  override def retrieve(context : Context, id : String) : Retrieve = {
    new Retrieve(context, id) {
      override def apply : Future[Result[_]] = {
        Future.successful(HtmlResult(echo_request("Retrieve", id).render(context)))
      }
    }
  }

  override def update(context : Context, id : String, fields : JsObject) : Update = {
    new Update(context, id, fields) {
      override def apply : Future[Result[_]] = {
        Future.successful(HtmlResult(echo_doc("Update", id, fields).render(context)))
      }
    }
  }

  override def delete(context : Context, id : String) : Delete = {
    new Delete(context, id) {
      override def apply : Future[Result[_]] = {
        Future.successful(HtmlResult(echo_request("Delete", id).render(context)))
      }
    }
  }

  override def query(context : Context, id : String, fields : JsObject) : Query = {
    new Query(context, id, fields) {
      override def apply : Future[Result[_]] = {
        Future.successful(HtmlResult(echo_doc("Query", id, fields).render(context)))
      }
    }
  }

  case class facet_doc(kind : String, what : Seq[String], instance : JsObject)
    extends PlainPageGenerator {
    val title = "Facet " + kind + " -" + what.head
    val description = "Echo Facet " + kind + " Request"
    def content(context : Context, args : ContentsArgs) : Html.Contents = {
      Seq(
        h1(title),
        h3("Facet ID: ", what.tail.mkString("/")),
        bson_document_panel("Instance Document", instance)(),
        display_context_table(context)
      )
    }
  }

  case class facet_request(kind : String, what : Seq[String])
    extends PlainPageGenerator {
    val title = "Facet " + kind + " - " + what.head
    val description = "Echo Facet " + kind + " Request"
    def content(context : Context, args : ContentsArgs) : Html.Contents = {
      Seq(
        h1(title),
        h3("Facet ID: ", what.tail.mkString("/")),
        display_context_table(context)
      )
    }
  }

  override def createFacet(context : Context, what : Seq[String], instance : JsObject) : CreateFacet = {
    new CreateFacet(context, what, instance) {
      override def apply : Future[Result[_]] = {
        Future.successful(HtmlResult(facet_doc("Create", what, instance).render(context)))
      }
    }
  }

  override def retrieveFacet(context : Context, what : Seq[String]) : RetrieveFacet = {
    new RetrieveFacet(context, what) {
      override def apply : Future[Result[_]] = {
        Future.successful(HtmlResult(facet_request("Retrieve", what).render(context)))
      }
    }
  }

  override def updateFacet(context : Context, what : Seq[String], fields : JsObject) : UpdateFacet = {
    new UpdateFacet(context, what, fields) {
      override def apply : Future[Result[_]] = {
        Future.successful(HtmlResult(facet_doc("Update", what, fields).render(context)))
      }
    }
  }

  override def deleteFacet(context : Context, what : Seq[String]) : DeleteFacet = {
    new DeleteFacet(context, what) {
      override def apply : Future[Result[_]] = {
        Future.successful(HtmlResult(facet_request("Delete", what).render(context)))
      }
    }
  }

  override def queryFacet(context : Context, what : Seq[String], args : JsObject) : QueryFacet = {
    new QueryFacet(context, what, args) {
      override def apply : Future[Result[_]] = {
        Future.successful(HtmlResult(facet_doc("Query", what, args).render(context)))
      }
    }
  }
}
