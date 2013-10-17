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

import java.io.File
import play.api.test._
import scala.slick.session.{Session}
import org.specs2.execute.{Result, AsResult}
import scrupal.api.Sketch
import scrupal.models.db.ScrupalSchema
import play.api.Logger
import play.libs.F


trait Specs2ExampleGenerator {
  def apply[T : AsResult](f: => T): Result
}

/**
 * One line sentence description here.
 * Further description here.
 */
class FakeScrupal extends FakeApplication(
  path = new File(".")
) {
  private var count = 0
  def sharedurl() : String ={ count = count + 1 ; "jdbc:h2:mem:test" + count  }
  def url : String = "jdbc:h2:mem:"
  val sketch: Sketch = Sketch(url)
}

class WithScrupal() extends WithApplication(new FakeScrupal) {
  override def around[T: AsResult](f: => T): Result = {
    super.around { f }
  }

  val fake_scrupal : FakeScrupal = app.asInstanceOf[FakeScrupal]

  def withDBSession[T : AsResult]( f: Session => T) : T = {
    implicit val session : Session = fake_scrupal.sketch.database.createSession()
    try { f(session)  } finally { if (session != null) session.close() }
  }

  def withScrupalSchema[T : AsResult]( f: ScrupalSchema => T ) : T = {
    withDBSession { implicit session : Session =>
      implicit val schema : ScrupalSchema = new ScrupalSchema(fake_scrupal.sketch)(session)
      schema.create(session)
      f(schema)
    }
  }
}
