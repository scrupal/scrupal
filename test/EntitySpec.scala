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

import scala.concurrent.ExecutionContext.Implicits.global
import scala.Symbol

import play.api.Logger
import play.api.libs.json.{JsString, JsNumber, JsObject, Json}
import play.api.test._
import play.api.test.Helpers.running

import org.specs2.mutable.Specification

// import scrupal.models.{StorableCompanion, Entity}



/**
 * One line sentence description here.
 * Further description here.
class TestEntity extends Entity
{
  uno(1)
  two("2")
  def one() = get[JsNumber]('one)
  def uno(i : Int) = set[JsNumber]('one, JsNumber(i))
  def two = get[JsString]('two)
  def two(s: String) = set[JsString]('two, JsString(s))
}

object TestEntity extends StorableCompanion[TestEntity]( { () => new TestEntity })
{
  override val collectionName = "test_entities"
  System.out.println("Constructing TestEntity")
}

class EntitySpec extends Specification
{
	val te = new TestEntity()

	"Entity" should {
		"generate plural collection name" in {
			TestEntity.collectionName must equalTo("test_entities")
		}
		"fail to compare against a non-entity" in {
			val other = "not-matchable"
			te.equals(other) must beFalse
			te.equals(te) must beTrue
		}
		"allow reincarnation of Entity Subclass" in {
			val js = te.toJson
      Logger.debug("TestEntity.toJson -> " + Json.prettyPrint(js))
			val cm2 = TestEntity.create(js)
      val js2 = cm2.toJson
      Logger.debug("TestEntity(2).toJson -> " + Json.prettyPrint(js2))
			te.equals(cm2) must beTrue
		}
    "save, load and delete with reactivemongo" in {
      running(FakeApplication()) {
        TestEntity.save(te) map { f =>
          f.ok must beTrue
          val id = te._id
          TestEntity.fetch(id) map { g =>
            g match {
              case None => { failure }
              case thing: Some[TestEntity] => {
                TestEntity.remove(thing.get._id) map { h =>
                h.ok must beTrue}
              }
            }
          }
        }
        success

      }
    }
	}
}
 */
