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

package scrupal.api

import akka.http.scaladsl.model._
import play.api.mvc._


trait Stimulus extends Request[AnyContent] {
  implicit val context : Context
  def mediaTyp : MediaType = mediaType match {
    case Some(mt) ⇒
      MediaTypes.getForKey( mt.mediaType → mt.mediaSubType ).getOrElse(MediaTypes.`application/octet-stream`)
    case None ⇒
      MediaTypes.`application/octet-stream`
  }
}

object Stimulus {
  def apply(ctxt: Context, req : Request[AnyContent]) : Stimulus = {
    new Stimulus {
      implicit val context : Context = ctxt
      override val id = req.id
      override val tags = req.tags
      override val uri = req.uri
      override val path = req.path
      override val method = req.method
      override val version = req.version
      override val queryString = req.queryString
      override val headers = req.headers
      override lazy val remoteAddress = req.remoteAddress
      override lazy val secure = req.secure
      override val body : AnyContent = req.body
    }
  }

  lazy val empty = new Stimulus {
    val context : Context = null
    val body: AnyContent = AnyContentAsEmpty
    val secure: Boolean = false
    val uri: String = ""
    val queryString: Map[String, Seq[String]] = Map.empty[String,Seq[String]]
    val remoteAddress: String = ""
    val method: String = ""
    val headers: Headers = new Headers(Seq.empty[(String,String)])
    val path: String = ""
    val version: String = ""
    val tags: Map[String, String] = Map.empty[String,String]
    val id: Long = Long.MinValue
  }

}
