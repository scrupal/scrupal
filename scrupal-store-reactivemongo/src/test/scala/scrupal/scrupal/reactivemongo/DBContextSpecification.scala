/**********************************************************************************************************************
 * Copyright © 2014 Reactific Software, Inc.                                                                          *
 *                                                                                                                    *
 * This file is part of Scrupal, an Opinionated Web Application Framework.                                            *
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
import akka.util.Timeout
import org.specs2.mutable.{Specification}
import org.specs2.specification._
import reactivemongo.api.DefaultDB
import scrupal.utils.ScrupalComponent

import scala.concurrent.Await
import scala.concurrent.duration.{Duration, FiniteDuration}


/** A Fake DB Context used for testing
 * Created by reidspencer on 10/21/14.
 */
abstract class DBContextSpecification(val specName: String,
                                      val timeout: FiniteDuration = Duration(5,"seconds"))
  extends Specification with ScrupalComponent  {

  // WARNING: Do NOT put anything but def and lazy val because of DelayedInit or app startup will get invoked twice
  // and you'll have a real MESS on your hands!!!! (i.e. no db interaction will work!)


  // Handle one time startup and teardown of the DBContext
  object dbContextActions {
    lazy val startDB = { DBContext.startup() }
    lazy val stopDB = { DBContext.shutdown() }
  }

  lazy val dbActions = dbContextActions
  override def map(fs: ⇒ Fragments) = Step(dbActions.startDB) ^ fs ^ Step(dbActions.stopDB)

  override def logger_identity = specName

  implicit lazy val akka_timeout : Timeout = timeout

  lazy val uri = "mongodb://localhost:27017/"
  lazy val counter : AtomicInteger = new AtomicInteger(0)
  def getDBContext() : DBContext = {
    val name = Symbol(specName + "-" + counter.incrementAndGet() )
    DBContext.fromURI(name, uri)
  }

  private def doWithDBC[T]( f: (DBContext) ⇒ T) : T = {
    val dbc = getDBContext()
    try {
      f (dbc)
    }
    finally {
      dbc.close()
    }
  }


  def withDBContext[T]( f: (DBContext) ⇒ T ) : T = {
    doWithDBC { implicit dbc ⇒
      f(dbc)
    }
  }

  def withDB[T](dbName: String) ( f : (DefaultDB) ⇒ T) : T = {
    doWithDBC { dbc ⇒
      dbc.withDatabase(dbName) { implicit db ⇒
        f(db)
      }
    }
  }

  def withEmptyDB[T](dbName: String)( f : (ScrupalDB) ⇒ T) : T = {
    doWithDBC { dbc ⇒
      dbc.withDatabase(dbName) { implicit db ⇒
        val future = db.emptyDatabase
        Await.result(future, timeout)
        f(db)
      }
    }
  }
}
