package scrupal.store.files

import java.net.URI

import scrupal.storage.api.StorageContext

/** Title Of Thing.
  *
  * Description of thing
  */
case class FilesStorageContext private[files] (id : Symbol, uri: URI, store: FilesStore) extends StorageContext {
  val driver = FilesStorageDriver
}
