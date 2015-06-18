/**********************************************************************************************************************
 * This file is part of Scrupal, a Scalable Reactive Content Management System.                                       *
 *                                                                                                                    *
 * Copyright © 2015 Reactific Software LLC                                                                            *
 *                                                                                                                    *
 * Licensed under the Apache License, Version 2.0 (the "License");  you may not use this file                         *
 * except in compliance with the License. You may obtain a copy of the License at                                     *
 *                                                                                                                    *
 *        http://www.apache.org/licenses/LICENSE-2.0                                                                  *
 *                                                                                                                    *
 * Unless required by applicable law or agreed to in writing, software distributed under the                          *
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,                          *
 * either express or implied. See the License for the specific language governing permissions                         *
 * and limitations under the License.                                                                                 *
 **********************************************************************************************************************/

package scrupal.store.reactivemongo

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
