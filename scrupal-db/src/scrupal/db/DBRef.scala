/**********************************************************************************************************************
 * Copyright © 2014 Reactific Software LLC                                                                            *
 *                                                                                                                    *
 * This file is part of Scrupal, an Opinionated Web Application Framework.                                            *
 *                                                                                                                    *
 * Scrupal is free software: you can redistribute it and/or modify it under the terms                                 *
 * of the GNU General Public License as published by the Free Software Foundation,                                    *
 * either version 3 of the License, or (at your option) any later version.                                            *
 *                                                                                                                    *
 * Scrupal is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;                               *
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                          *
 * See the GNU General Public License for more details.                                                               *
 *                                                                                                                    *
 * You should have received a copy of the GNU General Public License along with Scrupal.                              *
 * If not, see either: http://www.gnu.org/licenses or http://opensource.org/licenses/GPL-3.0.                         *
 **********************************************************************************************************************/

package scrupal.db

import reactivemongo.bson._

/** Database Reference
  *
  * A little thing that Stephane Godbillion wrote about on the reactivemongo google group. It allows us to
  * store references to other documents in a database system, even across databases. For some reason this didn't
  * seem to make it into reactivemongo itself.
  */
case class DBRef (
  collection: String,
  id: BSONObjectID,
  db: Option[String] = None
)

object DBRef {

  implicit object DBRefReader extends BSONDocumentReader[DBRef] {
    def read(bson: BSONDocument) =
      DBRef(
        bson.getAs[String]("$ref").get,
        bson.getAs[BSONObjectID]("$id").get,
        bson.getAs[String]("$db"))
  }

  implicit object DBRefWriter extends BSONDocumentWriter[DBRef] {
    def write(ref: DBRef) =
      BSONDocument(
        "$ref" -> ref.collection,
        "$id" -> ref.id,
        "$db" -> ref.db)
  }
}
