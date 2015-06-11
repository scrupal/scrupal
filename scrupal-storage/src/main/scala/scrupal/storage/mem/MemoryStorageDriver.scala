package scrupal.storage.mem

import java.net.URI

import scrupal.storage.api._
import scrupal.utils.{ TryWith, ScrupalComponent }

import scala.collection.mutable
import scala.util.{ Failure, Success, Try }

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

  override def storeExists(name : String) : Boolean = {
    stores.contains(name)
  }

  def open(url : URI, create : Boolean = false) : Option[MemoryStore] = {
    if (!canOpen(url))
      return None
    stores.get(url.getPath) match {
      case Some(s) ⇒ Some(s)
      case None ⇒
        if (create) {
          val result = new MemoryStore(this, url)
          stores.put(url.getPath, result)
          Some(result)
        } else {
          None
        }
    }
  }

  def withStore[T](uri : URI)(f : Store ⇒ T) : T = {
    stores.get(uri.getPath) match {
      case Some(s) ⇒ f(s)
      case None    ⇒ toss(s"No store found for $uri")
    }
  }

  def makeReference[S <: Storable[S]](coll : Collection[S], id : ID) : Reference[S] = {
    require(coll.isInstanceOf[MemoryCollection[S]])
    MemoryReference[S](coll.asInstanceOf[MemoryCollection[S]], id)
  }

  def makeContext(id : Symbol) : MemoryStorageContext = MemoryStorageContext(id)

  def makeStorage(uri : URI) : Store = MemoryStore(this, uri)

  def makeSchema(store : Store, name : String, design : SchemaDesign) = {
    require(store.isInstanceOf[MemoryStore])
    MemorySchema(store, name, design)
  }

  def makeCollection[S <: Storable[S]](schema : Schema, name : String) : Collection[S] = {
    require(schema.isInstanceOf[MemorySchema])
    MemoryCollection[S](schema, name)
  }

  override def close() : Unit = {
    for ((name, s) ← stores) s.close
    stores.clear()
  }

  private val stores : mutable.HashMap[String, MemoryStore] = new mutable.HashMap[String, MemoryStore]
  private val authority : String = "localhost"
}