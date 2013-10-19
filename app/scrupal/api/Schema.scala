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

import scala.slick.lifted.{ForeignKeyAction, DDL}
import org.joda.time.DateTime

import scala.slick.driver._
import scala.slick.jdbc.{StaticQuery0, StaticQuery}
import java.sql.{Clob, Timestamp}
import play.api.libs.json.{Json, JsValue}
import scala.Predef._
import scala.Some
import scala.Tuple2
import scala.slick.session.{Session, Database}
import play.api.Logger

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
  val database: scala.slick.session.Database
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
trait Component  {

  val sketch : Sketch
  // So we can define the Table items and queries, we import the Slick database profile here
  import sketch.profile.simple._

  import scrupal.db.CommonTypeMappers._

  implicit val session: Session

  abstract class ScrupalTable[S](tName: String) extends Table[S](sketch.schema, tName) {
    protected def nm(columnName: String) : String = {
      (if (schemaName.isDefined) schemaName.get + "." else "") + tableName + "_" + columnName
    }

    protected def fkn(foreignTableName: String ) : String = {
      nm( foreignTableName + "_fkey")
    }

    protected def idx(name: String) : String = nm(name + "_idx")

    lazy val findAllQuery = for (e <- this) yield e

     def findAll() : Seq[S] = findAllQuery.list()
  }

  trait SymbolicTable[S <: SymbolicIdentifiable] extends ScrupalTable[S] with AbstractStorage[Symbol,Symbol,S] {

    def id = column[Symbol](nm("id"), O.PrimaryKey)

    lazy val fetchByIDQuery = for { id <- Parameters[Symbol] ; ent <- this if ent.id === id } yield ent

    override def fetch(id: Symbol) : Option[S] =  fetchByIDQuery(id).firstOption

    override def insert(entity: S) : Symbol = { *  insert(entity) ; entity.id }

    override def update(entity: S) : Int = this.filter(_.id === entity.id) update(entity)

    override def delete(entity: S) : Boolean = delete(entity.id)

    override def delete(id: Symbol) : Boolean =  {
      this.filter(_.id === id).delete > 0
    }
  }

  trait NumericTable[S <: NumericIdentifiable] extends ScrupalTable[S] with StorageFor[S] {

    def id = column[Identifier](nm("id"), O.PrimaryKey, O.AutoInc)

    lazy val fetchByIDQuery = for { id <- Parameters[Identifier] ; ent <- this if ent.id === id } yield ent

    override def fetch(id: Identifier) : Option[S] =  fetchByIDQuery(id).firstOption

    override def insert(entity: S) : Identifier = * returning id insert(entity)

    override def update(entity: S) : Int = this.filter(_.id === entity.id) update(entity)

    override def delete(entity: S) : Boolean = delete(entity.id)

    protected def delete(oid: Option[Identifier]) : Boolean = {
      oid match { case None => false; case Some(id) => delete(id) }
    }

    override def delete(id: Identifier) : Boolean =  {
      this.filter(_.id === id).delete > 0
    }

  }

  trait CreatableTable[S <: Creatable] extends ScrupalTable[S] {

    def created = column[DateTime](nm("created"), O.Nullable) // FIXME: Dynamic Date required!

    lazy val findSinceQuery = for {
      created <- Parameters[DateTime] ;
      e <- this if (e.created > created)
    } yield e

    case class CreatedSince(d: DateTime) extends FinderOf[S] { override def apply() = findSinceQuery(d).list }
  }

  trait ModifiableTable[S <: Modifiable] extends ScrupalTable[S] {

    def modified_index = index(nm("modified_index"), modified, unique=false)

    def modified = column[DateTime](nm("modified"), O.Nullable) // FIXME: Dynamic Date required!

    lazy val modifiedSinceQuery = for {
      chg <- Parameters[DateTime];
      mt <- this if mt.modified > chg
    } yield mt

    case class ModifiedSince(d: DateTime) extends FinderOf[S] { override def apply() = modifiedSinceQuery(d).list }
  }

