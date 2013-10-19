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
  val id: Symbol
  lazy val label = id.name
}

/**
 * A trait for specifying the registration of some type of object (T) which must have at least Registrable mixed in.
 * This abstracts the notion of a registry of objects that conform to an interface.
 */
trait Registry[R <: Registrable] {
  protected val registryName : String
  protected val registrantsName : String
  private[scrupal] val registrants = new HashMap[Symbol, R]()

  def apply(name: Symbol) : Option[R] = getRegistrant(name)

  def all = registrants.values.toSeq

  def register(thing : R) : Unit = {
    Logger.debug("Registering " + thing.getClass().getCanonicalName() + " as " + thing.id.name + " with " +
      registrants.size + " other " + Pluralizer.pluralize(registrantsName))
    if (registrants.contains(thing.id)) {
      throw new IllegalArgumentException(
        "There is already a %s named %s registered with %s".format(registrantsName, thing.id.name, registryName)
      )
    }
    registrants.put(thing.id, thing)
  }

  def isRegistered(name: Symbol) = registrants.contains(name)
  def unRegister(thing: R) : Unit = registrants.remove( thing.id )
  def getRegistrant(name: Symbol) : Option[R]  = registrants.get(name)

  val rand = new Random(System.currentTimeMillis())

  def size = registrants.size
  def isEmpty = registrants.isEmpty

  def pick : R = {
    val random_index = rand.nextInt(registrants.size)
    val key : Symbol = registrants.keySet.toArray.apply(random_index)
    getRegistrant(key).get
  }
}
