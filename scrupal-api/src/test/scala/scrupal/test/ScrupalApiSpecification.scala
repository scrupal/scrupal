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

import java.util.concurrent.atomic.AtomicInteger

import scrupal.api.Scrupal
import scrupal.storage.api.Schema

import scala.concurrent.duration.{ Duration, FiniteDuration }

/** One line sentence description here.
  * Further description here.
  */
abstract class ScrupalApiSpecification(specName : String, timeout : FiniteDuration = Duration(5, "seconds"))
  extends ScrupalSpecification(specName) with OneAppPerSpec {

  // WARNING: Do NOT put anything but def and lazy val because of DelayedInit or app startup will get invoked twice
  // and you'll have a real MESS on your hands!!!! (i.e. no db interaction will work!)

  val testScrupal : Scrupal = FakeScrupal(ScrupalSpecification.next(specName))

  implicit val scrupal : Scrupal = testScrupal

  override protected def beforeAll() = {}

  override protected def afterAll() = {}


  def withSchema[T](f : Schema ⇒ T) : T =  ???
}

object ScrupalApiSpecification {

  def next(name : String) : String = name + "-" + counter.incrementAndGet()
  val counter = new AtomicInteger(0)

}
