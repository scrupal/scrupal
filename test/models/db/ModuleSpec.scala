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

package scrupal.models

import play.api.test.Helpers._
import org.specs2.mutable.Specification
import scala.slick.session.{Session, Database}
import org.joda.time.DateTime
import play.api.Logger

import scrupal.test.{WithDBSession, FakeScrupal}
import scrupal.models.db.Module

/**
 * One line sentence description here.
 * Further description here.
 */
class ModuleSpec extends Specification {

  "Module" should {
    "save to and fetch from the DB" in new WithDBSession {
      import schema._
      val mod = Modules.insert(Module(None, DateTime.now(), "foo", "Test Module", enabled=false))
      mod.label must beEqualTo("foo")
      val mod2 = Modules.fetch(mod.id.get).get
      mod.id must beEqualTo(mod2.id)
    }
  }
}
