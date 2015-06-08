/** ********************************************************************************************************************
  * This file is part of Scrupal, a Scalable Reactive Content Management System.                                       *
  *                                                                                                                  *
  * Copyright © 2015 Reactific Software LLC                                                                            *
  *                                                                                                                  *
  * Licensed under the Apache License, Version 2.0 (the "License");  you may not use this file                         *
  * except in compliance with the License. You may obtain a copy of the License at                                     *
  *                                                                                                                  *
  *      http://www.apache.org/licenses/LICENSE-2.0                                                                  *
  *                                                                                                                  *
  * Unless required by applicable law or agreed to in writing, software distributed under the                          *
  * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,                          *
  * either express or implied. See the License for the specific language governing permissions                         *
  * and limitations under the License.                                                                                 *
  * ********************************************************************************************************************
  */
package scrupal.utils

import java.util.concurrent.atomic.AtomicReference

import scala.annotation.tailrec
import scala.util.Random

trait Identifiable {
  def id : Symbol
  lazy val label = id.name
}

trait IdentifiedWithRegistry extends Identifiable {
  def registry : Registry[_]
  def registryName : String = registry.registryName
}

/** Mix this in to anything you want to register and define the "id"; then pass that object to the Registrar you want
  */
trait Registrable[T <: Registrable[T]] extends IdentifiedWithRegistry { self : T ⇒
  def registry : Registry[T]
  def register() : Unit = registry.register(self)
  def unregister() : Unit = registry.unregister(self)
  def isRegistered : Boolean = registry.isRegistered(id)
  override def finalize() : Unit = {
    try {
      unregister()
    } catch {
      case xcptn : Throwable ⇒
        /* ignore exceptions to always garbage collect the object */
        registry.log.trace(
          s"Ignoring failure to unregister $label at finalize so exception doesn't thwart GC. Cause:", xcptn
        )
    }
  }
  register()
}

trait AbstractRegistry[K, V <: AnyRef] extends ScrupalComponent {
  def contains(key : K) : Boolean = _registry.contains(key)

  def exists(name : K) : Boolean = _registry.contains(name)

  def containsValue(value : V) : Boolean = _registry.values.exists { item ⇒ item == value }

  def lookup(key : K) : Option[V] = _registry.get(key)

  def lookupOrElse(key : K, value : V) = _registry.getOrElse(key, value)

  def size : Int = _registry.size

  def isEmpty : Boolean = _registry.isEmpty

  def nonEmpty : Boolean = _registry.nonEmpty

  protected def _values = _registry.values.toSeq

  protected def _keys = _registry.keys

  protected def _registry : Map[K, V] = _registrants.get

  @tailrec
  protected final def _register(key : K, obj : V) : obj.type = {
    val reg = _registry
    val new_version = reg + (key → obj)
    if (_registrants.compareAndSet(reg, new_version))
      obj
    else {
      lookup(key) match {
        case Some(value) ⇒
          if (obj != value)
            toss (s"Insertion of $key was preempted by another thread. Fix your race condition.")
          else {
            log.warn(s"Preemptive replacement of $key yielding same value is indicative of race condition.")
            obj
          }
        case None ⇒
          _register(key, obj) // iterate by tail recursion until we succeed at registration without being pre-empted
      }
    }
  }

  @tailrec
  protected final def _unregister(key : K) : Unit = {
    val reg = _registry
    val new_version = reg - key
    if (!_registrants.compareAndSet(reg, new_version))
      if (!_registry.contains(key))
        toss (s"Removal of $key was preempted by another thread. Fix your race condition.")
      else
        _unregister(key) // Try again in case of concurrency!
  }

  private[this] val _registrants = new AtomicReference(Map.empty[K, V])

}

/** A trait for specifying the registration of some type of object (T) which must have at least Registrable mixed in.
  * This abstracts the notion of a registry of objects that conform to an interface.
  */
trait Registry[T <: Registrable[T]] extends AbstractRegistry[Symbol, T] {
  def registryName : String
  def registrantsName : String
  override def logger_identity = s"${registryName}Registry"

  def isRegistered(name : Symbol) : Boolean = contains(name)
  def getRegistrant(name : Symbol) : Option[T] = lookup(name)

  def apply(name : Symbol) : Option[T] = lookup(name)

  def values = _values

  def keys = _keys

  def find(ids : Seq[Symbol]) : Seq[T] = {
    values.filter { t ⇒ ids.contains(t.id) }
  }

  def as[U <: Registrable[U]](id : Symbol) : U = {
    this(id) match {
      case Some(typ) ⇒ typ.asInstanceOf[U]
      case None ⇒ toss(s"Could not find $registrantsName named '$id'")
    }
  }

  def register(thing : T) : Unit = {
    val theName = thing.id.toString() // If id is null, let's fail before we do anything
    if (contains(thing.id)) {
      toss(s"There is already a $registrantsName named $theName registered with $registryName")
    }
    _register(thing.id, thing)
    log.trace(
      s"Registered ${thing.getClass.getName} as $theName with $size other ${Pluralizer.pluralize(registrantsName)}")
  }

  def unregister(thing : T) : Unit = {
    val theName = thing.id.toString // If id is null, let's fail before we do anything
    if (!contains(thing.id))
      toss(s"There is no $registrantsName named $theName registered with $registryName")
    _unregister(thing.id)
    log.trace(
      s"Unregistered ${thing.getClass.getName} as $theName with $size other ${Pluralizer.pluralize(registrantsName)}")
  }

  val rand = new Random(System.currentTimeMillis())

  def pick : T = {
    val random_index = if (size == 0) toss("Can't pick from an empty registry") else rand.nextInt(size)
    val key : Symbol = _registry.keySet.toArray.apply(random_index)
    getRegistrant(key).get
  }
}

