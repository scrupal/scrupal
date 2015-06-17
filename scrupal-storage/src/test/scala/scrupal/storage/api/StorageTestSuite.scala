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

import java.net.URI

import org.specs2.execute.Result
import play.api.libs.json.Json
import scrupal.storage.impl.JsonFormatter
import scrupal.test.ScrupalSpecification

import scala.concurrent.{Future, Await}
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

case class DingBot(id : Long, ding : String, bot : Long) extends Storable

object DingBotFormatter$ extends JsonFormatter[DingBot](Json.format[DingBot])

object DingBotsSchema extends SchemaDesign {
  override def name : String = "DingBotSchemaDesign"

  override def requiredNames : Seq[String] = Seq("dingbots")

  override def indicesFor(name : String) : Seq[Index] = Seq.empty[Index]
}

/** TestSuite Pattern For Testing Storage Implementations
  *
  * Each implementation of the Storage API should create a testing specification that inherits from this class
  * and fills in the blanks. All test must pass these tests before the implementation is considered conforming.
  * Note that in addition to implementing the missing definitions, a number of configuration files will be needed
  * as well.
  * @param name
  */
abstract class StorageTestSuite(name: String) extends ScrupalSpecification(name) {

  def driver: StorageDriver
  def driverName : String
  def scheme: String
  def configDir: String

  def getContext(id: Symbol, file : String, name: String)(func : StoreContext ⇒ Future[Result]) : Result = {
    val f = Storage.fromConfigFile(id, configDir + "/" + file, name, create=true) flatMap { context ⇒ func(context) }
    Await.result(f, 2.seconds)
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
      getContext('context, "testing.conf", "testing") { context ⇒ Future { success } }
    }

    "create the DingBotsSchema" in {
      getContext('schema, "testing.conf", "testing")  { context ⇒
        if (context.hasSchema(DingBotsSchema.name)) {
          val wr = Await.result(context.dropSchema(DingBotsSchema.name), 1.second)
          Future { wr.tossOnError }
        }
        if (context.hasSchema(DingBotsSchema.name))
          Future { toss("Dropping schema failed") }
        else {
          context.addSchema(DingBotsSchema) map { schema ⇒
            if (!context.hasSchema(DingBotsSchema.name))
              toss("Adding schema failed")
            success
          }
        }
      }
    }

    "create a collection" in {
      getContext('collection, "testing.conf", "testing")  { context ⇒
        context.withSchema(DingBotsSchema.name) { schema ⇒
          schema.addCollection[DingBot]("dingbots") flatMap { coll ⇒
            coll.insert(new DingBot(1, "ping", 42)) map { wr: WriteResult ⇒
              wr.isSuccess must beTrue
            }
          }
        }
      }
    }
  }
}
