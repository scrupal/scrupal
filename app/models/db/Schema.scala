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

import scala.slick.driver.ExtendedProfile
import scala.slick.lifted.{ForeignKeyAction, DDL}
import org.joda.time.DateTime
import play.api.Logger
import scala.slick.driver._
import scala.slick.jdbc.{StaticQuery0, StaticQuery}
import scala.slick.direct.AnnotationMapper.column

/**
 * A Sketch is a simple trait that sketches out some basic things we need to know about a particular database
 * implementation. This is similar to Slick's Profiles, but we call them Sketches here because they're a related but
 * different concept. The subclasses of this trait are concrete, one for each kind of database. Every Sketch
 * has a member that is the Slick ExtendedProfile to be used, specifies  the schema name to be used
 * for the scrupal tables, knows the driver class name, can instantiate that driver, and knows how to create the
 * schema in which the Scrupal tables live. This allows administrators to keep scrupal's tables separate from other
 * modules added to scrupal, while keeping all the data in the same database
 */
trait Sketch {
  val profile : ExtendedProfile
  val driverClass : String
  val schema : Option[String] = None
  def driver = Class.forName(driverClass).newInstance()
  def makeSchema : StaticQuery0[Int] = throw new NotImplementedError("Making DB Schema for " + driverClass )
  override def toString = { "{" + driverClass + ", " + schema + "," + profile }
}


/**
 * Provides the notion of something stored in the database that is identifiable by a long integer.
 * Additionally we want to know when the record was created. This pair of values forms the base class upon which all
 * things stored in the database must derive.
 *
 * @tparam T
 */
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

trait Modifiable[T <: Modifiable[T]] extends Identifiable[T]  {
  val changed: DateTime
}

/**
 * Most tables that we define are some sort of entity in the database and every entity has certain fields, this
 * trait allows them to be mixed in to a subclass. Entities are identifiable, have a label, a description, and a last
 * modified time.
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

trait ModifiableEntity[T <: ModifiableEntity[T]] extends Entity[T] with Modifiable[T]  ;

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

  abstract class ModifiableEntityTable[C <: ModifiableEntity[C]](tableName: String) extends EntityTable[C](tableName) {

    def changed = column[DateTime]("changed", O.NotNull)

    def changed_index = index(tableName + "_changed_index", changed, unique=false)

    lazy val fetchByChangedQuery = for { chg <- Parameters[DateTime]; me <- this if me.changed >= chg } yield me

  }

  /**
   * The base class of all correlation tables.
   * This allows many-to-many relationships to be established by simply listing the pairs of IDs
   */
  abstract class ManyToManyTable[A <: Identifiable[A], B <: Identifiable[B] ] (
      nameA: String, nameB: String, tableA: IdentifiableTable[A], tableB:  IdentifiableTable[B]) extends Table[(Long,Long)](schema, nameA + "_" + nameB) {
    def a_id = column[Long](nameA + "_id")
    def b_id = column[Long](nameB + "_id")
    def a_fkey = foreignKey(nameA + "_fkey", a_id, tableA)(_.id, onDelete = ForeignKeyAction.Cascade )
    def b_fkey = foreignKey(nameB + "_fkey", b_id, tableB)(_.id, onDelete = ForeignKeyAction.Cascade )
    def selectAssociatedA(b: Long) = for ( c <- this if c.b_id === b ) yield c.a_id
    def selectAssociatedB(a: Long) = for ( c <- this if c.a_id === a ) yield c.b_id
    def * = a_id ~ b_id
  }
}

/**
 * Abstract Database Schema.
 * A Schema is a collection of Components. Each module or the Scrupal Core should only define on Schema each.
 * Each Schema should extend this Schema class and then mixin all the components involved in the schema. This class
 * provides a little bit of magic. It extends Sketch, which endows it with the "profile" member which is an instance
 * of the Slick EmbeddedProfile class. This provides access to the kind of database chosen to represent the data.
 * However, we don't know which database that is at compile time so this class also takes a Sketch constructor
 * argument that it uses to populate its Sketch members such as profile, schema and driverClass. In this way we
 * can abstractly provide access to all the things needed to access the database, without actually locking in on
 * any particular database, schema name or DB driver.
 * @param sketch
 */
abstract class Schema(val sketch: Sketch ) extends Sketch
{
  // The primary reason for this class to exist is to give concrete values to ScrupalSketch's members based on
  // an instance not inheritance. ScrupalSketch is abstract and has no concrete members. We want to have those members
  // be filled out from
  override val profile: ExtendedProfile = sketch.profile
  override val schema: Option[String] = sketch.schema
  override val driverClass : String = sketch.driverClass

  // This is where the magic happens :)
  import profile.simple._

  /**
   * Provides the set of DDL from all the tables, etc that this Schema offers. Subclasses must override this so this
   * class can provide functionality around the DDL.
   */
  val ddl : DDL

  def create(implicit session: Session): Unit = {
    if (schema.isDefined) {
      val update = sketch.makeSchema
      update.execute
    }
    ddl.create
  }

}

/**
 * The Sketch for H2 Database
 * @param schema - optional schema name for the profile
 */
case class H2Profile(override val schema: Option[String] = None ) extends Sketch
{
  override val profile: ExtendedProfile = H2Driver
  override val driverClass : String = "org.h2.Driver"
  override def makeSchema : StaticQuery0[Int] = StaticQuery.u + "SET TRACE_LEVEL_FILE 4; CREATE SCHEMA IF NOT EXISTS " + schema.get
}

/**
 * The Sketch for H2 Database
 * @param schema - optional schema name for the profile
 */
case class MySQLProfile (override val schema: Option[String] = None )  extends Sketch {
  override val profile: ExtendedProfile = MySQLDriver
  override val driverClass : String = "com.mysql.jdbc.Driver"
}

/**
 * The Sketch for SQLite Database
 * @param schema - optional schema name for the profile
 */
class SQLiteProfile (override val schema: Option[String] = None ) extends Sketch {
  override val profile: ExtendedProfile = SQLiteDriver
  override val driverClass : String = "org.sqlite.JDBC"

}

/**
 * The Sketch for Postgres Database
 * @param schema - optional schema name for the profile
 */
class PostgresProfile (override val schema: Option[String] = None ) extends Sketch {
  override val profile: ExtendedProfile = PostgresDriver
  override val driverClass : String = "org.postgresql.Driver"

}