  trait NameableTable[S <: Nameable] extends ScrupalTable[S] {
    def name = column[Symbol](nm("name"), O.NotNull)

    def name_index = index(idx("name"), name, unique=true)

    lazy val fetchByNameQuery = for {
      n <- Parameters[Symbol] ;
      e <- this if e.name === n
    } yield e

    case class ByName(n: Symbol) extends FinderOf[S] { override def apply() = fetchByNameQuery(n).list }
  }

  trait DescribableTable[S <: Describable] extends ScrupalTable[S] {
    def description = column[String](nm("_description"), O.NotNull)
  }

  trait EnablableTable[S <: Enablable] extends ScrupalTable[S] {
    def enabled = column[Boolean](nm("enabled"), O.NotNull)
    def enabled_index = index(idx("enabled"), enabled, unique=false)

    lazy val enabledQuery = for { en <- this if en.enabled === true } yield en
    def allEnabled() : List[S] = enabledQuery.list
  }

  trait NumericThingTable[S <: NumericThing]
    extends NumericTable[S]
    with NameableTable[S]
    with CreatableTable[S]
    with ModifiableTable[S]
    with DescribableTable[S]

  trait NumericEnablableThingTable[S <: NumericEnablableThing]
    extends NumericThingTable[S] with EnablableTable[S]

  trait SymbolicThingTable[S <: SymbolicThing]
    extends SymbolicTable[S]
    with CreatableTable[S]
    with ModifiableTable[S]
    with DescribableTable[S]

  trait SymbolicEnablableThingTable[S <: SymbolicEnablableThing]
    extends SymbolicThingTable[S] with EnablableTable[S]


  /**
   * The base class of all correlation tables.
   * This allows many-to-many relationships to be established by simply listing the pairs of IDs
   */
  abstract class ManyToManyTable[A <: NumericIdentifiable, B <: NumericIdentifiable ] (tableName: String,
      nameA: String, nameB: String, tableA: NumericTable[A], tableB: NumericTable[B])
      extends ScrupalTable[(Identifier,Identifier)](tableName) {
    def a_id = column[Identifier](nm(nameA + "_id"))
    def b_id = column[Identifier](nm(nameB + "_id"))
    def a_fkey = foreignKey(fkn(nameA), a_id, tableA)(_.id, onDelete = ForeignKeyAction.Cascade )
    def b_fkey = foreignKey(fkn(nameB), b_id, tableB)(_.id, onDelete = ForeignKeyAction.Cascade )
    def a_b_uniqueness = index(idx(nameA + "_" + nameB), (a_id, b_id), unique= true)
    lazy val findBsQuery = for { aId <- Parameters[Long]; a <- this if a.a_id === aId; b <- tableB if b.id === a.b_id } yield b
    lazy val findAsQuery = for { bId <- Parameters[Long]; b <- this if b.b_id === bId; a <- tableA if a.id === b.a_id } yield a
    def selectAssociatedA(b: B) : List[A] = { if (b.id.isDefined) findAsQuery(b.id.get).list else List() }
    def selectAssociatedB(a: A) : List[B] = { if (a.id.isDefined) findBsQuery(a.id.get).list else List() }
    def * = a_id ~ b_id

  };

