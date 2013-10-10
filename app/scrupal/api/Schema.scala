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

package scrupal.api

import scala.slick.driver.ExtendedProfile
import scala.slick.lifted.{ForeignKeyAction, DDL}
import org.joda.time.DateTime
import org.joda.time.DateTimeZone.UTC
import play.api.Logger
import scala.slick.driver._
import scala.slick.jdbc.{StaticQuery0, StaticQuery}
import scala.slick.direct.AnnotationMapper.column
import java.sql.{Clob, Timestamp}
import play.api.libs.json.{JsObject, Json, JsValue}

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
 * The abstract database component.
 * This trait allows use to define database components which are simply collections of related tables and the
 * various query methods on those tables to provide access to them. Since Components contain Tables and Scrupal requires
 * all database entities to have a particular shape, that shape is enforced in the EntityTable class. Note that
 * Component extends Sketch which is mixed in to other components but resolved by the Schema class.
 */
trait Component extends Sketch { this : Sketch =>

  // So we can define the Table items and queries, we import the Slick database profile here
  import profile.simple._

  implicit lazy val dateTimeMapper = MappedTypeMapper.base[DateTime,Timestamp](
    { d => new Timestamp( d getMillis ) },
    { t => new DateTime (t getTime, UTC)  }
  )

  implicit lazy val symbolMapper = MappedTypeMapper.base[Symbol,String] (
    { s => s.name},
    { s => Symbol(s) }
  )

  abstract class IdentifiableTable[C <: Identifiable[C]](val tname: String) extends Table[C](schema, tname ) {
    def id = column[Long](tableName + "_id", O.PrimaryKey, O.AutoInc);

    lazy val fetchByIDQuery = for { id <- Parameters[Long] ; ent <- this if ent.id === id } yield ent

    def fetch(id: Long)(implicit s: Session) : Option[C] = fetchByIDQuery(id).firstOption

    def fetch(oid: Option[Long])(implicit s: Session) : Option[C] = {
      oid match { case None => None ; case Some(id) => fetch(id) }
    }

    def insert(entity: C)( implicit s: Session ) : C = {
      val resulting_id = * returning id insert(entity)
      entity.forId(resulting_id)
    }

    def update(entity: C)(implicit s: Session ) : C = {
      this.filter(_.id === entity.id) update(entity)
      entity
    }

