/**********************************************************************************************************************
 * Copyright © 2014 Reactific Software, Inc.                                                                          *
 *                                                                                                                    *
 * This file is part of Scrupal, an Opinionated Web Application Framework.                                            *
 *                                                                                                                    *
 * Scrupal is free software: you can redistribute it and/or modify it under the terms                                 *
 * of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License,   *
 * or (at your option) any later version.                                                                             *
 *                                                                                                                    *
 * Scrupal is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied      *
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more      *
 * details.                                                                                                           *
 *                                                                                                                    *
 * You should have received a copy of the GNU General Public License along with Scrupal. If not, see either:          *
 * http://www.gnu.org/licenses or http://opensource.org/licenses/GPL-3.0.                                             *
 **********************************************************************************************************************/

package scrupal.api

import org.joda.time.DateTime
import reactivemongo.api.DefaultDB
import reactivemongo.api.indexes.{Index, IndexType}
import reactivemongo.bson._
import scrupal.db.{IdentifierDAO, Storable}

/* TODO: Generalize Instance to carry any kind of payload, using VariantDataAccessObject like Node[_]
trait Instance[P] extends Storable[Identifier] with Nameable with Describable with Modifiable with Facetable {
  def payload : P
}

case class BasicInstance(
  _id : Identifier,
  name: String,
  description: String,
  entityId: Identifier,
  payload: BSONDocument,
  facets: Map[String,Facet] = Map.empty[String,Facet],
  modified : Option[DateTime] = None,
  created : Option[DateTime] = None
) {

}
*/

/** The basic unit of storage and operation in Scrupal
  * Further description needed here.
  */
case class Instance(
  _id : Identifier,
  name: String,
  description: String,
  entityId: Identifier,
  payload: BSONDocument,
  // TODO: facets: Map[String,Facet],
  modified : Option[DateTime] = None,
  created : Option[DateTime] = None
) extends Storable[Identifier] with Nameable with Describable with Modifiable {
}

object Instance {
  import BSONHandlers._

  /** Data Access Object For Instances
    * This DataAccessObject sublcass represents the "instances" collection in the database and permits management of
    * that collection as well as conversion to and from BSON format.
    * @param db A parameterless function returning a [[reactivemongo.api.DefaultDB]] instance.
    */
  case class InstanceDAO(db: DefaultDB) extends IdentifierDAO[Instance] {
    final def collectionName = "instances"
    implicit val reader : IdentifierDAO[Instance]#Reader = Macros.reader[Instance]
    implicit val writer : IdentifierDAO[Instance]#Writer = Macros.writer[Instance]
    override def indices : Traversable[Index] = super.indices ++ Seq(
      Index(key = Seq("entity" -> IndexType.Ascending), name = Some("Entity"))
    )
  }

}
