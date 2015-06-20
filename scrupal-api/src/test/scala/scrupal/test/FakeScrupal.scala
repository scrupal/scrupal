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

import akka.actor.ActorSystem
import play.api.Configuration
import scrupal.api.{Response, Reaction, Site, Scrupal}
import scrupal.storage.api.StoreContext

import scala.concurrent.{Future, ExecutionContext}

class FakeScrupal(
  name : String,
  config : Option[Configuration],
  ec : Option[ExecutionContext] ,
  sc : Option[StoreContext] ,
  actSys : Option[ActorSystem]) extends Scrupal(name, config, ec, sc, actSys) {

  def Copyright: String = "Copyright 2015, Reactific Software LLC"

  def dispatch(reaction: Reaction): Future[Response] = {
    reaction()
  }

  protected def load(config: Configuration, context: StoreContext): Future[Map[String, Site]] = {
    Future.successful ( Map.empty[String, Site] )
  }

  def onStart(): Unit = {}

  def close(): Unit = {}

  /** Called before the application starts.
    *
    * Resources managed by plugins, such as database connections, are likely not available at this point.
    *
    */
  def open(): (Configuration, StoreContext) = { _configuration → _storageContext }

}

object FakeScrupal {
  def apply(   nm : String = "Scrupal",
    config : Option[Configuration] = None,
    ec : Option[ExecutionContext] = None,
    sc : Option[StoreContext] = None,
    actSys : Option[ActorSystem] = None
    ) : FakeScrupal = {
    new FakeScrupal(nm, config, ec, sc, actSys)
  }
}