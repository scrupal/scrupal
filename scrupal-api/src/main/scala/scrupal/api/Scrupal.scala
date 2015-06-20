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

import java.util.concurrent.TimeUnit

import akka.actor.ActorSystem
import akka.util.Timeout
import play.api.Configuration
import scrupal.storage.api.StoreContext
import scrupal.utils._

import scala.concurrent.{ExecutionContext, Future}

trait Scrupal extends ScrupalComponent with AutoCloseable with Enablement[Scrupal] with Registrable[Scrupal] {

  def name : String

  def id : Symbol = Symbol(name)

  def key = ""
  def registry = Scrupal

  def Copyright : String

  implicit val _configuration : Configuration

  implicit val _actorSystem : ActorSystem

  implicit val _timeout = Timeout(
    _configuration.getMilliseconds("scrupal.response.timeout").getOrElse(8000L), TimeUnit.MILLISECONDS
  )

  implicit val _executionContext : ExecutionContext

  val _storageContext : StoreContext

  // TODO: Decide if assetsLocator is part of API or Core
  // val assetsLocator : AssetsLocator

  def withConfiguration[T](f : (Configuration) ⇒ T) : T = {
    f(_configuration)
  }

  def withStorageContext[T](f : StoreContext ⇒ T) : T = {
    f(_storageContext)
  }

  def withExecutionContext[T](f : (ExecutionContext) ⇒ T) : T = {
    f(_executionContext)
  }

  def withActorSystem[T](f : (ActorSystem) ⇒ T) : T = {
    f(_actorSystem)
  }

  def withActorExec[T](f : (ActorSystem, ExecutionContext, Timeout) ⇒ T) : T = {
    f(_actorSystem, _executionContext, _timeout)
  }

  /** Simple utility to determine if we are considered "ready" or not. Basically, if we have a non empty Site
    * Registry then we have had to found a database and loaded the sites. So that is our indicator of whether we
    * are configured yet or not.
    * @return True iff there are sites loaded
    */
  def isReady : Boolean = _configuration.getConfig("scrupal").nonEmpty && Site.nonEmpty

  def isChildScope(e : Enablement[_]) : Boolean = e match {
    case s : Site ⇒ Site.containsValue(s)
    case _ ⇒ false
  }

  def authenticate(rc : Context) : Option[Principal] = None

  /** Called before the application starts.
    *
    * Resources managed by plugins, such as database connections, are likely not available at this point.
    *
    */
  def open() : (Configuration, StoreContext)

  def close() : Unit

  /** Load the Sites from configuration
    * Site loading is based on the database configuration. Whatever databases are loaded, they are scanned and any
    * sites in them are fetched and instantiated into the memory registry. Note that multiple sites may utilize the
    * same database information. We utilize this to open the database and load the site objects they contain
    * @param config The Scrupal Configuration to use to determine the initial loading
    * @param context The database context from which to load the
    */
  protected def load(config : Configuration, context : StoreContext) : Future[Map[String, Site]]

  /** Handle An Action
    * This is the main entry point into Scrupal for processing actions. It very simply forwards the action to
    * the dispatcher for processing and (quickly) returns a future that will be completed when the dispatcher gets
    * around to it. The point of this is to hide the use of actors within Scrupal and have a nice, simple, quickly
    * responding synchronous call in order to obtain the Future to the eventual result of the action.
    * @param action The action to act upon (a Request ⇒ Result[P] function).
    * @return A Future to the eventual Result[P]
    */
  def dispatch(action : Reaction) : Future[Response]

  def onStart() : Unit
}

object Scrupal extends Registry[Scrupal] {
  def registryName = "Scrupalz"
  def registrantsName = "scrupali"

  private[scrupal] def findModuleOnClasspath(name : String) : Option[Module] = {
    None // TODO: Write ClassLoader code to load foreign modules on the classpath - maybe use OSGi ?
  }

}
