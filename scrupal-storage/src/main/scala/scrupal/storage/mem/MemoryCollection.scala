package scrupal.storage.mem

import scrupal.storage.api._

import scala.collection.mutable
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

/** A Collection Stored In Memory */
case class MemoryCollection[T, S <: Storable[T, S]](storage : MemoryStorage, name : String) extends Collection[T, S] {
  override def update(obj : S, upd : Modification) : Future[WriteResult] = update(obj.id, upd)

  override def update(id : ID, update : Modification) : Future[WriteResult] = Future {
    if (content.contains(id))
      WriteResult.success() // TODO: Implement modifications
    else
      WriteResult.failure(new Exception(s"Collection '$name' does not contain object with id #$id"))
  }

  override def insert(obj : S) : Future[WriteResult] = Future.successful[WriteResult] {
    content.put(obj.id, obj)
    WriteResult.success()
  }

  override def fetch(id : ID) : Future[Option[S]] = Future {
    content.get(id)
  }

  override def delete(obj : S) : Future[WriteResult] = delete(obj.id)

  override def delete(id : ID) : Future[WriteResult] = Future {
    if (content.contains(id)) {
      content.remove(id)
      WriteResult.success()
    } else {
      WriteResult.failure(new Exception(s"Collection '$name' does not contain object with id #$id"))
    }
  }

  override def delete(ids : Seq[ID]) : Future[Seq[WriteResult]] = {
    val futures = for (id ← ids) yield { delete(id) }
    Future sequence futures
  }

  override def find(query : Query) : Future[Seq[S]] = Future {
    // TODO: Implement MemoryCollection.find(query)
    Seq.empty[S]
  }

  override def addIndex(field : String) : Future[WriteResult] = Future {
    // TODO: Implement MemoryCollection.addIndex(field) not implemented
    WriteResult.failure(new Exception("MemoryCollection.addIndex(field) not implemented"))
  }

  override def removeIndex(field : String) : Future[WriteResult] = Future {
    // TODO: Implement MemoryCollection.removeIndex(field) not implemented
    WriteResult.failure(new Exception("MemoryCollection.removeIndex(field) not implemented"))
  }

  override def indices : Seq[Index[T, S]] = {
    // TODO: Implement MemoryCollection.indices
    Seq.empty[Index[T, S]]
  }

  override def updateWhere(query : Query, update : Modification) : Future[Seq[WriteResult]] = Future {
    // TODO: Implement MemoryCollection.updateWhere
    Seq.empty[WriteResult]
  }

  override def close() : Unit = { content.empty }

  private val content = mutable.HashMap.empty[Long, S]
}
