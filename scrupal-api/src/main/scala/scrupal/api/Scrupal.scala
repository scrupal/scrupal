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

import akka.actor.ActorSystem
import akka.util.Timeout

import java.lang.Thread.UncaughtExceptionHandler
import java.util.concurrent._
import java.util.concurrent.atomic.AtomicInteger

import play.api.Configuration

import scrupal.storage.api.{Schema, Storage, StoreContext}
import scrupal.utils._

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration._
import scala.util.matching.Regex

abstract class Scrupal(
  val name : String = "Scrupal",
  config : Option[Configuration] = Some(ConfigHelpers.default()),
  ec : Option[ExecutionContext] = None,
  sc : Option[StoreContext] = None,
  actSys : Option[ActorSystem] = None
) extends { final val id : Symbol = Symbol(name); final val registry = Scrupal }
  with ScrupalComponent with AutoCloseable with Authorable with Enablement[Scrupal] with Registrable[Scrupal] {

  val author = "Reactific Software LLC"
  val copyright = "© 2013-2015 Reactific Software LLC. All Rights Reserved."
  val license = OSSLicense.ApacheV2

  val Sites = SitesRegistry()
  val Applications = ApplicationsRegistry()
  val Modules = ModulesRegistry()
  val Entities = EntitiesRegistry()
  val Types = TypesRegistry()
  val Features = FeaturesRegistry()

  implicit protected val _configuration : Configuration = config.getOrElse(ConfigHelpers.default())

  implicit protected val _actorSystem : ActorSystem = actSys.getOrElse(ActorSystem("Scrupal", _configuration.underlying))

  implicit protected val _executionContext : ExecutionContext = ec.getOrElse(getExecutionContext(_configuration))

  implicit val _storeContext : StoreContext = sc.getOrElse(getStoreContext(_configuration))

  implicit val _timeout = Timeout(
    _configuration.getMilliseconds("scrupal.response.timeout").getOrElse(8000L), TimeUnit.MILLISECONDS
  )

  implicit val _assetsLocator : AssetsLocator = new ConfiguredAssetsLocator(_configuration)



  /** Scrupal Thread Factory
    * This thread factory just names and numbers the threads created so we have a monotonically increasing number of
    * threads in the pool. It also ensures these are not Daemon threads and that there is an UncaughtExceptionHandler
    * in place that will log the escaped exception but otherwise take no action. These things help with with
    * identification of the threads during debugging and knowing that we have an escaped exception.
    */
  private object ScrupalThreadFactory extends ThreadFactory {
    val counter = new AtomicInteger(0)
    val ueh = new UncaughtExceptionHandler {
      def uncaughtException(t: Thread, x: Throwable) = {
        log.error("Exception escaped thread: " + t.getName + ", Id: " + t.getId + " error:", x)
      }
    }

    def newThread(r: Runnable) = {
      val result = new Thread(r)
      result.setDaemon(false)
      result.setUncaughtExceptionHandler(ueh)
      val num = counter.incrementAndGet()
      result.setName(s"$name-$num")
      result
    }
  }

  private object ScrupalRejectionHandler extends RejectedExecutionHandler {
    def rejectedExecution(r: Runnable, executor: ThreadPoolExecutor): Unit = {
      log.error(s"Execution rejected for $r in executor $executor")
    }
  }

  private[this] def getExecutionContext(config: Configuration): ExecutionContext = {
    def makeFixedThreadPool(config: Configuration) : ExecutionContext = {
      val numThreads = config.getInt("num-threads").getOrElse(16)
      ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(numThreads))
    }

    def makeWorkStealingPool() : ExecutionContext = {
      ExecutionContext.fromExecutorService(Executors.newWorkStealingPool())
    }

    def makeThreadPoolExecutionContext(config : Configuration) : ExecutionContext = {
      val corePoolSize = config.getInt("core-pool-size").getOrElse(16)
      val maxPoolSize = config.getInt("max-pool-size").getOrElse(corePoolSize*2)
      val keepAliveTime = config.getInt("keep-alive-secs").getOrElse(60)
      val queueCapacity = config.getInt("queue-capacity").getOrElse(maxPoolSize*8)
      val queue = new ArrayBlockingQueue[Runnable](queueCapacity)

      ExecutionContext.fromExecutorService(
        new ThreadPoolExecutor(
          corePoolSize, maxPoolSize, keepAliveTime, TimeUnit.SECONDS, queue, ScrupalThreadFactory,
          ScrupalRejectionHandler)
      )
    }

    config.getString("scrupal.executor") match {
      case Some("akka") ⇒ _actorSystem.dispatcher
      case Some("fixed-thread-pool") ⇒
        makeFixedThreadPool(config.getConfig("fixed-thread-pool").getOrElse(Configuration()))
      case Some("work-stealing-pool") ⇒
        makeWorkStealingPool()
      case Some("thread-pool") ⇒
        makeThreadPoolExecutionContext(config.getConfig("thread-pool").getOrElse(Configuration()))
      case Some("default") ⇒
        makeWorkStealingPool()
      case _ ⇒
        makeWorkStealingPool()
    }
  }

  def getStoreContext(config : Configuration) : StoreContext = {
    val configToSearch = {
      config.getString("scrupal.storage.config.file") match {
        case Some(fileName) ⇒
          ConfigHelpers.from(fileName).getOrElse(config)
        case None ⇒
          config
      }
    }
    val scrupalConfiguration = configToSearch.getConfig("scrupal")
    Await.result(Storage.fromConfiguration(scrupalConfiguration, "scrupal", create=true), 2.seconds)
  }


  // TODO: Decide if assetsLocator is part of API or Core
  // val assetsLocator : AssetsLocator

  def withConfiguration[T](f : (Configuration) ⇒ T) : T = {
    f(_configuration)
  }

  def withStoreContext[T](f : StoreContext ⇒ T) : T = {
    f(_storeContext)
  }

  def withSchema[T](schemaName: String)(f : Schema ⇒ T) : T = _storeContext.withSchema[T](schemaName)(f)

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
  def isReady : Boolean = _configuration.getConfig("scrupal").nonEmpty && Sites.nonEmpty

  def isChildScope(e : Enablement[_]) : Boolean = e match {
    case s : Site ⇒ Sites.containsValue(s)
    case _ ⇒ false
  }

  def authenticate(rc : Context) : Option[Principal] = None

  /** Called before the application starts.
    *
    * Resources managed by plugins, such as database connections, are likely not available at this point.
    *
    */
  def open() : Configuration = {
    // We do a lot of stuff in API objects and they need to be instantiated in the right order,
    // so "touch" them now because they are otherwise initialized randomly as used
    require(Types.registryName == "Types")
    require(Modules.registryName == "Modules")
    require(Sites.registryName == "Sites")
    require(Entities.registryName == "Entities")
    // FIXME: require(Template.registryName == "Templates")
    _configuration
  }

  def close() : Unit

  /** Load the Sites from configuration
    * Site loading is based on the database configuration. Whatever databases are loaded, they are scanned and any
    * sites in them are fetched and instantiated into the memory registry. Note that multiple sites may utilize the
    * same database information. We utilize this to open the database and load the site objects they contain
    * @param config The Scrupal Configuration to use to determine the initial loading
    * @param context The database context from which to load the
    */
  protected def load(config : Configuration, context : StoreContext) : Future[Map[Regex, Site]]

  /** Handle An Action
    * This is the main entry point into Scrupal for processing actions. It very simply forwards the action to
    * the dispatcher for processing and (quickly) returns a future that will be completed when the dispatcher gets
    * around to it. The point of this is to hide the use of actors within Scrupal and have a nice, simple, quickly
    * responding synchronous call in order to obtain the Future to the eventual result of the action.
    * @param action The action to act upon (a Request ⇒ Result[P] function).
    * @return A Future to the eventual Result[P]
    */
  def dispatch(action : Reactor) : Future[Response]

  def onStart() : Unit

}

object Scrupal extends Registry[Scrupal] {
  def registryName = "Scrupalz"
  def registrantsName = "scrupali"

  private[scrupal] def findModuleOnClasspath(name : String) : Option[Module] = {
    None // TODO: Write ClassLoader code to load foreign modules on the classpath - maybe use OSGi ?
  }

}
