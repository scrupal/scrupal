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

package scrupal.core.http

import akka.actor.ActorSystem
import akka.util.Timeout

import com.typesafe.config.ConfigValue

import java.lang.Thread.UncaughtExceptionHandler
import java.util.concurrent._
import java.util.concurrent.atomic.AtomicInteger

import play.api.Configuration
import play.api.inject.ApplicationLifecycle

import scala.collection.immutable.TreeMap
import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.matching.Regex

import scrupal.api._
import scrupal.core.{CoreModule, CoreSchemaDesign}
import scrupal.storage.api.{Storage, Collection, StoreContext}
import scrupal.utils.ConfigHelpers

class CoreScrupal(
  override val name: String = "CoreScrupal",
  config : Configuration,
  lifecycle : ApplicationLifecycle
) extends {
  implicit protected val _configuration: Configuration = config
} with scrupal.api.Scrupal(name) {

  lifecycle.addStopHook { () ⇒
    Future.successful {this.close()}
  }

  // NOTE: Because implicits are used during construction, the order of initialization here is important.

  implicit protected val _executionContext: ExecutionContext = getExecutionContext

  implicit val _assetsLocator: AssetsLocator = getAssetsLocator

  implicit protected val _actorSystem: ActorSystem = getActorSystem

  implicit val _timeout = getTimeout

  implicit val _storeContext: StoreContext = getStoreContext

  protected def getActorSystem: ActorSystem = {
    ActorSystem("Scrupal", _configuration.underlying)
  }

  protected def getTimeout: Timeout = {
    Timeout(
      _configuration.getMilliseconds("scrupal.timeout.response").getOrElse(8000L), TimeUnit.MILLISECONDS
    )
  }

  protected def getAssetsLocator: AssetsLocator = {
    new ConfiguredAssetsLocator(_configuration)
  }

  protected def getStoreContext: StoreContext = {
    val configToSearch: Configuration = {
      _configuration.getConfig("scrupal") match {
        case None ⇒
          toss("Invalid configuration provided to scrupal. No 'scrupal' at top level")
        case Some(config1) ⇒
          config1.getString("storage.config.file") match {
            case Some(configFileName) ⇒
              ConfigHelpers.from(configFileName) match {
                case Some(config2) ⇒
                  config2
                case None ⇒
                  log.warn(s"The configured file name, '$configFileName' did not contain storage configuration. Trying default.")

                  getDefaultStoreConfig(_configuration)
              }
            case None ⇒
              log.warn("The key 'storage.config.file' is missing from scrupal configuration. Trying default.")
              getDefaultStoreConfig(_configuration)
          }
      }
    }
    Await.result(Storage.fromConfiguration(Some(configToSearch), "scrupal", create = true), 2.seconds)
  }

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

  protected def getExecutionContext: ExecutionContext = {

    def makeFixedThreadPool(config: Configuration): ExecutionContext = {
      val numThreads = config.getInt("num-threads").getOrElse(16)
      ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(numThreads))
    }

    def makeWorkStealingPool(): ExecutionContext = {
      ExecutionContext.fromExecutorService(Executors.newWorkStealingPool())
    }

    def makeThreadPoolExecutionContext(config: Configuration): ExecutionContext = {
      val corePoolSize = config.getInt("core-pool-size").getOrElse(16)
      val maxPoolSize = config.getInt("max-pool-size").getOrElse(corePoolSize * 2)
      val keepAliveTime = config.getInt("keep-alive-secs").getOrElse(60)
      val queueCapacity = config.getInt("queue-capacity").getOrElse(maxPoolSize * 8)
      val queue = new ArrayBlockingQueue[Runnable](queueCapacity)

      ExecutionContext.fromExecutorService(
        new ThreadPoolExecutor(
          corePoolSize, maxPoolSize, keepAliveTime, TimeUnit.SECONDS, queue, ScrupalThreadFactory,
          ScrupalRejectionHandler)
      )
    }

    _configuration.getConfig("scrupal.executor") match {
      case Some(conf) ⇒
        conf.getString("type") match {
          case Some("default") ⇒
            makeWorkStealingPool()
          case Some("akka") ⇒
            _actorSystem.dispatcher
          case Some("fixed-thread-pool") ⇒
            makeFixedThreadPool(conf.getConfig("fixed-thread-pool").getOrElse(Configuration()))
          case Some("work-stealing-pool") ⇒
            makeWorkStealingPool()
          case Some("thread-pool") ⇒
            makeThreadPoolExecutionContext(conf.getConfig("thread-pool").getOrElse(Configuration()))
          case Some(s: String) ⇒
            toss("Invalid valid for configuration key 'scrupal.executor.type'")
          case None ⇒
            makeWorkStealingPool()
        }
      case None ⇒
        makeWorkStealingPool()
    }
  }

  def getDefaultStoreConfig(config: Configuration): Configuration = {
    config.getConfig("scrupal.storage.default").getOrElse {
      toss("Scrupal configuration is missing default storage configuration in scrupal.storage.default")
    }
  }

  /** Called before the application starts.
    *
    * Resources managed by plugins, such as database connections, are likely not available at this point.
    *
    */
  override def open(): Configuration = {
    super.open()
    log.debug("Scrupal startup initiated.")

    // Instantiate the core module and make sure that we registered it as 'Core

    val core = CoreModule()(this)
    require(Modules('Core).isDefined, "Failed to find the CoreModule as 'Core")
    core.bootstrap(_configuration)

    val configured_modules: Seq[String] = {
      // FIXME: can we avoid configuring the specific modules here?
      // How about JSR 367?  in JSE 9? https://www.jcp.org/en/jsr/detail?id=376
      _configuration.getStringSeq("scrupal.modules") match {
        case Some(list) ⇒ list
        case None ⇒ List.empty[String]
      }
    }

    // Now we go through the configured modules and bootstrap them
    for (class_name ← configured_modules) {
      scrupal.api.Scrupal.findModuleOnClasspath(class_name) match {
        case Some(module) ⇒ module.bootstrap(_configuration)
        case None ⇒ log.warn("Could not locate module with class name: " + class_name)
      }
    }

    // Load the configuration and wait at most 10 seconds for it
    val future = load(_configuration, _storeContext) map { sites ⇒
      if (sites.isEmpty)
        toss("Refusing to start because of load errors. Check logs for details.")
      else {
        log.info("Loaded Sites:\n" + sites.map { site ⇒ s"${site.label}" })
      }
      log.debug("Scrupal startup completed.")
      _configuration
    }
    Await.result(future, 10.seconds)
  }

  override def close() = {
    log.debug("Scrupal shutdown initiated")
    withExecutionContext { implicit ec: ExecutionContext ⇒
      _actorSystem.shutdown()
      _storeContext.close()
    }
    log.debug("Scrupal shutdown completed")
  }

  override def authenticate(rc: Context): Option[Principal] = None

  type FlatConfig = TreeMap[String, ConfigValue]

  def interestingConfig(config: Configuration): FlatConfig = {
    val elide: Regex = "^(akka|java|sun|user|awt|os|path|line).*".r
    val entries = config.entrySet.toSeq
    val filtered = entries filter { case (x, y) ⇒ !elide.findPrefixOf(x).isDefined }
    TreeMap[String, ConfigValue](filtered.toSeq: _*)
  }

  /** Load the Sites from configuration
    * Site loading is based on the database configuration. Whatever databases are loaded, they are scanned and any
    * sites in them are fetched and instantiated into the memory registry. Note that multiple sites may utilize the
    * same database information. We utilize this to open the database and load the site objects they contain
    * @param config The Scrupal Configuration to use to determine the initial loading
    * @param context The database context from which to load the
    */
  protected def load(config: Configuration, context: StoreContext): Future[Seq[Site]] = {
    val coreSchema = CoreSchemaDesign()
    try {
      context.addSchema(coreSchema) flatMap { schema ⇒
        schema.collectionFor[Site]("sites") match {
          case Some(sitesCollection: Collection[Site]) ⇒ {
            val result = sitesCollection.fetchAll().map {
              sites ⇒ {
                for (site ← sites) yield {
                  log.debug(s"Loading site '${site.name}' for host ${site.hostNames}, enabled=${site.isEnabled(this)}")
                  site.enable(this)
                  site
                }
              }.toSeq
            }
            DataCache.update(this, schema)
            result
          }
          case None ⇒
            toss("Collection 'sites' was not found")
        }
      }
    } catch {
      case x: Throwable ⇒
        log.error("Attempt to validate core schema failed: ", x)
        throw x
    }
  }
}

