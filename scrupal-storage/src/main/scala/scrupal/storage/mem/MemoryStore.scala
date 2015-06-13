package scrupal.storage.mem

import java.net.URI

import scrupal.storage.api._

import scala.collection.mutable

/** Title Of Thing.
  *
  * Description of thing
  */
case class MemoryStore private[mem] (driver : StorageDriver, uri : URI) extends Store {
  require(driver == MemoryStorageDriver)

  private val _schemas = new mutable.HashMap[String, MemorySchema]

  override def close : Unit = {
    for ((name, s) ← _schemas) { s.close() }
    _schemas.clear()
  }

  /** Returns the mapping of names to Schema instances for this kind of storage */
  def schemas : Map[String, Schema] = _schemas.toMap

  def addSchema(schema : Schema) : Schema = {
    _schemas.put(schema.name, schema.asInstanceOf[MemorySchema])
    schema
  }

  def withSchema[T](schema : String)(f : Schema ⇒ T) : T = {
    _schemas.get(schema) match {
      case Some(s) ⇒ f(s.asInstanceOf[MemorySchema])
      case None    ⇒ toss(s"Schema '$schema' not found in $uri ")
    }
  }

  def withCollection[T, S <: Storable](schema : String, collection : String)(f : (Collection[S]) ⇒ T) : T = {
    _schemas.get(schema) match {
      case Some(s) ⇒ s.withCollection[T, S](collection)(f)
      case None    ⇒ toss(s"Schema '$schema' not found in $uri ")
    }
  }
}
