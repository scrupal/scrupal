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
import play.api.{Environment, Configuration}
import scrupal.api.{ConfiguredAssetsLocator, AssetsLocator, Site, Scrupal}
import scrupal.storage.api.{Storage, StoreContext}

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._

class FakeScrupal(
  name : String,
  config_overrides : Map[String,AnyRef]) extends Scrupal(name) {

  implicit val _configuration : Configuration = {
    Configuration.load(Environment.simple(), config_overrides)
  }

  implicit val _executionContext = {
    scala.concurrent.ExecutionContext.Implicits.global
  }

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
