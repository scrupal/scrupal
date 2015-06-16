package scrupal.storage.mem

import java.net.URI

import scrupal.storage.api._
import scrupal.storage.impl.CommonStorageDriver

/** Title Of Thing.
  *
  * Description of thing
  */
object MemoryStorageDriver extends CommonStorageDriver {
  def id = 'memory
  override val name : String = "Memory"
  override val scheme : String = "scrupal-mem"
  private val authority : String = "localhost"

  override def isDriverFor(uri : URI) : Boolean = {
    super.isDriverFor(uri) && uri.getAuthority == authority && uri.getPort == -1
  }

  def makeStore(uri: URI) : Store = {
    MemoryStore(this, uri)
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

  def makeSchema(store : Store, name : String, design : SchemaDesign) = {
    require(store.isInstanceOf[MemoryStore])
    MemorySchema(store, name, design)
  }

  def makeCollection[S <: Storable](schema : Schema, name : String) : Collection[S] = {
    require(schema.isInstanceOf[MemorySchema])
    MemoryCollection[S](schema, name)
  }

}
