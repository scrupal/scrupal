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

package scrupal.fakes

import play.api.test._
import play.api.mvc.Handler

import org.specs2.execute.{Result, AsResult}

import scrupal.db.{CoreSchema,DBContext}


trait Specs2ExampleGenerator {
  def apply[T : AsResult](f: => T): Result
}

object WithFakeScrupal {
  implicit val dbContext = DBContext.fromURI('test, "mongodb://localhost:27017/test")
}

/**
 * One line sentence description here.
 * Further description here.
 */
abstract class WithFakeScrupal(
  path: java.io.File = new java.io.File("."),
  classLoader: ClassLoader = classOf[FakeApplication].getClassLoader,
  additionalPlugins: Seq[String] = Nil,
  withoutPlugins: Seq[String] = Nil,
  additionalConfiguration: Map[String, _ <: Any] = Map.empty,
  withGlobal: Option[play.api.GlobalSettings] = None,
  withRoutes: PartialFunction[(String, String), Handler] = PartialFunction.empty
) extends WithApplication(new FakeApplication(path, classLoader, additionalPlugins, withoutPlugins,
  additionalConfiguration, withGlobal, withRoutes)) {

  // WARNING: Do NOT put anything but def and lazy val because of DelayedInit or app startup will get invoked twice
  // and you'll have a real MESS on your hands!!!! (i.e. no db interaction will work!)

  def withCoreSchema[T : AsResult]( f: CoreSchema => T ) : T = {
    implicit val schema : CoreSchema = new CoreSchema(WithFakeScrupal.dbContext)
    schema.create(WithFakeScrupal.dbContext)
    f(schema)
  }

  def withDBContext[T: AsResult]( f: DBContext => T) : T = {
    f(WithFakeScrupal.dbContext)
  }
}
