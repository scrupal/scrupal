package scrupal.storage.mem

import java.net.URI

import play.api.libs.json._

import scrupal.storage.api._
import scrupal.test.ScrupalSpecification

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

case class DingBot(id : Long, ding : String, bot : Long) extends Storable[DingBot]

object DingBotTransformer extends JsonTransformer[DingBot] {
  private val writer = Json.writes[DingBot]
  private val reader = Json.reads[DingBot]
  def write(s : DingBot) : JsValue = writer.writes(s)
  def intermediate_read(v : JsValue) : JsResult[DingBot] = reader.reads(v)
}

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
      val driver = StorageDriver.apply(uri)
      driver.scheme must beEqualTo("scrupal-mem")
    }

    "provide access to storage" in {
      MemoryStorageDriver.name must beEqualTo("Memory")
      val uri = new URI("scrupal-mem://localhost/access_to_storage")
      val driver = StorageDriver.apply(uri)
      driver.open(uri, create = true) match {
        case Some(store) ⇒
          val schema = store.addSchema(driver.makeSchema(store, "dingbotsSchema", DingBotsSchema))
          val coll = driver.makeCollection[DingBot](schema, "dingbots")
          val result = coll.insert(new DingBot(1, "ping", 42)) map { wr ⇒ wr.isSuccess must beTrue }
          Await.result(result, 3.seconds)
          success
        case None ⇒
          failure("no storage")
      }
    }

    "create a context to access storage" in {
      MemoryStorageDriver.name must beEqualTo("Memory")
      val uri = new URI("scrupal-mem://localhost/access_via_context")
      uri.getScheme must beEqualTo ("scrupal-mem")
      val driver = StorageDriver.apply(uri)
      val context = driver.makeContext('test)
      context.withStore(uri, create = true) { store ⇒
        val schema = store.addSchema(driver.makeSchema(store, "dingbotsSchema", DingBotsSchema))
        val coll = driver.makeCollection[DingBot](schema, "dingbots")
        val result = coll.insert(new DingBot(1, "ping", 42)) map { wr ⇒ wr.isSuccess must beTrue }
        Await.result(result, 3.seconds)
      }
    }
  }

}
