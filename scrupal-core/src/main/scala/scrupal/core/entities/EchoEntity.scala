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
import scrupal.api._
import scrupal.api.html._
import scrupal.utils.OSSLicense

import scala.concurrent.Future
import scalatags.Text.all._


/** The Echo Entity
  *
  * An entity object that takes in an HTTP stimulus and formats it into HTML content which is the response. As such,
  * it simply echos its input to its output. This can be be used for benchmarking the routing and dispatch and layout
  * parts of Scrupal since the response content is generated computationally without blocking.
  */
case class EchoEntity(override val id : Symbol = 'Echo)(implicit scrpl : Scrupal) extends Entity(id)(scrpl) {

  def instanceType : BundleType = BundleType.empty

  def author : String = "Reid Spencer"

  def copyright : String = "© 2014, 2015 Reid Spencer. All Rights Reserved."

  def license : OSSLicense = OSSLicense.GPLv3

  def description : String = "An entity that stores nothing and merely echos its stimuluss"

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

  case class echo_stimulus(kind : String, id : String, details: String) extends PlainPageGenerator {
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

  def parseJSON(stimulus: Stimulus) : JsObject = {
    // TODO: Implement payload parsing in EchoEntity
    JsObject(Seq())
  }

  override def create(details: String) : EntityCreate = {
    new EntityCreate {
      override def name = "EchoCreate"
      def apply(stimulus: Stimulus) : Future[Response] = {
        Future.successful {
          val content = parseJSON(stimulus)
          HtmlResponse(echo_doc("Create", "<>", details, content).render(stimulus.context))
        }
      }
    }
  }

  override def retrieve(instance_id: String, details: String) : EntityRetrieve = {
    new EntityRetrieve {
      override def name = "EchoRetrieve"
      def apply(stimulus: Stimulus) : Future[Response] = {
        Future.successful(HtmlResponse(echo_stimulus("Retrieve", instance_id, details).render(stimulus.context)))
      }
    }
  }

  override def info(instance_id: String, details: String) : EntityInfo = {
    new EntityInfo {
      override def name= "EchoInfo"
      def apply(stimulus: Stimulus) : Future[Response] = {
        Future.successful(HtmlResponse(echo_stimulus("Info", instance_id, details).render(stimulus.context)))
      }
    }
  }

  override def update(instance_id: String, details: String) : EntityUpdate = {
    new EntityUpdate {
      override def name = "EchoUpdate"
      def apply(stimulus : Stimulus) : Future[Response] = {
        Future.successful {
          val content = parseJSON(stimulus)
          HtmlResponse(echo_doc("Update", instance_id, details, content).render(stimulus.context))
        }
      }
    }
  }

  override def delete(instance_id: String, details: String) : EntityDelete = {
    new EntityDelete {
      override def name = "EchoDelete"
      override def apply(stimulus : Stimulus) : Future[Response] = {
        Future.successful(HtmlResponse(echo_stimulus("Delete", instance_id, details).render(stimulus.context)))
      }
    }
  }

  override def query(details: String) : EntityQuery = {
    new EntityQuery {
      override def name = "EchoQuery"
      override def apply(stimulus : Stimulus) : Future[Response] = {
        Future.successful(HtmlResponse(echo_stimulus("Query", "<>", details).render(stimulus.context)))
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

  case class facet_stimulus(kind : String, instance_id: String, facet: String, facet_id: String, details: String)
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

  override def add(id: String, facet: String, rest: String) : EntityAdd = {
    new EntityAdd {
      override def name = "EchoAdd"
      override def apply(stimulus : Stimulus) : Future[Response] = {
        Future.successful{
          val content = parseJSON(stimulus)
          HtmlResponse(facet_doc("Add", id, facet, "<>", rest, content).render(stimulus.context))
        }
      }
    }
  }

  override def get(id: String, facet: String, facet_id: String, details: String) : EntityGet = {
    new EntityGet {
      override def name = "EchoGet"
      override def apply(stimulus : Stimulus) : Future[Response] = {
        Future.successful(HtmlResponse(facet_stimulus("Get", id, facet, facet_id, details).render(stimulus.context)))
      }
    }
  }

  override def facetInfo(id: String, facet: String, facet_id: String, details: String) : EntityFacetInfo = {
    new EntityFacetInfo {
      override def name = "EchoFacetInfo"
      override def apply(stimulus : Stimulus) : Future[Response] = {
        Future.successful(HtmlResponse(facet_stimulus("FacetInfo", id, facet, facet_id, details).render(stimulus.context)))
      }
    }
  }

  override def set(id: String, facet: String, facet_id: String, details: String) : EntitySet = {
    new EntitySet {
      override def name = "EchoSet"
      override def apply(stimulus : Stimulus) : Future[Response] = {
        Future.successful {
          val content = parseJSON(stimulus)
          HtmlResponse(facet_doc("Set", id, facet, facet_id, details, content).render(stimulus.context))
        }
      }
    }
  }

  override def remove(id: String, facet: String, facet_id: String, details: String) : EntityRemove = {
    new EntityRemove {
      override def name = "EchoRemove"
      override def apply(stimulus : Stimulus) : Future[Response] = {
        Future.successful(HtmlResponse(facet_stimulus("Remove", id, facet, facet_id, details).render(stimulus.context)))
      }
    }
  }

  override def find(id: String, facet: String, details: String) : EntityFind = {
    new EntityFind {
      override def name = "EchoFind"
      override def apply(stimulus : Stimulus) : Future[Response] = {
        Future.successful(HtmlResponse(facet_stimulus("Find", id, facet, "<>", details).render(stimulus.context)))
      }
    }
  }
}
