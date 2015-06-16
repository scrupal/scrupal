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
  private val authority : String = "localhost"
  private val stores : mutable.HashMap[URI, MemoryStore] = new mutable.HashMap[URI, MemoryStore]

  private[mem] def storeExists(uri: URI) : Boolean = {
    stores.contains(uri)
  }

  override def isDriverFor(uri : URI) : Boolean = {
    super.isDriverFor(uri) && uri.getAuthority == authority && uri.getPort == -1
  }

  override def canOpen(uri : URI) : Boolean = {
    super.canOpen(uri) && storeExists(uri)
  }

  def open(uri : URI, create : Boolean = false) : Option[MemoryStore] = {
    if (!isDriverFor(uri))
      None
    if (!storeExists(uri) && !create)
      None

    stores.get(uri) match {
      case Some(s) ⇒ Some(s)
      case None ⇒
        if (create) {
          val result = new MemoryStore(this, uri)
          stores.put(uri, result)
          Some(result)
        } else {
          None
        }
    }
  }

  def withStore[T](uri : URI, create : Boolean = false)(f : Store ⇒ T) : T = {
    stores.get(uri) match {
      case Some(store) ⇒ f(store)
      case None ⇒
        open(uri, create) match {
          case Some(store) ⇒ f(store)
          case None ⇒
            toss(s"No store found for $uri")
        }
    }
  }

  def makeReference[S <: Storable](coll : Collection[S], id : ID) : Reference[S] = {
    require(coll.isInstanceOf[MemoryCollection[S]])
    StorableReference[S](coll, id)
  }

  def makeContext(id : Symbol, uri: URI, create: Boolean = false) : StorageContext = {
    withStore(uri, create) { store: Store ⇒
      MemoryStorageContext(id, uri, store.asInstanceOf[MemoryStore])
    }
  }

  def makeStorage(uri : URI) : Store = MemoryStore(this, uri)

  def makeSchema(store : Store, name : String, design : SchemaDesign) = {
    require(store.isInstanceOf[MemoryStore])
    MemorySchema(store, name, design)
  }

  def makeCollection[S <: Storable](schema : Schema, name : String) : Collection[S] = {
    require(schema.isInstanceOf[MemorySchema])
    MemoryCollection[S](schema, name)
  }

  override def close() : Unit = {
    for ((name, s) ← stores) s.close
    stores.clear()
  }

}
