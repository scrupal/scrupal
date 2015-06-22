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
import scrupal.utils.OSSLicense


/** The Echo Entity
  *
  * An entity object that takes in an HTTP request and formats it into HTML content which is the response. As such,
  * it simply echos its input to its output. This can be be used for benchmarking the routing and dispatch and layout
  * parts of Scrupal since the response content is generated computationally without blocking.
  */
case class EchoEntity(implicit scrpl : Scrupal) extends Entity('Echo)(scrpl) {

  def kind : Symbol = 'Echo

  val key : String = "Echo"

  def instanceType : BundleType = BundleType.empty

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
        json_document_panel("Instance Document", instance)(),
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

  def parseJSON(request: Request) : JsObject = {
    // TODO: Implement payload parsing in EchoEntity
    JsObject(Seq())
  }

  override def create(req: Request) : CreateReactor = {
    new CreateReactor {
      val request : Request = req
      def apply(request: Request) : Future[Response] = {
        val instance = parseJSON(request)
        Future.successful(HtmlResponse(echo_doc("Create", id, instance).render(request.context)))
      }
    }
  }

  override def retrieve(req: Request) : RetrieveReactor = {
    new RetrieveReactor {
      val request : Request = req
      def apply(request: Request) : Future[Response] = {
        Future.successful(HtmlResponse(echo_request("Retrieve", id).render(request.context)))
      }
    }
  }

  override def update(req: Request) : UpdateReactor = {
    new UpdateReactor {
      val request : Request = req
      def apply(request : Request) : Future[Response] = {
        Future.successful(HtmlResponse(echo_doc("Update", id, fields).render(request.context)))
      }
    }
  }

  override def delete(req: Request) : DeleteReactor = {
    new DeleteReactor {
      val request : Request = req
      override def apply(request : Request) : Future[Response] = {
        Future.successful(HtmlResponse(echo_request("Delete", id).render(request.context)))
      }
    }
  }

  override def query(req: Request) : QueryReactor = {
    new QueryReactor {
      val request : Request = req
      override def apply(request : Request) : Future[Response] = {
        Future.successful(HtmlResponse(echo_doc("Query", id, fields).render(request.context)))
      }
    }
  }

  case class facet_doc(kind : String, what : Iterable[String], instance : JsObject)
    extends PlainPageGenerator {
    val title = "Facet " + kind + " -" + what.head
    val description = "Echo Facet " + kind + " Request"
    def content(context : Context, args : ContentsArgs) : Html.Contents = {
      Seq(
        h1(title),
        h3("Facet ID: ", what.tail.mkString("/")),
        json_document_panel("Instance Document", instance)(),
        display_context_table(context)
      )
    }
  }

  case class facet_request(kind : String, what : Iterable[String])
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

  override def add(req: Request) : AddReactor = {
    new AddReactor {
      val request : Request = req
      override def apply(request : Request) : Future[Response] = {
        Future.successful(HtmlResponse(facet_doc("Create", what, instance).render(request.context)))
      }
    }
  }

  override def get(req: Request) : GetReactor = {
    new GetReactor {
      def request : Request = req
      override def apply(request : Request) : Future[Response] = {
        Future.successful(HtmlResponse(facet_request("Retrieve", what).render(request.context)))
      }
    }
  }

  override def set(req : Request) : SetReactor = {
    new SetReactor {
      val request : Request = req
      override def apply(request : Request) : Future[Response] = {
        Future.successful(HtmlResponse(facet_doc("Update", what, fields).render(request.context)))
      }
    }
  }

  override def remove(req: Request) : RemoveReactor = {
    new RemoveReactor {
      val request : Request = req
      override def apply(request : Request) : Future[Response] = {
        Future.successful(HtmlResponse(facet_request("Delete", what).render(request.context)))
      }
    }
  }

  override def find(req: Request) : FindReactor = {
    new FindReactor {
      val request : Request = req
      override def apply(request : Request) : Future[Response] = {
        Future.successful(HtmlResponse(facet_doc("Query", what, args).render(request.context)))
      }
    }
  }
}
