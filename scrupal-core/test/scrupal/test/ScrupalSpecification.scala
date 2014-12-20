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

package scrupal.test

import java.util.concurrent.atomic.AtomicInteger

import org.specs2.specification.{Step, Fragments}
import scrupal.core.api.{Scrupal, Schema}
import scrupal.db.{DBContext, DBContextSpecification}

import scala.concurrent.duration.{Duration, FiniteDuration}

/**
 * One line sentence description here.
 * Further description here.
 */
abstract class ScrupalSpecification(specName: String, timeout: FiniteDuration = Duration(5,"seconds"))
  extends DBContextSpecification(specName, timeout) {

  // WARNING: Do NOT put anything but def and lazy val because of DelayedInit or app startup will get invoked twice
  // and you'll have a real MESS on your hands!!!! (i.e. no db interaction will work!)

  implicit lazy val scrupal = new Scrupal(ScrupalSpecification.next(specName))

  // Handle one time startup and teardown of the DBContext
  private object Actions {
    lazy val open = { scrupal.open() }
    lazy val close= { scrupal.close() }
  }

  override def map(fs: => Fragments) = Step(Actions.open) ^ super.map(fs) ^ Step(Actions.close)

  def withSchema[T]( f: Schema => T ) : T = {
    withDBContext { dbContext: DBContext =>
      val schema: Schema = new Schema(dbContext, specName)
      schema.create(dbContext)
      f(schema)
    }
  }
}

object ScrupalSpecification {

  def next(name: String) : String = name + "-" + counter.incrementAndGet()
  val counter = new AtomicInteger(0)

}
