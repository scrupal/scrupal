package scrupal.storage.mem

import scrupal.storage.api.StorageContext

/** Title Of Thing.
  *
  * Description of thing
  */
case class MemoryStorageContext(id : Symbol) extends StorageContext {
  val driver = MemoryStorageDriver
}
