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

import java.util.concurrent.TimeUnit

import akka.actor.ActorSystem
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import play.api.Configuration
import scrupal.api.{ConfiguredAssetsLocator, AssetsLocator, Site, Scrupal}
import scrupal.storage.api.{Storage, StoreContext}

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._

class FakeScrupal(
  name : String,
  config_overrides : Map[String,AnyRef]) extends Scrupal(name) {

  implicit val _configuration : Configuration = {
    val default_config = Configuration(
      ConfigFactory.parseString(
        s"""scrupal {
           |  response {
           |    timeout : 16000
           |  }
           |
           |  storage {
           |    config {
           |      file: "conf/storage.conf"
           |    }
           |  }
           |
           |  default {
           |    storage {
           |      scrupal {
           |        user :"",
           |        pass :"",
           |        uri  :"scrupal-mem://localhost/scrupal"
           |      }
           |    }
           |  }
           |
           |  developer {
           |    mode : true
           |    footer: true
           |  }
           |
           |  dispatcher {
           |    # Dispatcher is the name of the event-based dispatcher
           |    type = Dispatcher
           |    # What kind of ExecutionService to use
           |    executor = "fork-join-executor"
           |    # Configuration for the fork join pool
           |    fork-join-executor {
           |      # minimum number of threads to cap factor-based core number to
           |      core-pool-size-min = 2
           |      # No of core threads ... ceil(available processors * factor)
           |      core-pool-size-factor = 2.0
           |      # maximum number of threads to cap factor-based number to
           |      core-pool-size-max = 32
           |    }
           |
           |    # Throughput defines the maximum number of messages to be
           |    # processed per actor before the thread jumps to the next actor.
           |    # Set to 1 for as fair as possible.
           |    throughput = 8
           |  }
           |}""".stripMargin
      )
    )
    val override_config = Configuration.from(config_overrides)
    Configuration(override_config.underlying.withFallback(default_config.underlying))
  }

  implicit val _executionContext = scala.concurrent.ExecutionContext.Implicits.global

  implicit val _storeContext = {
    val configToSearch = _configuration.getConfig("scrupal.storage.default")
    Await.result(Storage.fromConfiguration(configToSearch, "scrupal", create=true), 2.seconds)
  }

  implicit val _actorSystem : ActorSystem = ActorSystem(name, _configuration.underlying)


  protected def load(config: Configuration, context: StoreContext): Future[Seq[Site]] = {
    Future.successful(Seq.empty[Site])
  }

  implicit val _timeout = Timeout(
    _configuration.getMilliseconds("scrupal.response.timeout").getOrElse(16000L), TimeUnit.MILLISECONDS
  )

  implicit val _assetsLocator : AssetsLocator = new ConfiguredAssetsLocator(_configuration)
}


object FakeScrupal {
  def apply(   nm : String = "Scrupal",
    config_overrides : Map[String,AnyRef]) : FakeScrupal = {
    new FakeScrupal(nm, config_overrides)
  }
}
