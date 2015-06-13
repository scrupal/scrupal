package scrupal.storage.filesys

import java.net.URI

import scrupal.storage.api.StorageContext

/** Title Of Thing.
  *
  * Description of thing
  */
case class FileSysStorageContext private[filesys] (id : Symbol, uri: URI, store: FileSysStore) extends StorageContext {
  val driver = FileSysStorageDriver
}
