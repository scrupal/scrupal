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

package scrupal.api

import org.joda.time.DateTime

/**
 * A piece of information that can be created within Scrupal.
 * Creatable maintain an identifier (long value) that is unique for all values of the Creatable's leaf type.
 */
trait Creatable {
  val id : Option[Long] = None
  val created : Option[DateTime] = None
  def exists = id.isDefined && created.isDefined
}

/**
 * A trait for
 */
trait Modifiable {
  val modified : Option[DateTime] = None
  def isModified = modified.isDefined
}

trait Nameable {
  val name : Symbol
  def isNamed : Boolean = ! name.name.isEmpty
}

trait Describable {
  val description : String
  def isDescribed : Boolean = ! description.isEmpty
}

abstract class CreatableThing(
  override val id : Option[Long] = None,
  override val created : Option[DateTime] = Some(DateTime.now())
) extends Creatable

abstract class Thing(
  override val name: Symbol,
  override val description: String
) extends CreatableThing with Modifiable with Nameable with Describable



