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

import org.specs2.execute.AsResult
import org.specs2.specification.Fixture
import scrupal.core.Scrupal
import scrupal.core.api._

/**
 * Created by reidspencer on 11/9/14.
 */
case class FakeContext(scrupalName: String) extends Context {
  val scrupal = new Scrupal(scrupalName)
  scrupal.open()
  override val site = Some(BasicSite(Symbol(scrupalName+"-Site"), "ContextSite", "Just For Testing", "localhost"))
  def close() = { scrupal.close() }
}

object FakeContext {
  def fixture(name: String) = new Fixture[Context] {
    def apply[R: AsResult](f: Context => R) = {
      val ctxt = FakeContext(name)
      try {
        val result = f(ctxt)
        AsResult(result)
      } finally {
        ctxt.close()
      }
    }
  }
}
