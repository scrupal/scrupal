/**********************************************************************************************************************
 * This file is part of Scrupal, a Scalable Reactive Content Management System.                                       *
 *                                                                                                                    *
 * Copyright © 2015 Reactific Software LLC                                                                            *
 *                                                                                                                    *
 * Licensed under the Apache License, Version 2.0 (the "License");  you may not use this file                         *
 * except in compliance with the License. You may obtain a copy of the License at                                     *
 *                                                                                                                    *
 *        http://www.apache.org/licenses/LICENSE-2.0                                                                  *
 *                                                                                                                    *
 * Unless required by applicable law or agreed to in writing, software distributed under the                          *
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,                          *
 * either express or implied. See the License for the specific language governing permissions                         *
 * and limitations under the License.                                                                                 *
 **********************************************************************************************************************/

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

package scrupal.store.reactivemongo

import java.util.concurrent.atomic.AtomicInteger

import com.typesafe.config.ConfigFactory
import rxmongo.bson.BSONCollection
import reactivemongo.api.{MongoConnection, MongoDriver}
import scrupal.utils._

import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}

/** Database Context.
  * Provides the context for connecting to a Mongo Database Replica Set via the URL and credentials provided.
  * This just collects together the driver, connection and [[reactivemongo.api.MongoConnection.ParsedURI]].
  * The utility methods withContext, withConnection and withDatabase provide functional access to the
  * context, connection and database.
  *
  */
case class DBContext(id: Symbol, mongo_uri: String, driver: MongoDriver,
                     user: Option[String] = None, pass: Option[String] = None) extends Registrable[DBContext] with ScrupalComponent {
  def registry = DBContext
  def asT = this

  val parsedURI = MongoConnection.parseURI(mongo_uri) match {
    case Success(uri) ⇒ uri
    case Failure(xcptn) ⇒ throw xcptn
  }

  val connection = driver.connection(parsedURI)

  def withDatabase[T](dbName: String)(f: (ScrupalDB) ⇒ T) : T = {
    implicit val database = new ScrupalDB(dbName, connection)
    f(database)
  }

  def withCollection[T](db: ScrupalDB, collName: String)(f : (BSONCollection) ⇒ T) : T = {
    val coll = db.collection[BSONCollection](collName, DefaultFailoverStrategy)
    f(coll)
  }

  def checkExists(dbNames:Seq[String]) : Seq[String] = {
    // FIXME: Reactive mongo doesn't support "getDatabaseNames" call to Mongo.
    dbNames
  }

  def close() = Try {
    if (connection != null) {
      log.debug("Closing registered context:" + id.name + " with connection: " + connection)
      connection.askClose()(3.seconds)
    }
  } match {
    case Success(x) ⇒ log.debug("Closed DBContext '" + id.name + "' successfully.")
    case Failure(x) ⇒ log.error("Failed to close DBContext '" + id.name + "': ", x)
  }

}

object DBContext extends Registry[DBContext] with ScrupalComponent {
  val registrantsName: String = "dbContext"
  val registryName: String = "DatabaseContexts"

  private case class State(driver: MongoDriver, counter: AtomicInteger = new AtomicInteger(1))
  private var state: Option[State] = None

  def fromConfiguration(id: Symbol, conf: Option[Configuration] = None) : DBContext = {
    val topConfig = conf.getOrElse(ConfigHelpers.default)
    val helper = new ConfigHelper(topConfig)
    val config = helper.getDbConfig
    config.getConfig("db.scrupal") match {
      case Some(cfg) ⇒ fromSpecificConfig(id, cfg)
      case None ⇒ fromURI(id, "mongodb://localhost/scrupal")
    }
  }

  def fromSpecificConfig(id: Symbol, config: Configuration) : DBContext = {
    config.getString("uri") match {
      case Some(uri) ⇒ fromURI(id, uri)
      case None ⇒ throw new Exception("Missing 'uri' in database configuration for '" + id.name + "'")
    }
  }

  def fromURI(id: Symbol, uri: String) : DBContext = {
    getRegistrant(id) match {
      case Some(dbc) ⇒ dbc
      case None ⇒
        state match {
          case Some(s) ⇒
            val result = new DBContext(id, uri, s.driver)
            result
          case None ⇒
            toss("The mongoDB driver has not been initialized")
        }
    }
  }

  def numberOfStartups : Int = {
    state match {
      case None ⇒ 0
      case Some(s) ⇒ s.counter.get()
    }
  }

  def isStartedUp : Boolean = {
    state match {
      case None ⇒ false
      case Some(s) ⇒ ! s.driver.system.isTerminated
    }
  }

  def startup() : Unit = Try {
    state match {
      case Some(s) ⇒
        val startCount = s.counter.incrementAndGet()
        log.debug("The mongoDB driver initialized " + startCount + " times.")
      case None ⇒
        val full_config = ConfigFactory.load()
        val driver = MongoDriver(full_config)
        val s = State(driver)
        state = Some(State(driver))
    }
  } match {
    case Success(x) ⇒ log.debug("Successful mongoDB startup.")
    case Failure(x) ⇒ log.error("Failed to start up mongoDB", x)
  }

  def shutdown() : Unit = Try {
    state match {
      case Some(s) ⇒
        s.counter.decrementAndGet() match {
          case 0 ⇒
            for (ctxt <- values) {
              ctxt.close()
              ctxt.unregister()
            }
            Try { s.driver.close(10.seconds) } match {
              case Success(x) ⇒ log.debug("Successfully closed ReactiveMongo Driver")
              case Failure(x) ⇒ log.error("Failed to close ReactiveMongo Driver", x)
            }
            for (connection <- s.driver.connections) {
              log.debug("Connection remains open:" + connection)
            }
            state = None
          case x: Int ⇒
            log.debug("The DBContext requires " + x + " more shutdowns before MongoDB driver shut down.")
        }
      case None ⇒
        log.debug("The MongoDB Driver has never been started up.")
    }
  } match {
    case Success(x) ⇒ log.debug("Successful DBContext shutdown.")
    case Failure(x) ⇒ log.error("Failed to shut down DBContext", x)
  }
}

