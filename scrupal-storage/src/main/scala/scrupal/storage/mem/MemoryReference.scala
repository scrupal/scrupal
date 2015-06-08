package scrupal.storage.mem

import scrupal.storage.api.{ ID, Collection, Reference, Storable }

import scala.concurrent.Future

/** Title Of Thing.
  *
  * Description of thing
  */
class MemoryReference[T, S <: Storable[T, S]](coll : MemoryCollection[T, S], id : ID) extends Reference[T, S](coll, id) {
  override def fetch : Future[Option[S]] = coll.fetch(id)
}
