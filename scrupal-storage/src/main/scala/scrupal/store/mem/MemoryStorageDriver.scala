package scrupal.store.mem

import java.net.URI

import scrupal.storage.api._
import scrupal.storage.impl.CommonStorageDriver

import scala.concurrent.{Future, ExecutionContext}

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

  def open(uri : URI, create : Boolean = false)(implicit ec: ExecutionContext) : Future[Store] = Future {
    if (!isDriverFor(uri))
      toss(s"Wrong URI type for MemoryStorageDriver. Expected '$scheme' scheme but got: $uri")
    stores.get(uri) match {
      case Some(store) ⇒
        store
      case None ⇒
        if (!create)
          toss(s"Store at $uri does not exist and create was not requested")
        else {
          val result = MemoryStore(uri)
          stores.put(uri, result)
          result
        }
    }
  }

  def addStore(uri: URI)(implicit ec: ExecutionContext): Future[Store] = Future {
    stores.get(uri) match {
      case Some(store) ⇒
        toss("Store at $uri already exists")
      case None ⇒
        val result = MemoryStore(uri)
        stores.put(uri, result)
        result
    }
  }


  def makeStore(uri: URI) : Store = {
    MemoryStore(uri)
  }

  def makeReference[S <: Storable](coll : Collection[S], id : ID) : Reference[S] = {
    require(coll.isInstanceOf[MemoryCollection[S]])
    StorableReference[S](coll, id)
  }

  def makeSchema(store : Store, name : String, design : SchemaDesign) = {
    require(store.isInstanceOf[MemoryStore])
    MemorySchema(store.asInstanceOf[MemoryStore], name, design)
  }

  def makeCollection[S <: Storable](schema : Schema, name : String) : Collection[S] = {
    require(schema.isInstanceOf[MemorySchema])
    MemoryCollection[S](schema.asInstanceOf[MemorySchema], name)
  }

}
