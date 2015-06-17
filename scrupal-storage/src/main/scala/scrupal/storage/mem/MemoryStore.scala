package scrupal.storage.mem

import java.net.URI
import java.time.Instant

import scrupal.storage.api._
import scrupal.storage.impl.CommonStore

import scala.concurrent.{ExecutionContext, Future}

/** A Data Store In Memory
  *
  * @param uri The uri of the storage ( scrupal-mem://localhost/{name} )
  */
case class MemoryStore private[mem] (uri : URI) extends CommonStore {
  def driver : StorageDriver = MemoryStorageDriver
  def created: Instant = Instant.now

  /** Create a new collection for storing objects */
  def addSchema(design: SchemaDesign)(implicit ec: ExecutionContext) : Future[Schema] = Future {
    val schema = MemorySchema(this, design.name, design)
    _schemas.put(design.name, schema)
    design.construct(schema)
    schema
  }


  override def dropSchema(name: String)(implicit ec: ExecutionContext) : Future[WriteResult] = {
    super.dropSchema(name)
  }

}
