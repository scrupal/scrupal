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

import java.io.File
import java.util.concurrent.atomic.AtomicInteger

import org.specs2.specification.core.Fragments

import play.api.Play
import play.api.mvc.Handler
import play.api.test.{ FakeApplication, PlaySpecification }

import scrupal.utils.ScrupalComponent

import scala.concurrent.duration.{ Duration, FiniteDuration }

/** Title Of Thing.
  *
  * Description of thing
  */
abstract class ScrupalSpecification(
  specName : String, timeout : FiniteDuration = Duration(5, "seconds")) extends PlaySpecification with ScrupalComponent {

  def fakeApplication(
    path : File = new File("."),
    additionalConfiguration : Map[String, _ <: Any] = Map.empty,
    withRoutes : PartialFunction[(String, String), Handler] = PartialFunction.empty) : FakeApplication = {
    FakeApplication(path, additionalConfiguration = additionalConfiguration, withRoutes = withRoutes)
  }

  // WARNING: Do NOT put anything but def and lazy val because of DelayedInit or app startup will get invoked twice
  // and you'll have a real MESS on your hands!!!! (i.e. no db interaction will work!)
}

object ScrupalSpecification {

  def next(name : String) : String = name + "-" + counter.incrementAndGet()
  val counter = new AtomicInteger(0)

}

trait OneAppPerSpec extends ScrupalSpecification { self : ScrupalSpecification ⇒
  /** Override app if you need a FakeApplication with other than default parameters. */
  implicit lazy val application = fakeApplication()

  private def startRun() = {
    Play.start(application)
  }
  protected def beforeAll() = {}

  protected def afterAll() = {}

  private def stopRun() = {
    Play.stop(application)
  }

  override def map(fs : ⇒ Fragments) = {
    step(startRun()) ^ step(beforeAll()) ^ fs ^ step(afterAll()) ^ step(stopRun())
  }
}
