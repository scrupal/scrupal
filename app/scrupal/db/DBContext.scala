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

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Try, Failure, Success}
import akka.actor.ActorSystem


/** Database Context.
  * Provides the context for connecting to a Mongo Database Replica Set via the URL and credentials provided.
  * This just collects together the driver, connection and [[reactivemongo.api.MongoConnection.ParsedURI]].
  * The utility methods withContext, withConnection and withDatabase provide functional access to the
  * context, connection and database.
  */
case class DBContext(id: Symbol, uri: String, driver: MongoDriver,
                     user: Option[String] = None, pass: Option[String] = None) extends Registrable with ScrupalComponent {

  val default_dbName = "scrupal"

  val parsedURI = MongoConnection.parseURI(uri) match {
    case Success(u) => u
    case Failure(xcptn) => throw xcptn
  }

  val connection = driver.connection(parsedURI)

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

  def close() = Try {
    log.debug("Closing registered context:" + id.name + " with connection: " + connection)
    connection.askClose()(3.seconds)
  } match {
    case Success(x) => log.debug("Closed DBContext '" + id.name + "' successfully.")
    case Failure(x) => log.error("Failed to close DBContext '" + id.name + "': ", x)
  }

}

object DBContext extends Registry[DBContext] with ScrupalComponent {
  val registrantsName: String = "dbContext"
  val registryName: String = "DatabaseContexts"

  private case class State(system: ActorSystem, driver: MongoDriver)
  private var state: Option[State] = None

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
        state match {
          case Some(s) =>
            val result = new DBContext(id, uri, s.driver)
            register(result)
            result
          case None =>
            toss("The mongoDB driver has not been initialized")
        }
    }
  }

  def startup() : Unit = Try {
    state match {
      case Some(s) => log.debug("The mongoDB driver is already initialized.")
      case None =>
        val system = ActorSystem("MongoDB")
        val driver = MongoDriver(system)
        state = Some(State(system, driver))
    }
  } match {
    case Success(x) => log.debug("Successful mongoDB startup.")
    case Failure(x) => log.error("Failed to start up mongoDB", x)
  }

  def shutdown() : Unit = Try {
    for ((symbol, ctxt) <- registrants) {
      ctxt.close()
    }
    state match {
      case Some(s) =>
        s.driver.close()
        val future = Future {
          while (s.driver.connections.size > 0)
            Thread.sleep(50)
        }
        Try { Await.result(future, 3.seconds) } match {
          case Success(x) => log.debug("All mongoDB connections closed.")
          case Failure(x) => log.debug("Failed to close all mongoDB connections, " + s.driver.connections.size + " remain: ", x)
        }
        for (connection <- s.driver.connections) {
          log.debug("Connection remains open:" + connection)
        }
        s.system.shutdown()
        Try { s.driver.system.awaitTermination(3.seconds) } match {
          case Success(x) => log.debug("The mongoDB driver has been closed.")
          case Failure(x) => log.debug("The mongoDB driver failed to close: ", x)
        }
      case None =>
        log.debug("No MongoDB driver to shut down.")
    }
  } match {
    case Success(x) => log.debug("Successful mongoDB shutdown.")
    case Failure(x) => log.error("Failed to shut down mongoDB", x)
  }
}

