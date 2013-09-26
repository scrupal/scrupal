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
import play.api.Logger

trait Identifiable[T <: Identifiable[T]] extends scala.Equals {
  val id: Option[Long]
  val created: DateTime
  def forId(id: Long) : T
  override def equals(entity: Any) : Boolean = {
    entity match {
      case that : Entity[T] => {
        ( this.id.isDefined == that.id.isDefined ) && ( ! this.id.isDefined || (this.id.get == that.id.get) ) &&
        ( this.created == that.created )
      }
      case _ => false
    }
  }
}

/**
 * Every table that we define is some sort of entity in the database and every entity has certain fields, this
 * trait allows them to be mixed in
 * @tparam T The type into which the Entity is being mixed
 */
trait Entity[T <: Entity[T]] extends Identifiable[T] {
  val label : String
  val description: String
  override def equals(entity : Any) : Boolean = {
    entity match {
      case that : Entity[T] => {
        ( this.id.isDefined == that.id.isDefined ) && ( ! this.id.isDefined || (this.id.get == that.id.get) ) &&
        this.label.equals(that.label) && this.description.equals(that.description)
      }
      case _ => false
    }
  }
}

/**
 * The abstract database component.
 * This trait allows use to define database components which are simply collections of related tables and the
 * various query methods on those tables to provide access to them. Since Components contain Tables and Scrupal requires
 * all database entities to have a particular shape, that shape is enforced in the EntityTable class. Note that
 * Component extends Sketch which is mixed in to other components but resolved by the Schema class.
 */
trait Component extends Sketch { this : Sketch =>

  // So we can define the Table items and queries, we import the Slick database profile here
  import profile.simple._

  // Many of the subclasses will require the common type mappers so we import them now
  import CommonTypeMappers._

  abstract class IdentifiableTable[C <:Identifiable[C]](tableName: String) extends Table[C](schema, tableName) {
    def id = column[Long](tableName + "_id", O.PrimaryKey, O.AutoInc);

    def created = column[DateTime]("created")

    lazy val fetchByIDQuery = for { id <- Parameters[Long] ; ent <- this if ent.id === id } yield ent

    def fetch(id: Long)(implicit s: Session) : Option[C] = fetchByIDQuery(id).firstOption

    def fetch(oid: Option[Long])(implicit s: Session) : Option[C] = {
      oid match { case None => None ; case Some(id) => fetch(id) }
    }

    lazy val findSinceQuery = for {  created <- Parameters[DateTime] ; e <- this if e.created >= created } yield e

    def findSince(dt: DateTime)(implicit s: Session ) : List[C] = findSinceQuery(dt).list

    def insert(entity: C)( implicit s: Session ) : C = {
      val resulting_id = * returning id insert(entity)
      entity.forId(resulting_id)
    }

    def update(entity: C)(implicit s: Session ) : C = {
      this.filter(_.id === entity.id) update(entity)
      entity
    }

    // -- operations on rows
    def delete(id: Long)(implicit s: Session ) : Boolean =  {
      this.filter(_.id === id).delete > 0
    }

    def delete(oid: Option[Long])(implicit s: Session) : Boolean = {
      oid match { case None => false; case Some(id) => delete(id) }
    }

    lazy val findAllQuery = for (entity <- this) yield entity

    def findAll() (implicit s: Session) : List[C] = {
      findAllQuery.list
    }

    def upsert(entity: C)(implicit s: Session) : C = {
      entity.id match {
        case None => Logger.debug("Inserting: " + entity) ; insert(entity)
        case Some(id) => Logger.debug("Updating: " + entity); update(entity)
      }
    }
  }

  /**
   * The base class of all table definitions in Scrupal.
   * Most tables in the Scrupal database represent some form of entity that can be manipulated through the
   * admin interface. To ensure a certain level of consistency across all such entities, we enforce the structure
   * of the entities with this class. Every entity table should subclass from EntityTable
   * @param tableName The name of the table in the database
   * @tparam C The case class that represents rows in this table
   */
  abstract class EntityTable[C <:Entity[C]](tableName: String)  extends IdentifiableTable[C](tableName) {

    def label = column[String]("label", O.NotNull)

    def label_index = index(tableName + "_label_index", label, unique=true)

    def description = column[String]("description", O.NotNull)

    lazy val fetchByNameQuery = for { l <- Parameters[String] ; e <- this if e.label === l } yield e

    def fetch(label: String)(implicit s: Session) : Option[C] = fetchByNameQuery(label).firstOption

  }

  /**
   * The base class of all correlation tables.
   * This allows many-to-many relationships to be established by simply listing the pairs of IDs
   */
  abstract class CorrelationTable[A <:Identifiable[A], B<:Identifiable[B]](tableName: String)
  extends Table[(Long,Long)](schema, tableName) {
    def a_id = column[Long]("a_id")
    def b_id = column[Long]("b_id")
    def correlation_index = index(tableName + "_index", (a_id, b_id), unique=true)
  }
}
