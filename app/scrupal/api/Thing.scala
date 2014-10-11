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

trait StorableThing extends Storable with Equals {
  def canEqual(other: Any) : Boolean = other.isInstanceOf[StorableThing]
}

/** A Storable with a name and description
  *
  */
trait NamedDescribedThing extends StorableThing with Nameable with Describable

/** A Storable that just has an id and a timestamp
  *
  */
trait CreatableThing extends StorableThing with Creatable

trait ModifiableThing extends CreatableThing with Modifiable

/** The most basic thing Scrupal can represent: Identifiable, Creatable, and Nameable.
  * Notably, these are immutable things because we don't keep track of a modification time stamp. The only storage
  * operations permitted are create, read and delete (CRD, not CRUD).
  *
  */
trait ImmutableThing extends CreatableThing  with Nameable {
  override def canEqual(other: Any) : Boolean = other.isInstanceOf[ImmutableThing]
  override def equals(other: Any) : Boolean = {
    other match {
      case that: ImmutableThing => { (this eq that) || (
        that.canEqual(this) &&
          ( this.isIdentified == that.isIdentified) &&
          ( this.isNamed == that.isNamed) &&
          ( ! this.isIdentified || this.id.equals(that.id)) &&
          this.created.equals(that.created) &&
          ( ! this.isNamed || this.name.equals(that.name))
        )
      }
      case _ => false
    }
  }
}

trait MutableThing extends ModifiableThing with Nameable

/** An `ImmutableThing` with a description.
  * Just adds a short textual description of the thing.
  *
  */
trait DescribableImmutableThing extends ImmutableThing with Describable
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
  */
trait Thing extends MutableThing with Describable {
  override def canEqual(other: Any) : Boolean = other.isInstanceOf[Thing]
  override def equals(other: Any) : Boolean = {
    other match {
      case that: Thing => { (this eq that) || (
        that.canEqual(this) && super.equals(that) &&
          ( this.isModified == that.isModified) &&
          ( ! this.isModified || this.modified.get.equals(that.modified.get))
        )
      }
      case _ => false
    }
  }
}

trait EnablableThing extends Thing with Enablable {
  override def canEqual(other: Any) : Boolean = other.isInstanceOf[EnablableThing]
  override def equals(other: Any) : Boolean = {
    other match {
      case that: EnablableThing => { (this eq that) || (
        that.canEqual(this) && super.equals(that) &&
          ( this.enabled == that.enabled)
        )
      }
      case _ => false
    }
  }
}

