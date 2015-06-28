package scrupal.store.mem

import java.time.Instant

import scrupal.storage.api._
import scrupal.storage.impl.{CommonCollection, IdentityFormat, IdentityFormatter}

import scala.collection.mutable
import scala.concurrent.{ExecutionContext, Future}

/** A Collection Stored In Memory */
case class MemoryCollection[S <: Storable] private[mem] (schema : MemorySchema, name : String) extends CommonCollection[S] {

  type SF = IdentityFormat[S]
  implicit val formatter = new IdentityFormatter[S]

  private val content = mutable.HashMap.empty[Long, S]

  def created: Instant = Instant.now()

  def drop(implicit ec: ExecutionContext): Future[WriteResult] = Future {
    content.clear()
    WriteResult.success()
  }

  def size: Long = content.size

  override def close() : Unit = { content.clear(); super.close() }

  override def update(obj : S, upd : Modification[S])(implicit ec: ExecutionContext) : Future[WriteResult] = {
    update(obj.primaryId, upd)
  }

  override def update(id : ID, update : Modification[S])(implicit ec: ExecutionContext) : Future[WriteResult] = {
    Future {
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
  }

  override def insert(obj : S, update : Boolean)(implicit ec: ExecutionContext) : Future[WriteResult] = {
    Future.successful[WriteResult] {
      content.get(obj.primaryId) match {
        case Some(s : S @unchecked) ⇒
          if (update) {
            content.put(obj.primaryId, s)
            WriteResult.success()
          } else {
            WriteResult.error(s"Update not permitted during insert of #${obj.primaryId} in collection '$name")
          }
        case None ⇒
          ensureUniquePrimaryId(obj)
          content.put(obj.primaryId, obj)
          WriteResult.success()
      }
    }
  }

  override def fetch(id : ID)(implicit ec: ExecutionContext) : Future[Option[S]] = Future {
    content.get(id)
  }

  override def fetchAll()(implicit ec: ExecutionContext) : Future[Iterable[S]] = Future {
    content.values
  }

  override def delete(obj : S)(implicit ec: ExecutionContext) : Future[WriteResult] = {
    val id = super.erasePrimaryId(obj)
    delete(id)
  }

  override def delete(id : ID)(implicit ec: ExecutionContext) : Future[WriteResult] = Future {
    if (content.contains(id)) {
      content.remove(id)
      WriteResult.success()
    } else {
      WriteResult.failure(new Exception(s"Collection '$name' does not contain object with id #$id"))
    }
  }

  override def delete(ids : Seq[ID])(implicit ec: ExecutionContext) : Future[WriteResult] = {
    WriteResult.coalesce {
      for (id ← ids) yield {
        delete(id)
      }
    }
  }

  override def find(query : Query[S])(implicit ec: ExecutionContext) : Future[Seq[S]] = Future {
    // TODO: Implement MemoryCollection.find(query)
    Seq.empty[S]
  }

  override def addIndex(index : Index)(implicit ec: ExecutionContext) : Future[WriteResult] = Future {
    // TODO: Implement MemoryCollection.addIndex(field) not implemented
    WriteResult.failure(new Exception("MemoryCollection.addIndex(field) not implemented"))
  }

  override def removeIndex(index : Index)(implicit ec: ExecutionContext) : Future[WriteResult] = Future {
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

  override def updateWhere(query : Query[S], update : Modification[S])(implicit ec: ExecutionContext)
      : Future[Seq[WriteResult]] = Future {
    // TODO: Implement MemoryCollection.updateWhere
    Seq.empty[WriteResult]
  }

  def queriesFor[T <: Queries[S]]: T = ??? // TODO: Implement MemoryCollection.queriesFor
}
