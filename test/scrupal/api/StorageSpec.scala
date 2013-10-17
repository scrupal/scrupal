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

package scrupal.api

import org.specs2.mutable.Specification
import play.api.Logger

case class Empty() extends Storable
case class One(o:Int) extends Storable
case class Two(o:Int, s: String) extends Storable

object Store {

  type IdentityFakeStorage[T <: Storable] = scrupal.api.SimpleMemoryStorageFor[T]

  val e = new IdentityFakeStorage[Empty]()
  val o = new IdentityFakeStorage[One]()
  val t = new IdentityFakeStorage[Two]()

}

/** One line sentence description here.
  * Further description here.
  */

class StorageSpec extends Specification {

/*
  "Empty" should {
    "support CRUD operations on FakeStorage" in {
      val e = Empty()
      val e2 = Store.e.insert(e)
      Logger.debug("e.id = " + e.id)
      e.id.get == e2 must beTrue
      val e3 = Store.e.fetch(e2)
      e3.isDefined must beTrue
      e3.get.id == e2 must beTrue
      val e4 = Store.e.update(e3.get)
      e4 must beGreaterThan(0)
      Store.e.delete(e) must beTrue
      Store.e.fetch(e2).isDefined must beFalse
    }
  }

  "One" should {
    "support CRUD operations on FakeStorage" in {
      val e = One(42)
      val e2 = Store.o.insert(e)
      Logger.debug("e.id = " + e.id)
      e.id.get == e2 must beTrue
      val e3 = Store.o.fetch(e2)
      e3.isDefined must beTrue
      e3.get.id.get == e2 must beTrue
      val e4 = Store.o.update(e3.get)
      e4 must beGreaterThan(0)
      Store.o.delete(e) must beTrue
      Store.o.fetch(e2).isDefined must beFalse
    }
  }

  "Two" should {
    "support CRUD operations on FakeStorage" in {
      val e = Two(42, " is the answer!")
      val e2 = Store.t.insert(e)
      Logger.debug("e.id = " + e.id)
      e.id.get == e2 must beTrue
      val e3 = Store.t.fetch(e2)
      e3.isDefined must beTrue
      e3.get.id.get == e2 must beTrue
      val e4 = Store.t.update(e3.get)
      e4 must beGreaterThan(0)
      Store.t.delete(e) must beTrue
      Store.t.fetch(e2).isDefined must beFalse
    }
  }
  */
}