    def delete(entity: C)(implicit s: Session) : Boolean = delete(entity.id)

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
      if (entity.id.isDefined) update(entity) else insert(entity)
    }
  }

  abstract class CreatableTable[C <: Creatable[C]](tname: String) extends IdentifiableTable[C](tname) {
    def created = column[DateTime](tableName + "_created", O.Nullable) // FIXME: Dynamic Date required!

    lazy val findSinceQuery = for {  created <- Parameters[DateTime] ; e <- this if e.created >= created } yield e

    def findSince(dt: DateTime)(implicit s: Session ) : List[C] = findSinceQuery(dt).list

  }

  abstract class ImmutableThingTable[C <: ImmutableThing[C]](tname: String) extends CreatableTable[C](tname) {
    def name = column[Symbol](tableName + "_name", O.NotNull)

    def name_index = index(tableName + "_name_index", name, unique=true)

    lazy val fetchByNameQuery = for { n <- Parameters[Symbol] ; e <- this if e.name === n } yield e

    def fetch(aName: Symbol)(implicit s: Session) : Option[C] = fetchByNameQuery(aName).firstOption

  }

  /**
   * The base class of all table definitions in Scrupal.
   * Most tables in the Scrupal database represent some form of entity that can be manipulated through the
   * admin interface. To ensure a certain level of consistency across all such entities, we enforce the structure
   * of the entities with this class. Every entity table should subclass from EntityTable
   * @param tableName The name of the table in the database
   * @tparam C The case class that represents rows in this table
   */
  abstract class DescribedThingTable[C <: DescribedThing[C]](tableName: String)
    extends ImmutableThingTable[C] (tableName) {

    def description = column[String](tableName + "_description", O.NotNull)

  };

  abstract class ThingTable[C <: Thing[C]](tableName: String)
    extends DescribedThingTable[C](tableName) {

    def modified = column[DateTime](tableName + "_modified", O.Nullable)

    def modified_index = index(tableName + "_modified_index", modified, unique=false)

    override def update(thing: C)(implicit s: Session ) : C = {
      val selected = this.where(_.id === thing.id)
      selected.update(thing)
      selected.map(t => t.modified).update(DateTime.now())
      thing
    }

    lazy val modifiedSinceQuery = for { chg <- Parameters[DateTime]; mt <- this if mt.modified > chg } yield mt

    def modifiedSince(chg: DateTime)(implicit s: Session) : List[C] = modifiedSinceQuery(chg).list

  };

  /**
   * The base class of all correlation tables.
   * This allows many-to-many relationships to be established by simply listing the pairs of IDs
   */
  abstract class ManyToManyTable[A <: Identifiable[A], B <: Identifiable[B] ] (tableName: String,
      nameA: String, nameB: String, tableA: IdentifiableTable[A], tableB:  IdentifiableTable[B])
      extends Table[(Long,Long)](schema, tableName) {
    def a_id = column[Long](tableName + "_" + nameA + "_id")
    def b_id = column[Long](tableName + "_" + nameB + "_id")
    def a_fkey = foreignKey(tableName + "_" + nameA + "_fkey", a_id, tableA)(_.id, onDelete = ForeignKeyAction.Cascade )
    def b_fkey = foreignKey(tableName + "_" + nameB + "_fkey", b_id, tableB)(_.id, onDelete = ForeignKeyAction.Cascade )
    def a_b_uniqueness = index(tableName + "_uniqueness", (a_id, b_id), unique= true)
    lazy val findBsQuery = for { aId <- Parameters[Long]; a <- this if a.a_id === aId; b <- tableB if b.id === a.b_id } yield b
    lazy val findAsQuery = for { bId <- Parameters[Long]; b <- this if b.b_id === bId; a <- tableA if a.id === b.a_id } yield a
    def selectAssociatedA(b: B)(implicit s: Session) : List[A] = { if (b.id.isDefined) findAsQuery(b.id.get).list else List() }
    def selectAssociatedB(a: A)(implicit s: Session) : List[B] = { if (a.id.isDefined) findBsQuery(a.id.get).list else List() }
    def * = a_id ~ b_id

  };

  /**
   * The base class of all tables that provide a string key to reference some Identifiable table.
   * This allows a
   */
  abstract class NamedIdentifiableTable[ReferentType <: Identifiable[ReferentType]](
      tableName: String, valueTable: IdentifiableTable[ReferentType])
      extends Table[(String,Long)](schema, tableName) {
    def key = column[String](tableName + "_key")
    def value = column[Long](tableName + "_value")
    def value_fkey = foreignKey(tableName + "_value_fkey", value, valueTable)(_.id, onDelete = ForeignKeyAction.Cascade)
    def key_value_index = index(tableName + "_key_value_index", on=(key, value), unique=true)
    def * = key ~ value

    def insert( pair: (String, Long) )( implicit s: Session)  = { * insert pair }

    def insert(k: String, v: Long)( implicit s: Session ) : Unit = insert( Tuple2(k, v) )

    // -- operations on rows
    def delete(k: String)(implicit s: Session ) : Boolean =  {
      this.filter(_.key === k).delete > 0
    }

    lazy val findValuesQuery = for {
      k <- Parameters[String];
      ni <- this if ni.key === k ;
      vt <- valueTable if ni.value === vt.id
    } yield vt

    def findValues(aKey: String)(implicit s: Session) : List[ReferentType] = findValuesQuery(aKey).list

    lazy val findKeysQuery = for {
      id <- Parameters[Long];
      ni <- this if ni.value === id
    } yield ni.key

    def findKeys(id : Long)(implicit s: Session) : List[String] = findKeysQuery(id).list
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

  def toClob(js: JsValue)(implicit session: Session) : Clob = {
    val clob = session.conn.createClob()
    clob.setString(1, Json.stringify(js));
    clob
  }

  def toJson(clob: Clob) : JsValue = Json.parse(clob.getSubString(1,Int.MaxValue))
  // FIXME: Can't we parse an InputStream?


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
case class H2Sketch(override val schema: Option[String] = None ) extends Sketch
{
  override val profile: ExtendedProfile = H2Driver
  override val driverClass : String = "org.h2.Driver"
  override def makeSchema : StaticQuery0[Int] = StaticQuery.u + "SET TRACE_LEVEL_FILE 4; CREATE SCHEMA IF NOT EXISTS " + schema.get
}

/**
 * The Sketch for H2 Database
 * @param schema - optional schema name for the profile
 */
case class MySQLSketch (override val schema: Option[String] = None )  extends Sketch {
  override val profile: ExtendedProfile = MySQLDriver
  override val driverClass : String = "com.mysql.jdbc.Driver"
}

/**
 * The Sketch for SQLite Database
 * @param schema - optional schema name for the profile
 */
class SQLiteSketch (override val schema: Option[String] = None ) extends Sketch {
  override val profile: ExtendedProfile = SQLiteDriver
  override val driverClass : String = "org.sqlite.JDBC"

}

/**
 * The Sketch for Postgres Database
 * @param schema - optional schema name for the profile
 */
class PostgresSketch (override val schema: Option[String] = None ) extends Sketch {
  override val profile: ExtendedProfile = PostgresDriver
  override val driverClass : String = "org.postgresql.Driver"

}

