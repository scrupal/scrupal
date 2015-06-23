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


import akka.http.scaladsl.model.Uri
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
        h1(title), p(description),
        display_context_table(context)
      )
    }
  }

  def parseJSON(request: Request) : JsObject = {
    // TODO: Implement payload parsing in EchoEntity
    JsObject(Seq())
  }

  override def create(req: Request, id: String, rest_of_path: Uri.Path) : CreateReactor = {
    new CreateReactor {
      val request : Request = req
      val instance_id : String = id
      val rest = rest_of_path
      override val content = parseJSON(request)
      def apply(request: Request) : Future[Response] = {
        Future.successful(HtmlResponse(echo_doc("Create", instance_id, content).render(request.context)))
      }
    }
  }

  override def retrieve(req: Request, id: String, rest_of_path: Uri.Path) : RetrieveReactor = {
    new RetrieveReactor {
      val request : Request = req
      val instance_id : String = id
      val rest = rest_of_path
      def apply(request: Request) : Future[Response] = {
        Future.successful(HtmlResponse(echo_request("Retrieve", instance_id).render(request.context)))
      }
    }
  }

  override def update(req: Request, id: String, rest_of_path: Uri.Path) : UpdateReactor = {
    new UpdateReactor {
      val request : Request = req
      val instance_id : String = id
      val rest = rest_of_path
      override val content = parseJSON(request)
      def apply(request : Request) : Future[Response] = {
        Future.successful(HtmlResponse(echo_doc("Update", instance_id, content).render(request.context)))
      }
    }
  }

  override def delete(req: Request, id: String, rest_of_path: Uri.Path) : DeleteReactor = {
    new DeleteReactor {
      val request : Request = req
      val instance_id : String = id
      val rest = rest_of_path
      override def apply(request : Request) : Future[Response] = {
        Future.successful(HtmlResponse(echo_request("Delete", instance_id).render(request.context)))
      }
    }
  }

  override def query(req: Request, id: String, rest_of_path: Uri.Path) : QueryReactor = {
    new QueryReactor {
      val request : Request = req
      val instance_id : String = id
      val rest = rest_of_path
      override def apply(request : Request) : Future[Response] = {
        Future.successful(HtmlResponse(echo_request("Query", instance_id).render(request.context)))
      }
    }
  }

  case class facet_doc(kind : String, facet: String, what : Uri.Path, instance : JsObject)
    extends PlainPageGenerator {
    val title = "Facet " + kind + " -" + facet
    val description = "Echo Facet " + kind + " Request"
    def content(context : Context, args : ContentsArgs) : Html.Contents = {
      Seq(
        h1(title),
        h3("Facet Data: ", what.toString()),
        json_document_panel("Instance Document", instance)(),
        display_context_table(context)
      )
    }
  }

  case class facet_request(kind : String, facet: String, what : Uri.Path)
    extends PlainPageGenerator {
    val title = "Facet " + kind + " - " + facet
    val description = "Echo Facet " + kind + " Request"
    def content(context : Context, args : ContentsArgs) : Html.Contents = {
      Seq(
        h1(title),
        h3("Facet Data:", what.toString()),
        display_context_table(context)
      )
    }
  }

  override def add(req: Request, id: String, fct: String, rest_of_path: Uri.Path) : AddReactor = {
    new AddReactor {
      val request : Request = req
      val instance_id : String = id
      val facet = fct
      val rest = rest_of_path
      override val content = parseJSON(request)
      override def apply(request : Request) : Future[Response] = {
        Future.successful(HtmlResponse(facet_doc("Create", facet, rest, content).render(request.context)))
      }
    }
  }

  override def get(req: Request, id: String, fct: String, rest_of_path: Uri.Path) : GetReactor = {
    new GetReactor {
      val request : Request = req
      val instance_id : String = id
      val facet = fct
      val rest = rest_of_path
      override def apply(request : Request) : Future[Response] = {
        Future.successful(HtmlResponse(facet_request("Retrieve", facet, rest).render(request.context)))
      }
    }
  }

  override def set(req : Request, id: String, fct: String, rest_of_path: Uri.Path) : SetReactor = {
    new SetReactor {
      val request : Request = req
      val instance_id : String = id
      val facet = fct
      val rest = rest_of_path
      override val content = parseJSON(request)
      override def apply(request : Request) : Future[Response] = {
        Future.successful(HtmlResponse(facet_doc("Update", facet, rest, content).render(request.context)))
      }
    }
  }

  override def remove(req: Request, id: String, fct: String, rest_of_path: Uri.Path) : RemoveReactor = {
    new RemoveReactor {
      val request : Request = req
      val instance_id : String = id
      val facet = fct
      val rest = rest_of_path
      override def apply(request : Request) : Future[Response] = {
        Future.successful(HtmlResponse(facet_request("Delete", facet, rest).render(request.context)))
      }
    }
  }

  override def find(req: Request, id: String, fct: String, rest_of_path: Uri.Path) : FindReactor = {
    new FindReactor {
      val request : Request = req
      val instance_id : String = id
      val facet = fct
      val rest = rest_of_path
      override def apply(request : Request) : Future[Response] = {
        Future.successful(HtmlResponse(facet_request("Query", facet, rest).render(request.context)))
      }
    }
  }
}
