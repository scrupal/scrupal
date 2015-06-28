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

package scrupal.core.controllers

import play.api.mvc._
import play.api.mvc.{Request⇒PRequest}
import scrupal.api._
import scrupal.core.http.HttpUtils

import scala.concurrent.{Future, ExecutionContext}

/** Title Of Thing.
  *
  * Description of thing
  */
abstract class AbstractController(site : Site)(implicit scrupal : Scrupal) extends Controller {

  protected def cr2a(func: (Stimulus) => Option[Reactor]): Action[_] = {
    Action.async(BodyParsers.parse.anyContent) { req: PRequest[AnyContent] ⇒
      import HttpUtils._
      val context = Context(scrupal, site)
      val details: Stimulus = Stimulus(context, req)
      func(details) match {
        case Some (reactor) ⇒
          context.withExecutionContext { implicit ec: ExecutionContext ⇒
            reactor(details) map { response ⇒
              val d = response.disposition
              val status = d.toStatusCode.intValue()
              val msg = Some(s"HTTP($status): ${d.id.name}(${d.code}): ${d.msg}")
              val header = ResponseHeader(status, reasonPhrase = msg)
              Result(header, response.toEnumerator)
            }
          }
        case None ⇒
          Future.successful { NotFound("") }
      }
    }
  }

}
