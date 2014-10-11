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
import reactivemongo.bson.BSONObjectID

/** How Scrupal things are identified.
  * Objects stored in This works for both memory and database objects. The
  * So, in case we ever have to change this, we're calling it an Identifier throughout the api
  */
trait Storable {
  def _id : Option[BSONObjectID] = None
  def id : Symbol
  def isIdentified : Boolean = _id.isDefined
}

/** Something that can be created and keeps track of its modification time.
  * For reasons similar to [[scrupal.api.Storable]], the data provided by this trait is accessible to everyone
  * but mutable by only the scrupal package. This limits the impact of making the created_var a var. Creatable uses
  * the same justifications for this design as does [[scrupal.api.Storable]]
  */
trait Creatable  {
  def created : Option[DateTime] = None;
  def isCreated = created.isDefined
  def exists = isCreated
}

/** Something that can be modified and keeps track of its time of modification
  * For reasons similar to [[scrupal.api.Storable]], the data provided by this trait is accessible to everyone
  * but mutable by only the scrupal package. This limits the impact of making the created_var a var. Modifiable uses
  * the same justifications for this design as does [[scrupal.api.Storable]]
  */
trait Modifiable  {
  def modified : Option[DateTime] = None
  def isModified = modified.isDefined
  def changed = isModified
}

/** Something that can be named with a Symbol  */
trait Nameable  {
  val name : Symbol
  def isNamed : Boolean = ! name.name.isEmpty
}

/** Something that has a short textual description */
trait Describable  {
  def description : String
  def isDescribed : Boolean = ! description.isEmpty
}

trait Enablable  {
  def enabled : Boolean
}

trait CreatableModifiableNameableDescribable
  extends Storable
          with Creatable
          with Modifiable
          with Nameable
          with Describable

trait CreatableModifiableNameableDescribableEnablable extends CreatableModifiableNameableDescribable with Enablable
