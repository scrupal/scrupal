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

abstract class StorableThing (
  override val id: Option[Identifier] = None
) extends NumericIdentifiable with Equals

/** A Storable with a name and description
  *
  * @param name The name of the thing
  * @param description A brief description of the thing
  * @param id The unique identifier
  */
abstract class NamedDescribedThing (
  override val name : Symbol,
  override val description : String,
  override val id : Option[Identifier] = None
) extends StorableThing(id) with NumericNameable with NumericDescribable

/** A Storable that just has an id and a timestamp
  *
  * @param created The time of creation
  * @param id The unique identifier
  */
abstract class CreatableThing (
  override val created : Option[DateTime] = None,
  override val id : Option[Identifier] = None
) extends StorableThing(id) with NumericCreatable

abstract class ModifiableThing (
  override val modified : Option[DateTime] = None,
  override val created : Option[DateTime] = None,
  override val id : Option[Identifier] = None
) extends CreatableThing(created, id) with NumericModifiable

/** The most basic thing Scrupal can represent: Identifiable, Creatable, and Nameable.
  * Notably, these are immutable things because we don't keep track of a modification time stamp. The only storage
  * operations permitted are create, read and delete (CRD, not CRUD).
  *
  * @param name The name of the `BasicThing`
  */
abstract class ImmutableThing (
  override val name: Symbol,
  override val created : Option[DateTime] = None,
  override val id : Option[Identifier] = None
) extends CreatableThing(created, id) with NumericNameable {
  override def canEqual(other: Any) : Boolean = other.isInstanceOf[ImmutableThing]
  override def equals(other: Any) : Boolean = {
    other match {
      case that: ImmutableThing => { (this eq that) || (
        that.canEqual(this) &&
          ( this.isIdentified == that.isIdentified) &&
          ( this.isNamed == that.isNamed) &&
          ( ! this.isIdentified || this.id.get.equals(that.id.get)) &&
          this.created.equals(that.created) &&
          ( ! this.isNamed || this.name.equals(that.name))
        )
      }
      case _ => false
    }
  }
}

abstract class MutableThing (
  override val name: Symbol,
  override val modified : Option[DateTime] = None,
  override val created : Option[DateTime] = None,
  override val id : Option[Identifier] = None
) extends ModifiableThing(modified, created, id) with NumericNameable

/** An `ImmutableThing` with a description.
  * Just adds a short textual description of the thing.
  *
  * @param name The name of the immutable thing
  * @param description A brief description of the immutable thing
  */
abstract class DescribableImmutableThing (
  override val name: Symbol,
  override val description: String,
  override val created : Option[DateTime] = None,
  override val id : Option[Identifier] = None
) extends ImmutableThing(name, created, id) with NumericDescribable
{
  override def canEqual(other: Any) : Boolean = other.isInstanceOf[DescribableImmutableThing]
  override def equals(other: Any) : Boolean = {
    other match {
      case that: DescribableImmutableThing => { (this eq that) || (
        that.canEqual(this) && super.equals(that) &&
          ( this.isDescribed == that.isDescribed) &&
          ( ! this.isDescribed || this.description.equals(that.description))
        )
      }
      case _ => false
    }
  }
}

/** The Basic `Thing` that we can model with Scrupal
  * Generally the things that are interesting are identifiable, creatable, modifiable,
  * named and described. These traits hold for most of the `Thing`s that Scrupal will manipulate.
  * @param name The name of the `Thing`
  * @param description A brief description of the `Thing`
  */
abstract class NumericThing (
  override val name: Symbol,
  override val description: String,
  override val modified : Option[DateTime] = None,
  override val created : Option[DateTime] = None,
  override val id : Option[Identifier] = None
)  extends MutableThing(name, modified, created, id) with NumericDescribable {
  override def canEqual(other: Any) : Boolean = other.isInstanceOf[NumericThing]
  override def equals(other: Any) : Boolean = {
    other match {
      case that: NumericThing => { (this eq that) || (
        that.canEqual(this) && super.equals(that) &&
          ( this.isModified == that.isModified) &&
          ( ! this.isModified || this.modified.get.equals(that.modified.get))
        )
      }
      case _ => false
    }
  }
}

abstract class NumericEnablableThing (
  override val name: Symbol,
  override val description: String,
  val enabled : Boolean = false,
  override val modified : Option[DateTime] = None,
  override val created : Option[DateTime] = None,
  override val id : Option[Identifier] = None
) extends NumericThing(name, description, modified, created, id)  with NumericEnablable {
  override def canEqual(other: Any) : Boolean = other.isInstanceOf[NumericEnablableThing]
  override def equals(other: Any) : Boolean = {
    other match {
      case that: NumericEnablableThing => { (this eq that) || (
        that.canEqual(this) && super.equals(that) &&
          ( this.enabled == that.enabled)
        )
      }
      case _ => false
    }
  }
}

abstract class SymbolicThing (
  override val id: Symbol,
  override val description: String,
  override val modified : Option[DateTime] = None,
  override val created : Option[DateTime] = None
) extends Equals with Describable with Modifiable with Creatable
          with SymbolicIdentifiable {
  override def canEqual(other: Any) : Boolean = other.isInstanceOf[SymbolicThing]
  override def equals(other: Any) : Boolean = {
    other match {
      case that: SymbolicThing => { (this eq that) || (
        that.canEqual(this) && super.equals(that))
      }
      case _ => false
    }
  }
}

abstract class SymbolicEnablableThing (
  override val id: Symbol,
  override val description: String,
  val enabled : Boolean = false,
  override val modified : Option[DateTime] = None,
  override val created : Option[DateTime] = None
) extends SymbolicThing(id, description, modified, created) with Enablable {
  override def canEqual(other: Any) : Boolean = other.isInstanceOf[SymbolicEnablableThing]
  override def equals(other: Any) : Boolean = {
    other match {
      case that: SymbolicEnablableThing => { (this eq that) || (
        that.canEqual(this) && super.equals(that) &&
          ( this.enabled == that.enabled)
        )
      }
      case _ => false
    }
  }
}
