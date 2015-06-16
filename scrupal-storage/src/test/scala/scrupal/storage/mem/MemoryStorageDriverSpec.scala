package scrupal.storage.mem

import java.net.URI

import play.api.libs.json._

import scrupal.storage.api._
import scrupal.storage.impl.JsonFormatter
import scrupal.test.ScrupalSpecification

import scala.concurrent.Await
import scala.concurrent.duration._
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
class MemoryStorageDriverSpec extends ScrupalSpecification("MemoryStorageDriver") {

  "MemoryStorageDriver" should {
    "map an URL to a MemoryStorageDriver" in {
      MemoryStorageDriver.name must beEqualTo("Memory")
      val uri = new URI("scrupal-mem://localhost/test")
      uri.getScheme must beEqualTo ("scrupal-mem")
      StorageDriver.apply(uri) match {
        case Some(driver) ⇒
          driver.scheme must beEqualTo("scrupal-mem")
        case None ⇒
          1 must beEqualTo(0)
      }
    }

    "provide access to storage" in {
      MemoryStorageDriver.name must beEqualTo("Memory")
      val uri = new URI("scrupal-mem://localhost/access_to_storage")
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
      MemoryStorageDriver.name must beEqualTo("Memory")
      val uri = new URI("scrupal-mem://localhost/access_via_context")
      uri.getScheme must beEqualTo ("scrupal-mem")
      StorageDriver.apply(uri) match {
        case Some(driver) ⇒
          driver.scheme must beEqualTo("scrupal-mem")
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
