/**********************************************************************************************************************
 * This file is part of Scrupal a Web Application Framework.                                                          *
 *                                                                                                                    *
 * Copyright (c) 2014, Reid Spencer and viritude llc. All Rights Reserved.                                            *
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

package scrupal.db

import play.api.Configuration
import play.api.Play.current

import play.modules.reactivemongo.json.collection.JSONCollection
import reactivemongo.api.{DefaultDB, MongoConnection, MongoDriver}
import scrupal.utils.{ScrupalComponent, ConfigHelper, Registry, Registrable}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.util.{Try, Failure, Success}

import scala.concurrent.ExecutionContext.Implicits.global

/** Database Context.
  * Provides the context for connecting to a Mongo Database Replica Set via the URL and credentials provided.
  * This just collects together the driver, connection and [[reactivemongo.api.MongoConnection.ParsedURI]].
  * The utility methods withContext, withConnection and withDatabase provide functional access to the
  * context, connection and database.
  */
case class DBContext(id: Symbol, uri: String, user: Option[String] = None, pass: Option[String] = None) extends Registrable {

  val default_dbName = "scrupal"

  val parsedURI = MongoConnection.parseURI(uri) match {
    case Success(u) => u
    case Failure(xcptn) => throw xcptn
  }

  val connection = DBContext.driver.connection(parsedURI)

  def withDatabase[T](f: (DefaultDB) => T) : T = {
    val name = parsedURI.db match {
      case None => default_dbName
      case Some(dbName) => dbName
    }
    f(connection.db(name))
  }

  def withCollection[T](collName: String)(f : (JSONCollection) => T) : T = {
    withDatabase { db =>
      val coll = db.collection[JSONCollection](collName, ScrupalFailoverStrategy)
      f(coll)
    }
  }

  def emptyDatabase() = {
    withDatabase { db =>
      val future = db.collectionNames map { names =>
        val futures = for (cName <- names) yield {
          val coll = db.collection[JSONCollection](cName)
          coll.drop()
        }
        Await.ready(Future sequence futures, Duration.Inf)
      }
      Await.ready(future, Duration.Inf)
    }
  }

  def isEmpty = {
    withDatabase { db =>
      val future = db.collectionNames
      val list = Await.result(future, Duration.Inf)
      list.isEmpty
    }
  }

}

object DBContext extends Registry[DBContext] with ScrupalComponent {
  val registrantsName: String = "dbContext"
  val registryName: String = "DatabaseContexts"

  private var driver = MongoDriver()
  private var connection: Option[MongoConnection] = None

  def fromConfiguration() : DBContext = {
    val helper = new ConfigHelper(play.api.Play.application.configuration)
    val config = helper.getDbConfig
    config.getConfig("db.scrupal") match {
      case Some(cfg) => fromSpecificConfig('scrupal, cfg)
      case None => fromURI('scrupal, "mongodb://localhost/scrupal")
    }
  }

  def fromSpecificConfig(id: Symbol, config: Configuration) : DBContext = {
    config.getString("uri") match {
      case Some(uri) => fromURI(id, uri)
      case None => throw new Exception("Missing 'uri' in database configuration for '" + id.name + "'")
    }
  }

  def fromURI(id: Symbol, uri: String) : DBContext = {
    getRegistrant(id) match {
      case Some(dbc) => dbc
      case None =>
        val result = new DBContext(id, uri)
        register(result)
        result
    }
  }

  def startup() : Unit = Try {
    driver = MongoDriver()
  } match {
    case Success(x) => log.debug("Successful mongoDB startup.")
    case Failure(x) => log.error("Failed to start up mongoDB", x)
  }

  def shutdown() : Unit = Try {
    connection match {
      case Some(conn) => log.debug("Closing mongoDB connections"); conn.close()
      case None => log.debug("No mongoDB connections to close.")
    }
    driver.close()
  } match {
    case Success(x) => log.debug("Successful mongoDB shutdown.")
    case Failure(x) => log.error("Failed to shut down mongoDB", x)
  }

}

