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

package scrupal.core.http.netty

import com.google.inject.AbstractModule

import com.typesafe.config.ConfigValue

import javax.inject.{Inject, Singleton}

import play.api.Configuration
import play.api.inject.ApplicationLifecycle
import scrupal.api._
import scrupal.core.{CoreSchemaDesign, CoreModule, Core}
import scrupal.core.sites.WelcomeSite
import scrupal.storage.api.{Collection, Schema, StoreContext}
import scrupal.utils.LoggingHelpers

import scala.collection.immutable.TreeMap
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration._

import scala.util.matching.Regex

class ScrupalModule extends AbstractModule {
  def configure() = {
    bind(classOf[scrupal.api.Scrupal])
      .to(classOf[Scrupal]).asEagerSingleton()
  }
}

@Singleton
case class Scrupal @Inject() (
  override val name : String = "Scrupal",
  config : Configuration,
  lifecycle : ApplicationLifecycle
) extends scrupal.api.Scrupal(name, Some(config), None, None, None) {

  lifecycle.addStopHook { () ⇒
    Future.successful { this.close() }
  }

  /** Called before the application starts.
    *
    * Resources managed by plugins, such as database connections, are likely not available at this point.
    *
    */
  override def open() : Configuration = {
    LoggingHelpers.initializeLogging(forDebug = true)

    log.debug("Scrupal startup initiated.")

    // We do a lot of stuff in API objects and they need to be instantiated in the right order,
    // so "touch" them now because they are otherwise initialized randomly as used
    require(Types.registryName == "Types")
    require(Modules.registryName == "Modules")
    require(Sites.registryName == "Sites")
    require(Entities.registryName == "Entities")
    require(Template.registryName == "Templates")

    // Instantiate the core module and make sure that we registered it as 'Core

    val core = CoreModule()(this)
    require (Modules('Core).isDefined, "Failed to find the CoreModule as 'Core")
    core.bootstrap(_configuration)

    val configured_modules : Seq[String] = {
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
        case Some(module) ⇒ module.bootstrap(config)
        case None ⇒ log.warn("Could not locate module with class name: " + class_name)
      }
    }

    // Load the configuration and wait at most 10 seconds for it
    val future = load(_configuration, _storeContext) map { sites ⇒
      if (sites.isEmpty)
        toss("Refusing to start because of load errors. Check logs for details.")
      else {
        log.info("Loaded Sites:\n" + sites.map { site ⇒ s"${site.label}"})
      }
      log.debug("Scrupal startup completed.")
      config
    }
    Await.result(future, 10.seconds)
  }

  override def close() = {
    log.debug("Scrupal shutdown initiated")
    withExecutionContext { implicit ec : ExecutionContext ⇒
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
            sitesCollection.fetchAll().map {
              sites ⇒ {
                for (site ← sites) yield {
                  log.debug(s"Loading site '${site.name}' for host ${site.hostNames}, enabled=${site.isEnabled(this)}")
                  site.enable(this)
                  site
                }
              }.toSeq
            } map { sites ⇒
              if (sites.isEmpty) {
                val ws = new WelcomeSite(Symbol(name + "-Welcome"))(this)
                ws.enable(this)
                DataCache.update(this, schema)
                // AdminApp.enable(ws)
                // CoreModule.enable(AdminApp)
                Seq(ws)
              } else {
                DataCache.update(this, schema)
                sites
              }
            }
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
