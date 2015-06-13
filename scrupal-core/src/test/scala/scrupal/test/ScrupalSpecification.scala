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

import org.specs2.specification.{ Step, Fragments }
import scrupal.core.api.{ Scrupal, Schema }
import scrupal.db.{ DBContext, DBContextSpecification }

import scala.concurrent.duration.{ Duration, FiniteDuration }

/** One line sentence description here.
  * Further description here.
  */
abstract class ScrupalSpecification(specName : String, timeout : FiniteDuration = Duration(5, "seconds"))
  extends DBContextSpecification(specName, timeout) {

  // WARNING: Do NOT put anything but def and lazy val because of DelayedInit or app startup will get invoked twice
  // and you'll have a real MESS on your hands!!!! (i.e. no db interaction will work!)

  implicit lazy val scrupal = new Scrupal(ScrupalSpecification.next(specName))

  // Handle one time startup and teardown of the DBContext
  private object Actions {
    lazy val open = { scrupal.open() }
    lazy val close = { scrupal.close() }
  }

  override def map(fs : ⇒ Fragments) = Step(Actions.open) ^ super.map(fs) ^ Step(Actions.close)

  def withSchema[T](f : Schema ⇒ T) : T = {
    withDBContext { dbContext : DBContext ⇒
      val schema : Schema = new Schema(dbContext, specName)
      schema.create(dbContext)
      f(schema)
    }
  }
}

object ScrupalSpecification {

  def next(name : String) : String = name + "-" + counter.incrementAndGet()
  val counter = new AtomicInteger(0)

}
