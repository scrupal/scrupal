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

package scrupal.storage.api

import java.util.concurrent.atomic.AtomicInteger

import com.typesafe.config.ConfigFactory
import scrupal.utils.ScrupalComponent

import scala.concurrent.{ExecutionContext, Await, Future}
import scala.util.{Failure, Success, Try}
import scala.concurrent.duration._

/** Title Of Thing.
  *
  * Description of thing
  */
object Storage extends ScrupalComponent {

  private case class State(driver : StorageDriver, counter : AtomicInteger = new AtomicInteger(1))
  private var state : Option[State] = None


  def numberOfStartups : Int = {
    state match {
      case None    ⇒ 0
      case Some(s) ⇒ s.counter.get()
    }
  }

  def isStartedUp : Boolean = {
    state match {
      case None    ⇒ false
      case Some(s) ⇒ s.driver.isOpen
    }
  }

  def startup() : Unit = Try {
    state match {
      case Some(s) ⇒
        val startCount = s.counter.incrementAndGet()
        log.debug("Storage initialized " + startCount + " times.")
      case None ⇒
        val full_config = ConfigFactory.load()
        StorageDriver(full_config) match {
          case Some(driver: StorageDriver) ⇒
            val s = State(driver)
            state = Some(State(driver))
          case None ⇒
            toss(s"Unable to locate default storage to open from configuration")
        }
    }
  } match {
    case Success(x) ⇒ log.debug("Successful mongoDB startup.")
    case Failure(x) ⇒ log.error("Failed to start up mongoDB", x)
  }

  def shutdown(implicit ec: ExecutionContext) : Unit = Try {
    state match {
      case Some(s) ⇒
        s.counter.decrementAndGet() match {
          case 0 ⇒
            Try {
              for (ctxt ← StorageContext.values) {
                ctxt.close()
                ctxt.unregister()
              }
            } match {
              case Success(x) ⇒ log.debug("Successfully closed StorageContexts")
              case Failure(x) ⇒ log.error("Failed to close StorageContexts: ", x)
            }
            Try {
              val futures = for (driver ← StorageDriver.values) yield {
                driver.unregister()
                driver.closeF
              }
              val waitFor = Future sequence futures
              Await.result(waitFor, 10.seconds)
            } match {
              case Success(x) ⇒ log.debug("Successfully closed StorageDrivers")
              case Failure(x) ⇒ log.error("Failed to close StorageDrivers: ", x)
            }
            state = None
          case x : Int ⇒
            log.debug("The StorageContext requires " + x + " more shutdowns before drivers shut down.")
        }
      case None ⇒
        log.debug("The MongoDB Driver has never been started up.")
    }
  } match {
    case Success(x) ⇒ log.debug("Successful DBContext shutdown.")
    case Failure(x) ⇒ log.error("Failed to shut down DBContext", x)
  }
}
