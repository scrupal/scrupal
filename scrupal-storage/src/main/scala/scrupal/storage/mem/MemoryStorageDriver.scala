package scrupal.storage.mem

import java.net.URI

import scrupal.storage.api._

import scala.collection.mutable

/** Title Of Thing.
  *
  * Description of thing
  */
object MemoryStorageDriver extends StorageDriver {
  def id = 'memory
  override val name : String = "Memory"
  override val scheme : String = "scrupal-mem"
  override def canOpen(url : URI) : Boolean = {
    url.getScheme == scheme && url.getAuthority == authority && url.getPort == -1 && url.getPath.length > 0
  }

  override def storageExists(name : String) : Boolean = {
    storage.contains(name)
  }

  def open(url : URI, create : Boolean = false) : Option[Storage] = {
    if (!canOpen(url))
      return None
    storage.get(url.getPath) match {
      case Some(s) ⇒ Some(s)
      case None ⇒
        if (create) {
          val result = new MemoryStorage(this, url)
          storage.put(url.getPath, result)
          Some(result)
        } else {
          None
        }
    }
  }

  def makeReference[S <: Storable[S]](coll : Collection[S], id : ID) : Reference[S] = {
    require(coll.isInstanceOf[MemoryCollection[S]])
    new MemoryReference[S](coll.asInstanceOf[MemoryCollection[S]], id)
  }

  def makeContext(id : Symbol) : StorageContext = {
    MemoryStorageContext(id)
  }

  override def close() : Unit = {
    for ((name, s) ← storage) s.close
    storage.clear()
  }

  private val storage : mutable.HashMap[String, MemoryStorage] = new mutable.HashMap[String, MemoryStorage]
  private val authority : String = "localhost"
}
