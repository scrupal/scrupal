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

import javax.inject.{Provider, Inject}

import org.specs2.specification.core.Fragments
import play.api.inject._
import play.api.inject.guice.GuiceApplicationBuilder
import play.api._
import play.api.mvc.{RequestHeader, Handler}
import play.api.routing.Router

import scala.runtime.AbstractPartialFunction

trait OneAppPerSpec extends ScrupalSpecification { self : ScrupalSpecification ⇒

  def fakeApplication() : Application = fakeApplicationBuilder().build()
  def fakeApplicationBuilder(
    path: java.io.File = new java.io.File("."),
    classloader: ClassLoader = classOf[OneAppPerSpec].getClassLoader,
    additionalConfiguration: Map[String, _ <: Any] = Map.empty,
    withRoutes: PartialFunction[(String, String), Handler] = PartialFunction.empty,
    router : Router = null
  ) : GuiceApplicationBuilder = {
    val bldr = new GuiceApplicationBuilder()
      .in(Environment(new java.io.File("."), this.getClass.getClassLoader, Mode.Test))
      .configure(additionalConfiguration)
      .bindings(
        bind[_root_.scrupal.api.Scrupal] to testScrupal
      )
    if (router != null) {
      bldr.bindings(bind[Router].to(router))
    } else if (withRoutes != null && withRoutes != PartialFunction.empty) {
      bldr
        .bindings(
          bind[FakeRouterConfig] to FakeRouterConfig(withRoutes)
        )
        .overrides(
          bind[play.api.routing.Router].toProvider[FakeRouterProvider]
        )
    } else {
      bldr
    }
  }

  /** Override app if you need a FakeApplication with other than default parameters. */
  implicit lazy val application : Application = fakeApplication()

  private def startRun() = {
    Play.start(application)
  }

  protected def beforeAll() = {}

  protected def afterAll() = {}

  private def stopRun() = {
    Play.stop(application)
  }

  override def map(fs : ⇒ Fragments) = {
    step(startRun()) ^ step(beforeAll()) ^ fs ^ step(afterAll()) ^ step(stopRun())
  }
}

private case class FakeRouterConfig(withRoutes: PartialFunction[(String, String), Handler])

private class FakeRoutes(
  injected: PartialFunction[(String, String), Handler], fallback: Router) extends Router {
  def documentation = fallback.documentation
  // Use withRoutes first, then delegate to the parentRoutes if no route is defined
  val routes = new AbstractPartialFunction[RequestHeader, Handler] {
    override def applyOrElse[A <: RequestHeader, B >: Handler](rh: A, default: A => B) =
      injected.applyOrElse((rh.method, rh.path), (_: (String, String)) => default(rh))
    def isDefinedAt(rh: RequestHeader) = injected.isDefinedAt((rh.method, rh.path))
  } orElse new AbstractPartialFunction[RequestHeader, Handler] {
    override def applyOrElse[A <: RequestHeader, B >: Handler](rh: A, default: A => B) =
      fallback.routes.applyOrElse(rh, default)
    def isDefinedAt(x: RequestHeader) = fallback.routes.isDefinedAt(x)
  }
  def withPrefix(prefix: String) = {
    new FakeRoutes(injected, fallback.withPrefix(prefix))
  }
}

private class FakeRouterProvider @Inject() (config: FakeRouterConfig, parent: RoutesProvider) extends Provider[Router] {
  lazy val get: Router = new FakeRoutes(config.withRoutes, parent.get)
}

