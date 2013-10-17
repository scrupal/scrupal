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
import scala.slick.lifted.DDL

import scrupal.api._
import scrupal.utils.Hash
import scala.slick.direct.AnnotationMapper.column

/**
 * Information about a Principal, the essential identify of a user of the system. Authentication of Principals requires
 * either one or more authentication factors. The first factor (something the Principal knows) is embodied in this object
 * via the password, hasher algorithm, salt and complexity fields. Subsequent authentication factors are dealt with in
 * separate objects. Each Principal is associated with an email address and a unique identifier (long number).
 * @param email The principal's Email address
 * @param password The Principal's hashed password
 * @param hasher The Hasher algorithm used
 * @param salt The salt used in generation of the principal's hashed password
 * @param complexity The complexity factor for the Hasher algorithm
 */
case class Principal(
  email: String,
  password: String,
  hasher: String,
  salt: String = Hash.salt,
  complexity: Long = 0,
  override val created: Option[DateTime] = None,
  override val id: Option[Identifier] = None
) extends NumericCreatable  {
}

/**
 * A database component for user related information.
 */
trait UserComponent extends Component {

  import sketch.profile.simple._

  import CommonTypeMappers._

  /**
   * The table of principals which are simple identifiable objects.
   */
  object Principals extends ScrupalTable[Principal]("principals") with NumericCreatableTable[Principal]  {
    def email = column[String](nm("email"))
    def password = column[String](nm("password"))
    def hasher = column[String](nm("hasher"))
    def salt = column[String](nm("salt"))
    def complexity = column[Long](nm("complexity"))
    def * =  email ~ password ~ hasher ~ salt ~ complexity ~ created.? ~ id.? <> (Principal.tupled,
      Principal.unapply _)
  }

  /**
   * The table of Handles by which Principals are known. This is a NamedIdentifiableTable because it identifies the
   * Principal with one or more names. A given Principal can have multiple names and a given name can identify multiple
   * Principals.
   */
  object Handles extends NamedNumericTable[Principal]("handles", Principals) {
    def handles(identity: Long) = findKeys(identity)
    def principals(handle: String) = findValues(handle)
  }

  /**
   * The table of temporary tokens by which a user is identified.
   */
  object Tokens extends NamedNumericTable[Principal]("tokens", Principals) {
    def expiration = column[DateTime](nm("expiration"))
    def tokens(principal: Long) = findKeys(principal)
    def principals(token: String) = findValues(token)

    lazy val unexpiredQuery = for {
      k <- Parameters[String];
      token <- this if token.key === k && token.expiration > DateTime.now();
      p <- Principals if token.value === p.id
    } yield p.id

    def unexpired(token: String) : List[Long] = { unexpiredQuery(token).list }
  }


  def userDDL : DDL = Principals.ddl ++ Handles.ddl ++ Tokens.ddl

}
