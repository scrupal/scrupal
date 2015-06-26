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

  def instanceType : BundleType = BundleType.empty

  def author : String = "Reid Spencer"

  def copyright : String = "© 2014, 2015 Reid Spencer. All Rights Reserved."

  def license : OSSLicense = OSSLicense.GPLv3

  def description : String = "An entity that stores nothing and merely echos its requests"

  case class echo_doc(kind : String, id : String, details: String, instance : JsObject)
    extends PlainPageGenerator {
    def title = kind + " -" + id
    def description = "Echo " + kind + " Request"
    def content(context : Context, args : ContentsArgs) : Html.Contents = {
      Seq(
        h1(title),
        dl(dt("Description"),dd(description),dt("Details"),dd(details)),
        json_document_panel("Instance Document", instance)(),
        display_context_table(context)
      )
    }
  }

  case class echo_request(kind : String, id : String, details: String) extends PlainPageGenerator {
    val title = kind + " - " + id
    val description = "Echo " + kind + " Request"
    def content(context : Context, args : ContentsArgs) : Html.Contents = {
      Seq(
        h1(title),
        dl(dt("Description"),dd(description),dt("Details"),dd(details)),
        display_context_table(context)
      )
    }
  }

  def parseJSON(request: Request) : JsObject = {
    // TODO: Implement payload parsing in EchoEntity
    JsObject(Seq())
  }

  override def create(details: String) : CreateReactor = {
    new CreateReactor {
      def apply(request: DetailedRequest) : Future[Response] = {
        Future.successful {
          val content = parseJSON(request)
          HtmlResponse(echo_doc("Create", "<>", details, content).render(request.context))
        }
      }
    }
  }

  override def retrieve(instance_id: String, details: String) : RetrieveReactor = {
    new RetrieveReactor {
      def apply(request: DetailedRequest) : Future[Response] = {
        Future.successful(HtmlResponse(echo_request("Retrieve", instance_id, details).render(request.context)))
      }
    }
  }

  override def update(instance_id: String, details: String) : UpdateReactor = {
    new UpdateReactor {
      def apply(request : DetailedRequest) : Future[Response] = {
        Future.successful {
          val content = parseJSON(request)
          HtmlResponse(echo_doc("Update", instance_id, details, content).render(request.context))
        }
      }
    }
  }

  override def delete(instance_id: String, details: String) : DeleteReactor = {
    new DeleteReactor {
      override def apply(request : DetailedRequest) : Future[Response] = {
        Future.successful(HtmlResponse(echo_request("Delete", instance_id, details).render(request.context)))
      }
    }
  }

  override def query(details: String) : QueryReactor = {
    new QueryReactor {
      override def apply(request : DetailedRequest) : Future[Response] = {
        Future.successful(HtmlResponse(echo_request("Query", "<>", details).render(request.context)))
      }
    }
  }

  case class facet_doc(kind : String, instance_id: String, facet: String, facet_id: String, details: String, instance : JsObject)
    extends PlainPageGenerator {
    val title = "Facet " + kind + " -" + facet
    val description = "Echo Facet " + kind + " Request"
    def content(context : Context, args : ContentsArgs) : Html.Contents = {
      Seq(
        h1(title),
        dl(
          dt("Description"), dd(description),
          dt("Instance ID"), dd(instance_id),
          dt("Facet ID"), dd(facet_id),
          dt("Details"), dd(details)
        ),
        json_document_panel("Instance Document", instance)(),
        display_context_table(context)
      )
    }
  }

  case class facet_request(kind : String, instance_id: String, facet: String, facet_id: String, details: String)
    extends PlainPageGenerator {
    val title = "Facet " + kind + " - " + facet
    val description = "Echo Facet " + kind + " Request"
    def content(context : Context, args : ContentsArgs) : Html.Contents = {
      Seq(
        h1(title),
        dl(
          dt("Description"), dd(description),
          dt("Instance ID"), dd(instance_id),
          dt("Facet ID"), dd(facet_id),
          dt("Details"), dd(details)
        ),
        display_context_table(context)
      )
    }
  }

  override def add(id: String, facet: String, rest: String) : AddReactor = {
    new AddReactor {
      override def apply(request : DetailedRequest) : Future[Response] = {
        Future.successful{
          val content = parseJSON(request)
          HtmlResponse(facet_doc("Create", id, facet, "<>", rest, content).render(request.context))
        }
      }
    }
  }

  override def get(id: String, facet: String, facet_id: String, details: String) : GetReactor = {
    new GetReactor {
      override def apply(request : DetailedRequest) : Future[Response] = {
        Future.successful(HtmlResponse(facet_request("Retrieve", id, facet, facet_id, details).render(request.context)))
      }
    }
  }

  override def set(id: String, facet: String, facet_id: String, details: String) : SetReactor = {
    new SetReactor {
      override def apply(request : DetailedRequest) : Future[Response] = {
        Future.successful {
          val content = parseJSON(request)
          HtmlResponse(facet_doc("Update", id, facet, facet_id, details, content).render(request.context))
        }
      }
    }
  }

  override def remove(id: String, facet: String, facet_id: String, details: String) : RemoveReactor = {
    new RemoveReactor {
      override def apply(request : DetailedRequest) : Future[Response] = {
        Future.successful(HtmlResponse(facet_request("Delete", id, facet, facet_id, details).render(request.context)))
      }
    }
  }

  override def find(id: String, facet: String, details: String) : FindReactor = {
    new FindReactor {
      override def apply(request : DetailedRequest) : Future[Response] = {
        Future.successful(HtmlResponse(facet_request("Query", id, facet, "<>", details).render(request.context)))
      }
    }
  }
}