  /**
   * The base class of all tables that provide a string key to reference some Identifiable table.
   * This allows a
   */
  abstract class NamedNumericTable[ReferentType <: NumericIdentifiable](
      tableName: String, valueTable: NumericTable[ReferentType])
      extends ScrupalTable[(String,Identifier)](tableName) {
    def key = column[String](nm("key"))
    def value = column[Identifier](nm("value"))
    def value_fkey = foreignKey(fkn(valueTable.tableName), value, valueTable)(_.id,
      onDelete = ForeignKeyAction.Cascade)
    def key_value_index = index(idx("key_value"), on=(key, value), unique=true)
    def * = key ~ value

    def insert( pair: (String, Long) )  = { * insert pair }

    def insert(k: String, v: Long) : Unit = insert( Tuple2(k, v) )

    // -- operations on rows
    def delete(k: String) : Boolean =  {
      this.filter(_.key === k).delete > 0
    }

    lazy val findValuesQuery = for {
      k <- Parameters[String];
      ni <- this if ni.key === k ;
      vt <- valueTable if ni.value === vt.id
    } yield vt

    def findValues(aKey: String): List[ReferentType] = findValuesQuery(aKey).list

    lazy val findKeysQuery = for {
      id <- Parameters[Long];
      ni <- this if ni.value === id
    } yield ni.key

    def findKeys(id : Long) : List[String] = findKeysQuery(id).list
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
abstract class Schema(val sketch: Sketch )(override implicit val session: Session) extends  Component
{
  // The primary reason for this class to exist is to give concrete values to ScrupalSketch's members based on
  // an instance not inheritance. ScrupalSketch is abstract and has no concrete members. We want to have those members
  // be filled out from
  val profile: ExtendedProfile = sketch.profile
  val schema: Option[String] = sketch.schema
  val driverClass : String = sketch.driverClass

  // override implicit val session : Session = sketch.database.createSession()

  // This is where the magic happens :)
  import profile.simple._

  def toClob(js: JsValue) : Clob = {
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
    Logger.debug("Creating Schema: \n" + (ddl.createStatements mkString("\n")))
    ddl.create
  }
}


/**
 * The Sketch for H2 Database
 * @param schema - optional schema name for the profile
 */
case class H2Sketch(url: String, override val schema: Option[String] = None ) extends Sketch
{
  override val profile: ExtendedProfile = H2Driver
  override val driverClass : String = "org.h2.Driver"
  override val database = Database.forURL(url, driverClass)
  override def makeSchema : StaticQuery0[Int] = StaticQuery.u + "SET TRACE_LEVEL_FILE 4; CREATE SCHEMA IF NOT EXISTS " + schema.get
}

/**
 * The Sketch for H2 Database
 * @param schema - optional schema name for the profile
 */
case class MySQLSketch (url: String, override val schema: Option[String] = None )  extends Sketch {
  override val profile: ExtendedProfile = MySQLDriver
  override val driverClass : String = "com.mysql.jdbc.Driver"
  override val database = Database.forURL(url, driverClass)
}

/**
 * The Sketch for SQLite Database
 * @param schema - optional schema name for the profile
 */
class SQLiteSketch (url: String, override val schema: Option[String] = None ) extends Sketch {
  override val profile: ExtendedProfile = SQLiteDriver
  override val driverClass : String = "org.sqlite.JDBC"
  override val database = Database.forURL(url, driverClass)
}

/**
 * The Sketch for Postgres Database
 * @param schema - optional schema name for the profile
 */
class PostgresSketch (url: String, override val schema: Option[String] = None ) extends Sketch {
  override val profile: ExtendedProfile = PostgresDriver
  override val driverClass : String = "org.postgresql.Driver"
  override val database = Database.forURL(url, driverClass)
}

object Sketch {
  /** Convert a URL into a Database Sketch by examining its prefix
   * @param url - The JDBC Connection URL for the database we should connect to
   * @return A Sketch for the corresponding database type
   */
  def apply(url: String, schema: Option[String] = None ) : Sketch = {
    Logger.debug("Creating Sketch With: " + url )
    url match {
      case s if s.startsWith("jdbc:h2:") => return new H2Sketch(url, schema)
      case s if s.startsWith("jdbc:mysql:") => return new MySQLSketch(url, schema)
      case s if s.startsWith("jdbc:sqllite:") => return new SQLiteSketch(url, schema)
      case s if s.startsWith("jdbc:postgresql:") => return new PostgresSketch(url, schema)
      case _ => throw new UnsupportedOperationException("JDBC Url (" + url + ") is not for a supported database.")
    }
  }
}

