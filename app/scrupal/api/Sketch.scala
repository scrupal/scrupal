package scrupal.api

import scala.slick.driver._
import scala.slick.jdbc.{StaticQuery, StaticQuery0}
import scala.slick.session.Database
import play.api.Logger
import play.api.db.DB

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
  def makeSession = {
    Logger.debug("Creating DB Session for " + driverClass)
    database.createSession()
  }
  def makeSchema : StaticQuery0[Int] = throw new NotImplementedError("Making DB Schema for " + driverClass )
  override def toString = { "{" + driverClass + ", " + schema + "," + profile }
}

/**
 * The Sketch for H2 Database
 * @param schema - optional schema name for the profile
 */
case class H2Sketch(url: String, override val schema: Option[String] = None ) extends Sketch
{
  override val profile: ExtendedProfile = H2Driver
  override val driverClass : String = "org.h2.Driver"
  override val database = Database.forURL(url, user = "", password="", driver=driverClass)
  override def makeSchema : StaticQuery0[Int] = StaticQuery.u + "SET TRACE_LEVEL_FILE 4; CREATE SCHEMA IF NOT EXISTS " + schema.get
}

/**
 * The Sketch for H2 Database
 * @param schema - optional schema name for the profile
 */
case class MySQLSketch (url: String, override val schema: Option[String] = None )  extends Sketch {
  override val profile: ExtendedProfile = MySQLDriver
  override val driverClass : String = "com.mysql.jdbc.Driver"
  override val database = Database.forURL(url, driver=driverClass)
}

/**
 * The Sketch for SQLite Database
 * @param schema - optional schema name for the profile
 */
class SQLiteSketch (url: String, override val schema: Option[String] = None ) extends Sketch {
  override val profile: ExtendedProfile = SQLiteDriver
  override val driverClass : String = "org.sqlite.JDBC"
  override val database = Database.forURL(url, driver=driverClass)
}

/**
 * The Sketch for Postgres Database
 * @param schema - optional schema name for the profile
 */
class PostgresSketch (url: String, override val schema: Option[String] = None ) extends Sketch {
  override val profile: ExtendedProfile = PostgresDriver
  override val driverClass : String = "org.postgresql.Driver"
  override val database = Database.forURL(url, driver=driverClass)
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

