/**********************************************************************************************************************
 * Copyright © 2014 Reactific Software LLC                                                                            *
 *                                                                                                                    *
 * This file is part of Scrupal, an Opinionated Web Application Framework.                                            *
 *                                                                                                                    *
 * Scrupal is free software: you can redistribute it and/or modify it under the terms                                 *
 * of the GNU General Public License as published by the Free Software Foundation,                                    *
 * either version 3 of the License, or (at your option) any later version.                                            *
 *                                                                                                                    *
 * Scrupal is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;                               *
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                          *
 * See the GNU General Public License for more details.                                                               *
 *                                                                                                                    *
 * You should have received a copy of the GNU General Public License along with Scrupal.                              *
 * If not, see either: http://www.gnu.org/licenses or http://opensource.org/licenses/GPL-3.0.                         *
 **********************************************************************************************************************/

package scrupal.test

import org.specs2.execute.{Result, AsResult}
import org.specs2.specification.Fixture

/** Title Of Thing.
  *
  * Description of thing
  */
class ClassFixture[CLASS <: AutoCloseable](create: ⇒ CLASS) extends Fixture[CLASS] {
  def apply[R: AsResult](f: CLASS => R) = {
    val fixture = create
    try {
      AsResult( f ( fixture ) )
    } finally {
      fixture.close()
    }
  }
}

class CaseClassFixture[T <: CaseClassFixture[T]] extends Fixture[T] {
  def apply[R: AsResult](f: T => R)  = {
    AsResult( f ( this.asInstanceOf[T] ) )
  }

}
