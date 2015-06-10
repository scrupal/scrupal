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

  private val colls = new mutable.HashMap[String, Collection[_]]

  /** Returns the set of collections that this Storage instance knows about */
  def collections : Map[String, Collection[_]] = colls.toMap

  /** Find and return a Collection of a specific name */
  def collectionFor[S <: Storable[S]](name : String) : Option[Collection[S]] = {
    colls.get(name) match {
      case None ⇒ None
      case Some(result) ⇒
        result match {
          case colls : Collection[S] @unchecked ⇒ Some(result.asInstanceOf[Collection[S]])
          case _ ⇒ None
        }
    }
  }

  def makeCollection[S <: Storable[S]](name : String) : Collection[S] = {
    TryWith { new MemoryCollection[S](this, name) } { coll ⇒
      colls.put(name, coll)
      coll
    } match {
      case Success(x) ⇒ x
      case Failure(x) ⇒ throw x
    }
  }

  /** Find collections matching a specific name pattern and return a Map of them */
  def collectionsFor(namePattern : Regex) : Map[String, Collection[_]] = {
    colls.filter {
      case (name : String, coll : Collection[_]) ⇒ namePattern.findFirstIn(name).isDefined
    }
  }.toMap

  override def close : Unit = {
    for ((name, coll) ← colls) { coll.close() }
    colls.clear()
  }
}
