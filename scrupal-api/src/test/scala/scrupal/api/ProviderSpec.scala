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

import org.specs2.mutable.Specification
import play.api.mvc.RequestHeader

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

class ProviderSpec extends Specification {

  case object NullReactor extends Reactor {
    val name = "Null"
    val description = "The Null Reactor"
    def apply(request: Stimulus) : Future[Response] = Future.successful { NoopResponse }
  }
  case class SimpleProvider(id : Symbol) extends Provider {
    def provide : ReactionRoutes = {
      case rh: RequestHeader ⇒ NullReactor
    }
  }

  val provider1 = SimpleProvider('One)
  val provider2 = SimpleProvider('Two)

  "DelegatingProvider" should {
    "delegate" in {
      val dp = new DelegatingProvider {
        def id: Identifier = 'DelegatingProvider
        def delegates: Iterable[Provider] = Seq(provider1, provider2)
      }
      val req : RequestHeader = null
      val maybe_reaction = dp.provide.lift(req)
      maybe_reaction.isDefined must beTrue
      val reaction = maybe_reaction.get
      reaction must beEqualTo(NullReactor)
      val future = reaction(Stimulus.empty).map { resp ⇒ resp.disposition must beEqualTo(Unimplemented) }
      Await.result(future, 1.seconds)
    }
  }
}
