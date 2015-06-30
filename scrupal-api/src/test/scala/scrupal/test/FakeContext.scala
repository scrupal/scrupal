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

import java.time.Instant
import org.specs2.execute.{Result, AsResult}
import org.specs2.specification.Fixture
import scrupal.api.{Site, Context, BundleType}

import scala.util.matching.Regex

/** Created by reidspencer on 11/9/14.
  */
trait FakeContext[T] extends Context with Fixture[T] {
  def name : String
  def scrupalName = scrupal.label + { if (name.isEmpty) "" else "-" + name }
  def nm(name : String) = ScrupalSpecification.next(scrupalName + "-" + name)
  def nm = ScrupalSpecification.next(scrupalName)
  def sym(name : String) = Symbol(nm(name))
  def sym = Symbol(nm)

  override val site = Some( new Site(sym("Site"))(scrupal) {
    val name = "FakeContextSite"
    val description  = "Just For Testing"
    val y = nm("localhost")
    def hostNames: Regex = ".*".r
    def modified: Option[Instant] = None
    def created: Option[Instant] = None
    val settingsTypes = BundleType.empty
  })

  def apply[R](f: (T) ⇒ R)(implicit evidence$11: AsResult[R]): Result = {
    val result = f(this.asInstanceOf[T])
    AsResult(result)
  }

}
