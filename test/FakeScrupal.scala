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

package scrupal.test

import java.io.File
import play.api.{db, Logger}
import play.api.test._
import scala.slick.session.{Session, Database}
import scrupal.models.db.{Sketch, ScrupalSchema}
import org.specs2.execute.{Result, AsResult}



/**
 * One line sentence description here.
 * Further description here.
 */
object FakeScrupal extends FakeApplication(
  path = new File(".")
) {

  val url = "jdbc:h2:mem:test1"
  val sketch = scrupal.models.db.DB.sketch4URL(url)
  val schema = new ScrupalSchema(sketch)
  val db = Database.forURL(url, sketch.driverClass)
}

abstract class WithDBSession() extends WithApplication(FakeScrupal) {
  lazy val sketch : Sketch = FakeScrupal.sketch
  lazy val schema : ScrupalSchema = new ScrupalSchema(sketch)
  implicit var session : Session = null
  override def around[T: AsResult](t: => T): Result = super.around {
    FakeScrupal.db withSession { implicit s: Session =>
      session = s
      schema.create(session)
      t
    }
  }
}
