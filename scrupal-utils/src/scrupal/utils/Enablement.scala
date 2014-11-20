/**********************************************************************************************************************
 * Copyright © 2014 Reactific Software, Inc.                                                                          *
 *                                                                                                                    *
 * This file is part of Scrupal, an Opinionated Web Application Framework.                                            *
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

import scala.collection.mutable

/** Enablement Trait For Tracking Enable/Disable Status of Enablees.
  *
  * This is intended to be mixed in to some container of other Enablement objects in a hierarchy that eventually
  * contains Enablees, or has them contained at any level. Enablement objects can also bee Enablees. For example,
  * a Module contains Features and Entities that are both Enablees so it is an Enablement object and it tracks the
  * enablement status of its enablees.  But the Module can be enabled/disabled itself so it can also be an Enablee
  *
  *
  */

trait Enablement[T <: Enablement[T]] extends Registrable[T] with ScrupalComponent {

  private val _enabled = new  AbstractRegistry[Enablee,mutable.HashSet[AnyRef]] {
    def register(key: Enablee, obj: mutable.HashSet[AnyRef]) = _register(key, obj)
    def unregister(key: Enablee) = _unregister(key)
    def keys = _keys
    def registry = _registry
  }

  def isChildScope(scope: Enablement[_]) : Boolean

  def isEnabled(enablee: Enablee, forScope: Enablement[_] = this) : Boolean = {
    if (forScope != this && !isChildScope(forScope))
      toss(s"Scope ${forScope.id} is not a child of $id so enablement for $enablee cannot be determined.")
    _enabled.lookup(enablee) match {
      case Some(set) ⇒
        set.contains(forScope) && (
          enablee.parent match {
            case Some(e) ⇒
              e.isEnabled(forScope)
            case None ⇒ true
          }
        )
      case None ⇒ false
    }
  }

  def enable(enablee: Enablee, forScope: Enablement[_] = this) : Unit = {
    if (forScope != this && !isChildScope(forScope))
      toss(s"Scope ${forScope.id} is not a child of $id so $enablee cannot be enabled for it.")
    val update_value : mutable.HashSet[AnyRef] = _enabled.lookup(enablee) match {
      case Some(set) ⇒ set + forScope
      case None ⇒ mutable.HashSet(forScope)
    }
    _enabled.register(enablee, update_value)
  }

  def disable(enablee: Enablee, forScope: Enablement[_] = this) : Unit = {
    if (forScope != this && !isChildScope(forScope))
      toss(s"Scope ${forScope.id} is not a child of $id so $enablee cannot be disabled for it.")
    _enabled.lookup(enablee) match {
      case Some(set) ⇒
        val update_value : mutable.HashSet[AnyRef] = set - forScope
        if (update_value.isEmpty)
          _enabled.unregister(enablee)
        else
          _enabled.register(enablee, update_value)
      case None ⇒
        log.debug(s"Attempt to disable $enablee that isn't enabled.")
    }
  }

  def forEach[C <: Enablee,R](p: Enablee ⇒ Boolean)(f : C ⇒ R) : Seq[R] = {
    for (e ← _enabled.keys if p(e)) yield { f(e.asInstanceOf[C]) }
  }.toSeq

  def forEachEnabled[R](f : Enablee ⇒ R) : Seq[R] = forEach( e ⇒ isEnabled(e))(f)

  def getEnablementMap : Map[Enablee,Seq[Enablement[_]]] = {
    _enabled.registry.map { case (k,v) ⇒ k -> v.toSeq.map { ar ⇒ ar.asInstanceOf[Enablement[_]] } }
  }

}

/** Something that can be enabled or disabled.
  *
  * Enablee objects have parents and the Enablee is only enabled in some Enablement scope if its parent is too. So
  * if you disable a module in a particular scope, all of its features and entities become disabled in that scope too
  * even without adjusting their enablement status. If the module is re-enabled, it comes back with the previous
  * enablement status at the lower level (entities and features).
  *
  * Note that enablement is NOT recorded in the Enablee directly even though the convenience methods might seem to
  * indicate that. All the enabled/disabled status is recorded in the Enablement objects.
  */
trait Enablee extends Identifiable {
  def parent : Option[Enablee] = None
  def isEnabled(scope: Enablement[_]) : Boolean = { scope.isEnabled(this) }
  def isEnabled(scope: Enablement[_], how: Boolean): Boolean = { scope.isEnabled(this) == how }
  def enable(scope: Enablement[_]) : this.type = { scope.enable(this); this  }
  def enable(scope: Enablement[_], how: Boolean) : this.type = {
    how match {
      case true ⇒ scope.enable(this)
      case false ⇒ scope.disable(this)
    }
    this
  }
  def disable(scope: Enablement[_]) : this.type = { scope.disable(this); this }
}

