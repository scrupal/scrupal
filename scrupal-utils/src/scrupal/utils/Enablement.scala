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

/**
 * Created by reid on 11/14/14.
 */

trait EnablementScope extends AbstractRegistry[Enablee,mutable.HashSet[EnablementScope]] {
  def id : Symbol
  def children : Seq[EnablementScope] = Seq.empty[EnablementScope]

  def isEnabled(enablee: Enablee, forScope: EnablementScope = this) : Boolean = {
    if (forScope != this && !children.contains(forScope))
      toss(s"Scope ${forScope.id} is not a child of ${id} so enablement for $enablee cannot be determined.")
    lookup(enablee) match {
      case Some(set) ⇒ set.contains(forScope)
      case None ⇒ false
    }
  }

  def enable(enablee: Enablee, forScope: EnablementScope = this) : Unit  = {
    if (forScope != this && !children.contains(forScope))
      toss(s"Scope ${forScope.id} is not a child of ${id} so $enablee cannot be enabled for it.")
    val update_value = lookup(enablee) match {
      case Some(set) ⇒ set + forScope
      case None ⇒ mutable.HashSet(forScope)
    }
    _register(enablee, update_value)
  }

  def disable(enablee: Enablee, forScope: EnablementScope = this) : Unit = {
    if (forScope != this && !children.contains(forScope))
      toss(s"Scope ${forScope.id} is not a child of ${id} so $enablee cannot be disabled for it.")
    lookup(enablee) match {
      case Some(set) ⇒
        val update_value = set - forScope
        if (update_value.isEmpty)
          _unregister(enablee)
        else
          _register(enablee, update_value)
      case None ⇒
        log.debug(s"Attempt to disable $enablee that wasn't enabled.")
    }
  }
}

trait Enablee {
  def parent : Option[Enablee] = None
  def isEnabled(scope: EnablementScope) : Boolean = {
    scope.isEnabled(this) && (parent match { case Some(e) ⇒ e.isEnabled(scope) ; case None ⇒ true } )
  }
  def isEnabled(scope: EnablementScope, how: Boolean): Boolean = { scope.isEnabled(this) == how }
  def enable(scope: EnablementScope) : this.type = { scope.enable(this); this  }
  def disable(scope: EnablementScope) : this.type = { scope.disable(this); this }
}

