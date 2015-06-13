package scrupal.storage.mem

import java.net.URI

import scrupal.storage.api.{StorageContext}

/** Title Of Thing.
  *
  * Description of thing
  */
case class MemoryStorageContext private[mem] (id : Symbol, uri: URI, store: MemoryStore) extends StorageContext {
  val driver = MemoryStorageDriver
}
