package scrupal.core.api

import reactivemongo.bson.{BSONString, BSONHandler}

/** Handle reading/writing Type instances to and from BSON.
  * Note that types are a little special. We write them as strings and restore them via lookup. Types are intended
  * to only ever live in memory but they can be references in the database. So when a Type is a field of some
  * class that is stored in the database, what actually gets stored is just the name of the type.
  */
class TypeHandlerForBSON[T <: Type ] extends BSONHandler[BSONString,T] {
  override def write(t: T): BSONString = BSONString(t.id.name)
  override def read(bson: BSONString): T = Type.as(Symbol(bson.value))
}


