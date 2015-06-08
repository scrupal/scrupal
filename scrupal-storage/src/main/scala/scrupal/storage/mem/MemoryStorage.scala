package scrupal.storage.mem

import java.net.URI

import scrupal.storage.api.{ StorageDriver, Collection, Storable, Storage }
import scrupal.utils.TryWith

import scala.collection.mutable
import scala.util.{ Failure, Success }
import scala.util.matching.Regex

/** Title Of Thing.
  *
  * Description of thing
  */
case class MemoryStorage(driver : StorageDriver, url : URI) extends Storage {

  private val colls = new mutable.HashMap[String, Collection[_, _]]

  /** Returns the set of collections that this Storage instance knows about */
  def collections : Map[String, Collection[_, _]] = colls.toMap

  /** Find and return a Collection of a specific name */
  def collectionFor[T, S <: Storable[T, S]](name : String) : Option[Collection[T, S]] = {
    colls.get(name) match {
      case None ⇒ None
      case Some(result) ⇒
        result match {
          case colls : Collection[T, S] @unchecked ⇒ Some(result.asInstanceOf[Collection[T, S]])
          case _ ⇒ None
        }
    }
  }

  def makeCollection[T, S <: Storable[T, S]](name : String) : Collection[T, S] = {
    TryWith { new MemoryCollection[T, S](this, name) } { coll ⇒
      colls.put(name, coll)
      coll
    } match {
      case Success(x) ⇒ x
      case Failure(x) ⇒ throw x
    }
  }

  /** Find collections matching a specific name pattern and return a Map of them */
  def collectionsFor(namePattern : Regex) : Map[String, Collection[_, _]] = {
    colls.filter {
      case (name : String, coll : Collection[_, _]) ⇒ namePattern.findFirstIn(name).isDefined
    }
  }.toMap

  override def close : Unit = {
    for ((name, coll) ← colls) { coll.close() }
    colls.clear()
  }
}
