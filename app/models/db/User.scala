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

package scrupal.models.db

import org.joda.time.DateTime
import scrupal.utils.Hash
import scala.slick.lifted.DDL

/**
 * Information about a Principal, the essential identify of a user of the system. Authentication of Principals requires
 * either one or more authentication factors. The first factor (something the Principal knows) is embodied in this object
 * via the password, hasher algorithm, salt and complexity fields. Subsequent authentication factors are dealt with in
 * separate objects. Each Principal is associated with an email address and a unique identifier (long number).
 * @param id The unique identifier of this Identity
 * @param created The timestamp when this Identity was created
 * @param password The Principal's hashed password
 * @param hasher The Hasher algorithm used
 * @param salt The salt used in generation of the principal's hashed password
 * @param complexity The complexity factor for the Hasher algorithm
 */
case class Principal(
  override val id: Option[Long],
  override val created: DateTime,
  email: String,
  password: String,
  hasher: String,
  salt: String = Hash.salt,
  complexity: Long = 0
) extends Identifiable[Principal] {
  def forId(id: Long) = Principal(Some(id), created, email, password, hasher, salt, complexity)
}

case class ProfileType(
  override val id: Option[Long],
  override val created: DateTime,
  override val label: String,
  override val description: String,
  override val modified: DateTime
) extends ModifiableThing[ProfileType] {
  def forId(id: Long) = ProfileType(Some(id), created, label, description, modified)
}

/**
 * A database component for user related information.
 */
trait UserComponent extends Component { self: Sketch =>

  import profile.simple._

  import CommonTypeMappers._

  /**
   * The table of principals which are simple identifiable objects.
   */
  object Principals extends IdentifiableTable[Principal]("identities") {
    def email = column[String]("email")
    def password = column[String]("password")
    def hasher = column[String]("hasher")
    def salt = column[String]("salt")
    def complexity = column[Long]("complexity")
    def * = id.? ~ created ~ email ~ password ~ hasher ~ salt ~ complexity  <> (Principal, Principal.unapply _)
  }

  /**
   * The table of Handles by which Principals are known. This is a NamedIdentifiableTable because it identifies the
   * Principal with one or more names. A given Principal can have multiple names and a given name can identify multiple
   * Principals.
   */
  object Handles extends NamedIdentifiableTable[Principal]("handles", Principals) {
    def handles(identity: Long)(implicit s: Session) = findKeys(identity)
    def principals(handle: String)(implicit s: Session) = findValues(handle)
  }

  /**
   * The table of temporary tokens by which a user is identified.
   */
  object Tokens extends NamedIdentifiableTable[Principal]("tokens", Principals) {
    def expiration = column[DateTime]("expiration")
    def tokens(principal: Long)(implicit s: Session) = findKeys(principal)
    def principals(token: String)(implicit s: Session) = findValues(token)

    lazy val unexpiredQuery = for {
      k <- Parameters[String];
      token <- this if token.key === k && token.expiration > DateTime.now();
      p <- Principals if token.value === p.id
    } yield p.id

    def unexpired(token: String)(implicit s: Session) : List[Long] = { unexpiredQuery(token).list }
  }


  object ProfileTypes extends ModifiableThingTable[ProfileType]("profile_types") {
    def * = id.? ~ created ~ label ~ description ~ modified <> (ProfileType.tupled, ProfileType.unapply _)
  }

  def userDDL : DDL = Principals.ddl ++ Handles.ddl

}
