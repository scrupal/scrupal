package scrupal.storage.filesys

import scrupal.storage.api.StorageContext

/** Title Of Thing.
  *
  * Description of thing
  */
case class FileSysStorageContext private[filesys] (id : Symbol) extends StorageContext {
  val driver = FileSysStorageDriver
}
