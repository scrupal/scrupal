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

package scrupal.core.akkahttp

import akka.actor.ActorSystem
import akka.http.scaladsl.model.MediaType
import scredis._
import scrupal.api.Scrupal

import scala.concurrent.duration.Duration
import scala.concurrent.{ExecutionContext, Future}

trait Cacheable[PT] {
  def mediaType : MediaType
  def payload : PT
  val ttl : Duration = Duration.Inf // non-expiring, doesn't time out after fixed amount of time after creation
  val tti : Duration = Duration.Inf // non-expiring, doesn't time out if not referenced
}

/** Redis Based Cache
  * This is a thread-safe, non-blocking, asynchronous Spray cache that uses Redis as its backing store. The interface
  * to Redis is non-blocking because we use Livestream's scredis interface to Redis. Cache entries are Cacheable
  * objecst that have an Array[Byte] payload and a media type. If expiration of the data is wanted, set the ttl field
  * and the EXPIRE command will be sent to Redis to expire it after that duration.
  *
  * TODO: Figure out a way to do time-to-idle using Redis `OBJECT IDLETIME <key>` command (responsibly).
  * Essentially we want an actor that once in a while (hourly?) goes through the cache or some fragment of
  * it and evicts things that have not been accessed in a while.
  * Created by reidspencer on 11/8/14.
  */
class ScredisCache(scrupal : Scrupal) { // FIXME: What does akka-http use for caching ? extends Cache[Cacheable[_]] {

  // TODO: Implement ScredisCache

  implicit val system = ActorSystem("scredis")

  val client = scrupal.withConfiguration {
    configuration ⇒
      configuration.getConfig("scredis") match {
        case Some(config) ⇒ Client(config.underlying)
        case None ⇒ Client("scredis")
      }
  }

  def get(key : Any) : Option[Future[Cacheable[_]]] = ???

  def apply(key : Any, genValue : () ⇒ Future[Cacheable[_]])(implicit ec : ExecutionContext) : Future[Cacheable[_]] = ???

  def remove(key : Any) = ???

  def clear() : Unit = ???

  def keys : Set[Any] = ???

  def ascendingKeys(limit : Option[Int] = None) = ???

  def size = ???
}
