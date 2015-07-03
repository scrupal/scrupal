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

import org.specs2.specification.core.Fragments
import play.api.Play
import play.api.test.FakeApplication

trait OneAppPerSpec extends ScrupalSpecification { self : ScrupalSpecification ⇒

  def fakeApplication : FakeApplication = {
    FakeApplication()
  }

  /** Override app if you need a FakeApplication with other than default parameters. */
  implicit lazy val application = fakeApplication

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
