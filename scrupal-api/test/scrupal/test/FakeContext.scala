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

import org.specs2.execute.AsResult
import org.specs2.specification.Fixture
import scrupal.api._

/**
 * Created by reidspencer on 11/9/14.
 */
class FakeContext[T <: FakeContext[T]](scrupalName: String) extends Context with Fixture[T] with AutoCloseable {
  val scrupal = new Scrupal(scrupalName)
  def nm(name: String = scrupalName) = name + FakeContext.counter.incrementAndGet()
  def sym = Symbol(nm(scrupalName))

  scrupal.open()
  override val site = Some(BasicSite(Symbol(scrupalName+"-Site"), "ContextSite", "Just For Testing", nm("localhost")))
  def close() = { scrupal.close() }
  def apply[R: AsResult](f: T => R) = {
    try {
      val result = f(this.asInstanceOf[T])
      AsResult(result)
    } finally {
      close()
    }
  }
}

object FakeContext {
  def apply(name: String) = new FakeContext(name)
  def fixture(name: String) = new FakeContext(name)
  val counter = new AtomicInteger(0)

}
