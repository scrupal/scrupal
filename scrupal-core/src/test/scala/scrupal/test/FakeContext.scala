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

package scrupal.test

import org.specs2.execute.AsResult
import org.specs2.specification.Fixture
import scrupal.core.api._
import scrupal.core.sites.NodeSite
import spray.http.{ HttpRequest, HttpMethods, HttpMethod, Uri }
import spray.routing.RequestContext

/** Created by reidspencer on 11/9/14.
  */
class FakeContext[T <: FakeContext[T]](
  name : String = "",
  path : String = "",
  method : HttpMethod = HttpMethods.GET)(implicit val scrupal : Scrupal) extends Context with Fixture[T] {
  private val scrupalName = scrupal.label + { if (name.isEmpty) "" else "-" + name }
  override val uri : Uri = Uri("http://localhost/" + path)
  val request : RequestContext = new RequestContext(new HttpRequest(method, uri), null, Uri.Path(path))
  def nm(name : String) = ScrupalSpecification.next(scrupalName + "-" + name)
  def nm = ScrupalSpecification.next(scrupalName)
  def sym(name : String) = Symbol(nm(name))
  def sym = Symbol(nm)

  override val site = Some(NodeSite(sym("Site"), "FakeContextSite", "Just For Testing", nm("localhost")))

  def apply[R : AsResult](f : T ⇒ R) = {
    val result = f(this.asInstanceOf[T])
    AsResult(result)
  }
}

object FakeContext {
  // def apply(name: String) = new FakeContext(name)
  // def fixture(name: String) = new FakeContext(name)
}
