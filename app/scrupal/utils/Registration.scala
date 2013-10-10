/**
 * Copyright 2012 Jorge Aliss (jaliss at gmail dot com) - twitter: @jaliss
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package scrupal.utils

import scala.collection.mutable.HashMap
import scala.util.Random
import play.api.Logger


/**
 * Mix this in to anything you want to register and define the "id"; then pass that object to the Registrar you want
 */
trait Registrable {
  def registration_id: Symbol
}

/**
 * A trait for specifying the registration of some type of object (T) which must have at least Registrable mixed in.
 * This abstracts the notion of a registry of objects that conform to an interface.
 * @tparam T
 */
trait Registry[T <: Registrable] {
  protected val registryName = "Registry"
  protected val registrantsName = "Registrable"
  protected val registrants = new HashMap[Symbol, T]()

  def register(thing : T) : Unit = {
    Logger.debug("Registering " + thing.getClass().getCanonicalName() + " as " + thing.registration_id.name + " with registrants = " +
      registrants.map( x => x._1 ))
    if (registrants.contains(thing.registration_id)) {
      throw new IllegalArgumentException(
        "There is already a %s named %s registered with %s".format(registrantsName, thing.registration_id.name, registryName)
      )
    }
    registrants.put(thing.registration_id, thing)
  }

  def isRegistered(name: Symbol) = registrants.contains(name)
  def unRegister(thing: T) = registrants -= thing.registration_id
  def getRegistrant(name: Symbol) : T  = {
    registrants.get(name).getOrElse {
      throw new IllegalArgumentException(
        "There is no %s named %s registered with %s".format(registrantsName, name, registryName)
      )
    }
  }

  val rand = new Random(System.currentTimeMillis())

  def size = registrants.size

  def pick : T = {
    val random_index = rand.nextInt(registrants.size)
    val key : Symbol = registrants.keySet.toArray.apply(random_index)
    getRegistrant(key)
  }
}


