/**********************************************************************************************************************
 * Copyright © 2014 Reactific Software, Inc.                                                                          *
 *                                                                                                                    *
 * This file is part of Scrupal, an Opinionated Web Application Framework.                                            *
 *                                                                                                                    *
 * Scrupal is free software: you can redistribute it and/or modify it under the terms                                 *
 * of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License,   *
 * or (at your option) any later version.                                                                             *
 *                                                                                                                    *
 * Scrupal is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied      *
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more      *
 * details.                                                                                                           *
 *                                                                                                                    *
 * You should have received a copy of the GNU General Public License along with Scrupal. If not, see either:          *
 * http://www.gnu.org/licenses or http://opensource.org/licenses/GPL-3.0.                                             *
 **********************************************************************************************************************/

package scrupal.http

import scrupal.core.api.{Result, TextResult, HTMLResult}
import spray.http.{HttpCharsets, MediaTypes, ContentType}
import spray.httpx.marshalling.{ToResponseMarshaller, BasicMarshallers}

trait ScrupalMarshallers extends BasicMarshallers{

  val html_ct = ContentType(MediaTypes.`text/html`,HttpCharsets.`UTF-8`)
  val text_ct = ContentType(MediaTypes.`text/plain`,HttpCharsets.`UTF-8`)

  def html_marshaller : ToResponseMarshaller[HTMLResult] = {
    ToResponseMarshaller.delegate[HTMLResult,String](html_ct) { h ⇒ h.payload.body }
  }

  def text_marshaller : ToResponseMarshaller[TextResult] = {
    ToResponseMarshaller.delegate[TextResult,String](text_ct) { h ⇒ h.payload }
  }

  implicit val mystery_marshaller: ToResponseMarshaller[Result[_]] = {
    ToResponseMarshaller.delegate[Result[_], String](text_ct, html_ct) { (r : Result[_], ct) ⇒
      r match {
        case h: HTMLResult ⇒ h.payload.body
        case t: TextResult ⇒ t.payload
      }
    }
  }


}
