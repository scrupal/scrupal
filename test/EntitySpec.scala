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

package scrupal.models.test

import org.specs2.mutable._
import scrupal.models.Entity
import play.api.Logger
import play.api.libs.json.Json

/**
 * One line sentence description here.
 * Further description here.
 */
case class TestEntity(one : Int = 1, two: String = "2") extends Entity
object TestEntity { implicit val teFormatter = Json.format[TestEntity] }

class EntitySpec extends Specification
{
	val te = new TestEntity()

	"Entity" should {
		"generate plural collection name" in {
			te.collectionName must equalTo("TestEntities")
		}
		"fail to compare against a non-entity" in {
			val other = "not-matchable"
			te.equals(other) must beFalse
			te.equals(te) must beTrue
		}
		"allow reincarnation of Entity Subclass" in {
			val js = Json.toJson[TestEntity](te)
      Logger.debug("TestEntity.toJson -> " + Json.prettyPrint(js))
			val cm2 : TestEntity = Json.fromJson[TestEntity](js).get
      val js2 = Json.toJson[TestEntity](cm2)
      Logger.debug("TestEntity(2).toJson -> " + Json.prettyPrint(js2))
			te.equals(cm2) must beTrue
		}
	}
}
