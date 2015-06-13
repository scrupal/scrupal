/**********************************************************************************************************************
 * This file is part of Scrupal, a Scalable Reactive Web Application Framework for Content Management                 *
 *                                                                                                                    *
 * Copyright (c) 2015, Reactific Software LLC. All Rights Reserved.                                                   *
 *                                                                                                                    *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance     *
 * with the License. You may obtain a copy of the License at                                                          *
 *                                                                                                                    *
 *     http://www.apache.org/licenses/LICENSE-2.0                                                                     *
 *                                                                                                                    *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed   *
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for  *
 * the specific language governing permissions and limitations under the License.                                     *
 **********************************************************************************************************************/

package scrupal.api

import org.joda.time.DateTime
import scrupal.storage.api.Storable
import scrupal.utils.Hash

/** Information about a Principal, the essential identify of a user of the system. Authentication of Principals requires
  * either one or more authentication factors. The first factor (something the Principal knows) is embodied in this object
  * via the password, hasher algorithm, salt and complexity fields. Subsequent authentication factors are dealt with in
  * separate objects. Each Principal is associated with an email address and a unique identifier.
  * @param email The principal's Email address
  * @param password The Principal's hashed password
  * @param hasher The Hasher algorithm used
  * @param salt The salt used in generation of the principal's hashed password
  * @param complexity The complexity factor for the Hasher algorithm
  */
case class Principal(
  _id : Identifier,
  email : String,
  aliases : List[String],
  password : String,
  hasher : String,
  salt : String = Hash.salt,
  complexity : Long = 0,
  override val created : Option[DateTime] = None) extends Storable with Creatable

object Principal {
  /*
  import BSONHandlers._

  case class PrincipalDAO(db : ScrupalDB) extends IdentifierDAO[Principal] {
    final def collectionName = "principals"
    implicit val reader : IdentifierDAO[Principal]#Reader = Macros.reader[Principal]
    implicit val writer : IdentifierDAO[Principal]#Writer = Macros.writer[Principal]
  }
  */
}
