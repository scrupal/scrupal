/**********************************************************************************************************************
 * This file is part of Scrupal a Web Application Framework.                                                          *
 *                                                                                                                    *
 * Copyright (c) 2013, Reid Spencer and viritude llc. All Rights Reserved.                                            *
 *                                                                                                                    *
 * Scrupal is free software: you can redistribute it and/or modify it under the terms                                 *
 * of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License,   *
 * or (at your option) any later version.                                                                             *
 *                                                                                                                    *
 * Scrupal is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied      *
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more      *
 * details.                                                                                                           *
 *                                                                                                                    *
 * You should have received a copy of the GNU General Public License along with Scrupal. If not, see either:          *
 * http://www.gnu.org/licenses or http://opensource.org/licenses/GPL-3.0.                                             *
 **********************************************************************************************************************/
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
