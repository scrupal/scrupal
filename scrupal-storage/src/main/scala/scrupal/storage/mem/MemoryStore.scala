package scrupal.storage.mem

import java.net.URI

import scrupal.storage.api._
import scrupal.storage.impl.CommonStore

import scala.collection.mutable

/** Title Of Thing.
  *
  * Description of thing
  */
case class MemoryStore private[mem] (driver : StorageDriver, uri : URI) extends CommonStore {
  require(driver == MemoryStorageDriver)


  def exists = true

}
