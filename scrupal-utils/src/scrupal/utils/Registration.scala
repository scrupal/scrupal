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

import java.util.concurrent.atomic.AtomicReference

import scala.annotation.tailrec
import scala.util.Random

trait Identifiable {
  def id : Symbol
  def label = id.name
}

/**
 * Mix this in to anything you want to register and define the "id"; then pass that object to the Registrar you want
 */
trait Registrable[T <: Registrable[T]] extends Identifiable {
  def registry : Registry[T]
  def asT : T
  def registryName : String = registry.registryName
  def register() : Unit = registry.register(asT)
  def unregister() : Unit = registry.unregister(asT)
  def isRegistered : Boolean = registry.isRegistered(id)
  override def finalize() : Unit = {
    try {
      unregister()
    } catch {
      case xcptn: Throwable ⇒
        /* ignore exceptions to always garbage collect the object */
        registry.log.trace(
          s"Ignoring failure to unregister $label at finalize so exception doesn't thwart GC. Cause:", xcptn
        )
    }
  }
  register()
}

trait AbstractRegistry[K, V <: AnyRef] {

  def contains(key: K) : Boolean = _registry.contains(key)

  def exists(name: K) : Boolean = _registry.contains(name)

  def containsValue(value: V) : Boolean = _registry.values.exists { item => item == value }

  def lookup(key: K): Option[V] = _registry.get(key)

  def lookupOrElse(key: K, value: V) = _registry.getOrElse(key, value)

  def size : Int = _registry.size

  def isEmpty : Boolean = _registry.isEmpty

  def nonEmpty : Boolean = _registry.nonEmpty

  protected def _all = _registry.values.toSeq

  protected def _registry: Map[K, V] = _registrants.get

  @tailrec
  protected final def _register(key: K, obj: V): obj.type = {
    val reg = _registry
    val new_version = reg + (key → obj)
    if (_registrants.compareAndSet(reg, new_version)) obj
    else _register(key, obj)
  }

  @tailrec
  protected final def _unregister(key: K) : Unit = {
    val reg = _registry
    val new_version = reg - key
    if (!_registrants.compareAndSet(reg, new_version))
      _unregister(key) // Try again in case of concurrency!
  }

  private[this] val _registrants = new AtomicReference(Map.empty[K, V])

}


/**
 * A trait for specifying the registration of some type of object (T) which must have at least Registrable mixed in.
 * This abstracts the notion of a registry of objects that conform to an interface.
 */
trait Registry[T <: Registrable[T]] extends AbstractRegistry[Symbol, T] with ScrupalComponent {
  def registryName : String
  def registrantsName : String

  def isRegistered(name: Symbol) : Boolean = contains(name)
  def getRegistrant(name: Symbol) : Option[T]  = lookup(name)

  def apply(name: Symbol) : Option[T] = lookup(name)

  def all = _all


  def find(ids: Seq[Symbol]) : Seq[T] = {
    all.filter { t ⇒ ids.contains(t.id) }
  }

  def as[T <: Registrable[T]](id: Symbol) : T = {
    this(id) match {
      case Some(typ) => typ.asInstanceOf[T]
      case None => toss(s"Could not find type named '$id'")
    }
  }

  def register(thing : T) : Unit = {
    if (contains(thing.id) ) {
      toss(s"There is already a $registrantsName named ${thing.label} registered with $registryName")
    }
    _register(thing.id,thing)
    log.debug(s"Registered ${thing.getClass.getName} as ${thing.label} with $size other ${Pluralizer.pluralize(registrantsName)}")
  }

  def unregister(thing: T) : Unit = {
    if (!contains(thing.id))
        toss(s"There is no $registrantsName named ${thing.label} registered with $registryName")
    _unregister(thing.id)
    log.debug(s"Unregistered ${thing.getClass.getName} as ${thing.label} with $size other ${Pluralizer.pluralize(registrantsName)}")
  }

  val rand = new Random(System.currentTimeMillis())

  def pick : T = {
    val random_index = rand.nextInt(size)
    val key : Symbol = _registry.keySet.toArray.apply(random_index)
    getRegistrant(key).get
  }
}



