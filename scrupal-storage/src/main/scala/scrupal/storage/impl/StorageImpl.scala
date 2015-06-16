package scrupal.storage.impl

import java.net.{URI}

import scrupal.storage.api.{ StorageDriver, StorageContext }

/** Storage Implementation Details
  */
object StorageImpl {

  def contextForURL(id: Symbol, uri : URI) : Option[StorageContext] = {
    StorageDriver(uri).map { driver ⇒ driver.makeContext(id, uri) }
  }
}
