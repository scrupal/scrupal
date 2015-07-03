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

package scrupal.test


import com.typesafe.config.ConfigFactory

import java.util.concurrent.atomic.AtomicInteger

import org.specs2.execute.{Failure, Result}

import play.api.Configuration
import play.api.mvc.{Request, AnyContent}

import scrupal.api._
import scrupal.storage.api.{SchemaDesign, Schema, StoreContext}
import scrupal.utils.ConfigHelpers

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration._


/** One line sentence description here.
  * Further description here.
  */
abstract class ScrupalApiSpecification(val specName : String, timeout : FiniteDuration = Duration(5, "seconds"))
  extends ScrupalSpecification(specName) {

  // WARNING: Do NOT put anything but def and lazy val because of DelayedInit or app startup will get invoked twice
  // and you'll have a real MESS on your hands!!!! (i.e. no db interaction will work!)

  lazy val testScrupal: Scrupal = FakeScrupal(
    ScrupalSpecification.next(specName), Map.empty[String,AnyRef]
  )

  implicit lazy val scrupal : Scrupal = testScrupal

  implicit lazy val site : Site = FakeSite(specName)(testScrupal)

  implicit lazy val context = Context(testScrupal, site)

  def withExecutionContext[T](f : ExecutionContext ⇒ T) : T = scrupal.withExecutionContext[T](f)

  def withStoreContext[T](f : StoreContext ⇒ T) : T =  scrupal.withStoreContext[T](f)

  def withSchema[T](schemaName : String)(f : Schema ⇒ T) : T =  scrupal.withSchema(schemaName)(f)

  def ensureSchema[T](d: SchemaDesign)(f : Schema ⇒ T) : Future[T] = {
    testScrupal.withExecutionContext { implicit ec : ExecutionContext ⇒
      scrupal.withStoreContext { sc : StoreContext ⇒
        sc.ensureSchema(d).map { schema: Schema ⇒ f(schema) }
      }
    }
  }
}

trait ProviderTest extends ScrupalApiSpecification {
  def providerTest(provider: Provider, request: Request[AnyContent])(f : Response ⇒ Result) : Result = {
    testScrupal.withExecutionContext { implicit ec: ExecutionContext ⇒
      val reactor = provider.provide.lift(request)
      if (reactor.isEmpty)
        failure(s"No reactor provided by $provider for $request")
      val stim = Stimulus(context, request)
      val future = reactor.get(stim).map { response ⇒
        f(response)
      }
      Await.result(future, 30.second)
    }
  }
}

trait NodeTest extends ScrupalApiSpecification {
  def nodeTest(node: Node)(f : Response ⇒ Result) : Result = {
    testScrupal.withExecutionContext { implicit ec: ExecutionContext ⇒
      val future = node.apply(context) map { response : Response ⇒
        f(response)
      }
      Await.result(future, 30.second)
    }
  }
}

object ScrupalApiSpecification {

  val counter = new AtomicInteger(0)

  def next(name: String): String = name + "-" + counter.incrementAndGet()

  def storageTestConfig(name: String): Option[Configuration] = {
    Some(
      ConfigHelpers.default() ++ Configuration(
        ConfigFactory.parseString(
          s"""storage {
             | scrupal {
             |   user :"",
             |   pass :"",
             |   uri  :"scrupal-mem://localhost/$name"
             | }
             |}""".stripMargin
        )
      )
    )
  }
}
