/**********************************************************************************************************************
 * This file is part of Scrupal, a Scalable Reactive Web Application Framework for Content Management                 *
 *                                                                                                                    *
 * Copyright (c) 2015, Reactific Software LLC. All Rights Reserved.                                                   *
 *                                                                                                                    *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance     *
 * with the License. You may obtain a copy of the License at                                                          *
 *                                                                                                                    *
 *     http://www.apache.org/licenses/LICENSE-2.0                                                                     *
 *                                                                                                                    *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed   *
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for  *
 * the specific language governing permissions and limitations under the License.                                     *
 **********************************************************************************************************************/

package scrupal.api

import org.joda.time.DateTime
import org.specs2.execute.{Error, Result}

import scrupal.storage.api._
import scrupal.test.ScrupalSpecification

import scala.concurrent.{Future, Await}
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global


/**
 * Test that our basic abstractions for accessing the database hold water.
 */
case class SomeValue(x: Short, y: Short)

case class TestEntity(
  _id : Identifier,
  name: String,
  description: String,
  testVal : SomeValue,
  modified : Option[DateTime] = None,
  created : Option[DateTime] = None
) extends Storable with Nameable with Describable with Modifiable {
}


object TestSchemaDesign extends SchemaDesign {
  def name: String = "TestSchema"
  def requiredNames: Seq[String] = Seq("test_entities")
  def indicesFor(name: String): Seq[Index] = Seq.empty[Index]
}

class EntitySpec extends ScrupalSpecification("EntitySpec")
{
	val te =  TestEntity('Test, "Test", "This is a test", SomeValue(1,2), None, None)

  def withTestSchema[T <: Result](name: String)(func : Schema ⇒ Future[T]) : Result = {
    val f = Storage.fromURI(s"scrupal-mem://localhost/$name",create=true) flatMap { context ⇒
      context.withSchema(TestSchemaDesign.name) { schema ⇒ func(schema) }
    }
    val g = f.recover { case x: Throwable ⇒
      Error(s"Unexpected exception: ${x.getClass.getSimpleName}: ${x.getMessage}", x)
    }
    Await.result(g, 2.seconds)
  }


  "Entity" should {
		"fail to compare against a non-entity" in {
			val other = "not-matchable"
			te.equals(other) must beFalse
			te.equals(te) must beTrue
		}
    "save, load and delete from DB" in {
      withTestSchema("save-load-delete") { schema ⇒
        schema.withCollection("test_entities") { coll : Collection[TestEntity] ⇒
          coll.insert(te) flatMap { wr ⇒
            wr.isSuccess must beTrue
            coll.fetch(te.getPrimaryId()) map { option ⇒
              (option.nonEmpty must beTrue).toResult
            }
          }
        }
      }
    }
  }
}
