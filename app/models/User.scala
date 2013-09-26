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

package scrupal.models

import org.joda.time.DateTime
import scrupal.models.db._
import scrupal.utils.Hash

/**
 * Information about a principal as authenticated by an email address and a password.
 * @param id The unique identifier of this Identity
 * @param created The timestamp when this Identity was created
 * @param password The Principal's hashed password
 * @param hasher The Hasher algorithm used
 * @param salt The salt used in generation of the principal's hashed password
 * @param complexity The complexity factor for the Hasher algorithm
 */
case class Principal(id: Option[Long], created: DateTime,
                    email: String, password: String, hasher: String, salt: String = Hash.salt, complexity: Long = 0)
  extends Identifiable[Principal] {
  def forId(id: Long) = Principal(Some(id), created, email, password, hasher, salt, complexity)
}

/**
 * A Handle that a given Principal might utilize
 * @param userName The user name the principal has chosen to utilize
 * @param identity_id The id (foreign key) of the principal
 * @param id The unique id of this handle
 * @param created The timestamp at which the Handle was created
 */
case class Handle(userName: String, identity_id: Long, id: Option[Long], created: DateTime)
  extends Identifiable[Handle] {
  def forId(id: Long) = Handle(userName, identity_id, Some(id),  created)
}

/**
 * A database component for user related information.
 */
trait UserComponent extends Component { self: Sketch =>

  object Principals extends IdentifiableTable[Principal]("identities") {
    def email = column[String]("email")
    def password = column[String]("password")
    def hasher = column[String]("hasher")
    def salt = column[String]("salt")
    def complexity = column[Long]("complexity")
    def * = id.? ~ created ~ email ~ password ~ hasher ~ salt ~ complexity  <> (Principal, Principal.unapply _)
  }

  object Handles extends IdentifiableTable[Handle]("handles") {
    def userName = column[String]("user_name")
    def identity_id = column[Long]("identity_id")
    def * = userName ~ identity_id ~ id.?  ~ created <> (Handle, Handle.unapply _)
  }

}
