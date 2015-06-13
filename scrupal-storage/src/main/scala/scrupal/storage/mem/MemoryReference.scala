package scrupal.storage.mem

import scrupal.storage.api.{ ID, Reference, Storable }

import scala.concurrent.Future

/** Title Of Thing.
  *
  * Description of thing
  */
case class MemoryReference[S <: Storable](coll : MemoryCollection[S], id : ID) extends Reference[S](coll, id) {
  override def fetch : Future[Option[S]] = coll.fetch(id)
  def close() : Unit = {}
}
