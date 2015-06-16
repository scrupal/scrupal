package scrupal.storage.mem

import java.util.concurrent.atomic.AtomicLong

import scrupal.storage.api._
import scrupal.storage.impl.{IdentityFormat, IdentityFormatter}

import scala.collection.mutable
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

/** A Collection Stored In Memory */
case class MemoryCollection[S <: Storable] private[mem] (schema : Schema, name : String) extends Collection[S] {

  type SF = IdentityFormat[S]
  implicit val formatter = new IdentityFormatter[S]

  require(schema.isInstanceOf[MemorySchema])

  def count : Long = content.size

  override def update(obj : S, upd : Modification[S]) : Future[WriteResult] = update(obj.primaryId, upd)

  override def update(id : ID, update : Modification[S]) : Future[WriteResult] = Future {
    content.get(id) match {
      case Some(s : S @unchecked) ⇒
        val newObj = update(s)
        newObj.primaryId = s.primaryId
        content.put(id, newObj)
        WriteResult.success()
      case None ⇒
        WriteResult.error(s"Collection '$name' does not contain object with id #$id")
    }
  }

  override def insert(obj : S, update : Boolean) : Future[WriteResult] = Future.successful[WriteResult] {
    content.get(obj.primaryId) match {
      case Some(s : S @unchecked) ⇒
        if (update) {
          content.put(obj.primaryId, s)
          WriteResult.success()
        } else {
          WriteResult.error(s"Update not permitted during insert of #${obj.primaryId} in collection '$name")
        }
      case None ⇒
        content.put(obj.primaryId, obj)
        WriteResult.success()
    }
  }

  override def fetch(id : ID) : Future[Option[S]] = Future {
    content.get(id)
  }

  override def fetchAll() : Future[Iterable[S]] = Future {
    content.values
  }

  override def delete(obj : S) : Future[WriteResult] = {
    delete(obj.primaryId)
  }

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

  override def addIndex(index : Index) : Future[WriteResult] = Future {
    // TODO: Implement MemoryCollection.addIndex(field) not implemented
    WriteResult.failure(new Exception("MemoryCollection.addIndex(field) not implemented"))
  }

  override def removeIndex(index : Index) : Future[WriteResult] = Future {
    // TODO: Implement MemoryCollection.removeIndex(field) not implemented
    WriteResult.failure(new Exception("MemoryCollection.removeIndex(field) not implemented"))
  }

  override def indexOf(field : Seq[Indexable]) : Option[Index] = {
    // TODO: Implement MemoryCollection.indexOf(field)
    None
  }

  override def indices : Seq[Index] = {
    // TODO: Implement MemoryCollection.indices
    Seq.empty[Index]
  }

  override def updateWhere(query : Query, update : Modification[S]) : Future[Seq[WriteResult]] = Future {
    // TODO: Implement MemoryCollection.updateWhere
    Seq.empty[WriteResult]
  }

  override def close() : Unit = { content.empty; pids.set(0) }

  private def ensurePrimaryId(s : S) : Unit = {
    if (s.primaryId == Storable.undefined_primary_id)
      s.primaryId = pids.getAndIncrement()
  }
  private val pids = new AtomicLong(0)
  private val content = mutable.HashMap.empty[Long, S]

}
