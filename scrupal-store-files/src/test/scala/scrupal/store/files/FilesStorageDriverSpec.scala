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

package scrupal.store.files

import java.net.URI

import play.api.libs.json.Json
import scrupal.storage.api._
import scrupal.storage.impl.{StorageConfigHelper, JsonFormatter}
import scrupal.test.ScrupalSpecification

import scala.concurrent.duration._
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global

case class DingBot(id : Long, ding : String, bot : Long) extends Storable

object DingBotFormatter$ extends JsonFormatter[DingBot](Json.format[DingBot])

object DingBotsSchema extends SchemaDesign {
  override def name : String = "DingBotSchemaDesign"

  override def requiredNames : Seq[String] = Seq("dingbots")

  override def indicesFor(name : String) : Seq[Index] = Seq.empty[Index]
}


/** Title Of Thing.
  *
  * Description of thing
  */
class FilesStorageDriverSpec extends ScrupalSpecification("FilesStorageDriver") {

  sequential

  def getContext(id: Symbol, file : String, name: String) = {
    StorageContext.fromConfigFile(id,
      "scrupal-store-files/src/test/resources/storage/config/" + file, name, create=true
    )
  }

  "FilesStorageDriver" should {
    "map an URL to a FilesStorageDriver" in {
      FilesStorageDriver.name must beEqualTo("Files")
      getContext('map, "testing.conf", "testing") match {
        case Some(context) ⇒ {
          if (!context.hasSchema("DingBotSchemaDesign")) {
            context.addSchema(DingBotsSchema)
          }
          context.withSchema("DingBotSchemaDesign") { schema ⇒
            val coll = schema.addCollection[DingBot]("dingbots")
            val result = coll.insert(new DingBot(1, "ping", 42)) map { wr ⇒ wr.isSuccess must beTrue }
            Await.result(result, 3.seconds)
            success
          }
        }
        case None ⇒
          failure("no context")
      }
    }

    "provide access to storage" in {
      FilesStorageDriver.name must beEqualTo("Files")
      val uri = new URI("scrupal-files://localhost/tmp/scrupal/testing")
      StorageDriver.apply(uri) match {
        case Some(driver) ⇒
          driver.open(uri, create = true) match {
            case Some(store: Store) ⇒
              val schema = store.addSchema(DingBotsSchema)
              val coll = driver.makeCollection[DingBot](schema, "dingbots")
              val result = coll.insert(new DingBot(1, "ping", 42)) map { wr ⇒ wr.isSuccess must beTrue }
              Await.result(result, 3.seconds)
              success
            case None ⇒
              failure("no store")
          }
        case None ⇒
          failure("no driver")
      }
    }

    "create a context to access storage" in {
      FilesStorageDriver.name must beEqualTo("Files")
      val uri = new URI("scrupal-files://localhost/tmp/scrupal/testing")
      uri.getScheme must beEqualTo ("scrupal-files")
      StorageDriver.apply(uri) match {
        case Some(driver) ⇒
          driver.scheme must beEqualTo("scrupal-files")
          StorageContext('via_context, uri, create=true) match {
            case Some(context) ⇒
              if (!context.hasSchema("DingBotSchemaDesign")) {
                context.addSchema(DingBotsSchema)
              }
              context.withSchema("DingBotSchemaDesign") { schema ⇒
                val coll = schema.addCollection[DingBot]("dingbots")
                val result = coll.insert(new DingBot(1, "ping", 42)) map { wr ⇒ wr.isSuccess must beTrue }
                Await.result(result, 3.seconds)
                success
              }
            case None ⇒
              failure("no context")
          }
        case None ⇒
          failure("no driver")
      }
    }
  }
}
