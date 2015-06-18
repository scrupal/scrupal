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

import org.specs2.execute.{Error, Success, ResultLike, Result}
import org.specs2.matcher.MatchResult
import play.api.libs.json.Json
import scrupal.storage.impl.JsonFormatter
import scrupal.test.ScrupalSpecification

import scala.concurrent.{Future, Await}
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

case class DingBot(id : Long, ding : String, bot : Long) extends Storable

object DingBotFormatter$ extends JsonFormatter[DingBot](Json.format[DingBot])

object DingBotsSchema extends SchemaDesign {
  override def name : String = "dingbots"

  override def requiredNames : Seq[String] = Seq("dingbots")

  override def indicesFor(name : String) : Seq[Index] = Seq.empty[Index]
}

/** TestSuite Pattern For Testing Storage Implementations
  *
  * Each implementation of the Storage API should create a testing specification that inherits from this class
  * and fills in the blanks. All test must pass these tests before the implementation is considered conforming.
  * Note that in addition to implementing the missing definitions, a number of configuration files will be needed
  * as well.
  * @param name The name of the test suite being run
  */
abstract class StorageTestSuite(name: String) extends ScrupalSpecification(name) {

  def driver: StorageDriver
  def driverName : String
  def scheme: String
  def configFile: String

  def getContext(name: String, create : Boolean=true)(func : StoreContext ⇒ Future[Result]) : Result = {
    val f = Storage.fromConfigFile(configFile, name, create) flatMap { context ⇒
      func(context)
    }
    val g = f.recover { case x: Throwable ⇒
      Error(s"Unexpected exception: ${x.getClass.getSimpleName}: ${x.getMessage}", x)
    }
    Await.result(g, 2.seconds)
  }

  sequential

  s"$name" should {
    "have same name as this test" in {
      driver.name must beEqualTo(driverName)
    }

    "recognize its scheme" in {
      driver.scheme must beEqualTo(scheme)
    }

    "obtain a context to the main test database" in {
      getContext("testing") { context ⇒
        Future {
          success
        }
      }
    }

    "obtain a store from the context" in {
      getContext("testing") { context ⇒
        Future {
          context.withStore { store ⇒
            (store.name must beEqualTo(store.uri.getPath)).toResult
          }
        }
      }
    }

    "create the DingBotsSchema" in {
      getContext("testing")  { context ⇒
        if (context.hasSchema(DingBotsSchema.name)) {
          context.dropSchema(DingBotsSchema.name) flatMap { wr ⇒
            wr.tossOnError
            if (context.hasSchema(DingBotsSchema.name))
              toss("Dropping schema failed")
            else {
              context.addSchema(DingBotsSchema) map { schema ⇒
                if (!context.hasSchema(DingBotsSchema.name))
                  toss("Adding schema failed")
                success
              }
            }
          }
        } else {
          context.addSchema(DingBotsSchema) map { schema ⇒
            if (!context.hasSchema(DingBotsSchema.name))
              toss("Adding schema failed")
            success
          }
        }
      }
    }

    "not allow duplication of a collection" in {
      getContext("testing")  { context ⇒
        val result = context.withSchema(DingBotsSchema.name) { schema ⇒
          schema.addCollection[DingBot]("dingbots") map { coll ⇒
            failure("schema.addCollection should have failed")
          } recover {
            case x: Throwable ⇒ success
          }
        }
        context.close()
        result
      }
    }

    "insert a dingbot in a collection" in {
      getContext("testing")  { context ⇒
        context.withSchema(DingBotsSchema.name) { schema ⇒
          schema.withCollection[DingBot,Future[Result]]("dingbots") { coll ⇒
            coll.insert(new DingBot(1, "ping", 42)) map { wr: WriteResult ⇒
              (wr.isSuccess must beTrue).toResult
            }
          }
        }
      }
    }
  }
}
