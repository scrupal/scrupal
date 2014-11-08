/**********************************************************************************************************************
 * This file is part of Scrupal a Web Application Framework.                                                          *
 *                                                                                                                    *
 * Copyright (c) 2013, Reid Spencer and viritude llc. All Rights Reserved.                                            *
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

package scrupal.core.api

import org.joda.time.DateTime
import reactivemongo.api.DefaultDB
import reactivemongo.api.indexes.{IndexType, Index}
import reactivemongo.bson.{BSONDocumentReader, BSONHandler, BSONDocumentWriter, BSONDocument}
import reactivemongo.bson.Macros._
import scrupal.db.{Storable, DataAccessObject}

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

  /** Data Access Object For Instances
    * This DataAccessObject sublcass represents the "instances" collection in the database and permits management of
    * that collection as well as conversion to and from BSON format.
    * @param db A parameterless function returning a [[reactivemongo.api.DefaultDB]] instance.
    */
  case class InstanceDao(db: DefaultDB) extends DataAccessObject[Instance,Symbol](db, "instances") {
    implicit val modelHandler  : BSONDocumentReader[Instance]
      with BSONDocumentWriter[Instance]
      with BSONHandler[BSONDocument,Instance]
        = handler[Instance]

    implicit val idHandler = (id: Symbol) ⇒ reactivemongo.bson.BSONString(id.name)

    override def indices : Traversable[Index] = super.indices ++ Seq(
      Index(key = Seq("entity" -> IndexType.Ascending), name = Some("Entity"))
    )
  }

}
