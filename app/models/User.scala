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
 * The Identity of a Principal as authenticated by an email address and a password.
 * @param email
 * @param password
 * @param hasher
 * @param salt
 * @param complexity
 * @param id
 * @param module_id
 * @param label
 * @param created
 */
case class Identity(email: String, password: String, hasher: String, salt: String = Hash.salt, complexity: Long = 0,
                    id: Option[Long], module_id: Long, label: String, created: DateTime)
  extends Entity[Identity] {
  def forId(id: Long) = Identity(email, password, hasher, salt, complexity, Some(id), module_id, label, created)
}

/**
 * A Handle that a given Principal might utilize
 * @param userName
 * @param identity_id
 * @param id
 * @param module_id
 * @param label
 * @param created
 */
case class Handle(userName: String, identity_id: Long,
                  id: Option[Long], module_id: Long, label: String, created: DateTime)
  extends Entity[Handle] {
  def forId(id: Long) = Handle(userName, identity_id, Some(id), module_id, label, created)
}


trait UserComponent extends Component { self: Sketch =>

  object Identities extends EntityTable[Identity]("identities") {
    def email = column[String]("email")
    def password = column[String]("password")
    def hasher = column[String]("hasher")
    def salt = column[String]("salt")
    def complexity = column[Long]("complexity")
    def * = email ~ password ~ hasher ~ salt ~ complexity ~ id.? ~ module_id ~ label ~ created <> (Identity, Identity.unapply _)
  }

  object Handles extends EntityTable[Handle]("handles") {
    def userName = column[String]("user_name")
    def identity_id = column[Long]("identity_id")
    def * = userName ~ identity_id ~ id.? ~ module_id ~ label ~ created <> (Handle, Handle.unapply _)
  }

}
