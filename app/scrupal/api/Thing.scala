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
import play.api.libs.json.{JsObject, JsValue}

/** Something that is identifiable by a long integer as a unique ID within some realm (e.g. a database) */
trait Identifiable[T <: Identifiable[T]] {
  val id: Option[Long] = None
  def isIdentified = id.isDefined

  /** Abstract method to allow subclasses to create the leaf class (T) with a new id. This supports the update of
    * Things after they've been created into the Storage system and their ID is consequently known.
    * @param id The new identifier for the object
    * @return A new instance of type T of this Thing with the new identifier in Identifiable.id field
    */
  def forId(id : Long) : T

  /** All things are inherently convertible to Json.
    * We allow each subclass to define the most efficient way to convert itself to Json. Only JsObject may be
    * returned.
   * This default implementation yields a NotImplemented exception .. by design.
   * @return The JsObject representing this "thing"
   */
  def toJson : JsObject = ???
}

/** Something that can be created and keeps track of its modification time */
trait Creatable[T <: Creatable[T]] extends Identifiable[T] {
  val created : DateTime = DateTime.now()
  def exists = isIdentified
}

/** Something that can be modified and keeps track of its time of modification */
trait Modifiable {
  val modified : Option[DateTime] = None
  def isModified = modified.isDefined
}

/** Something that can be named with a Symbol  */
trait Nameable {
  val name : Symbol
  def isNamed : Boolean = ! name.name.isEmpty
}

/** Something that has a short textual description */
trait Describable {
  val description : String
  def isDescribed : Boolean = ! description.isEmpty
}

/** The most basic thing Scrupal can represent: Identifiable, Creatable, and Nameable.
  * Notably, these are immutable things because we don't keep track of a modification time stamp. The only storage
  * operations permitted are create, read and delete (CRD, not CRUD).
  *
  * @param name The name of the `BasicThing`
  * @param created The timestamp of the creation of the `BasicThing`
  * @param id The unique storage identifier of the `BasicThing`
  * @tparam T The type of the
  */
abstract class ImmutableThing[T <: ImmutableThing[T]] (
  override val name: Symbol,
  override val created : DateTime = DateTime.now(),
  override val id : Option[Long] = None
) extends Equals with Creatable[T] with Nameable {
  override def canEqual(other: Any) : Boolean = other.isInstanceOf[ImmutableThing[T]]
  override def equals(other: Any) : Boolean = {
    other match {
      case that: ImmutableThing[T] => { (this eq that) || (
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

/** An `ImmutableThing` with a description.
  * Just adds a short textual description of the thing.
  *
  * @param name The name of the immutable thing
  * @param description A brief description of the immutable thing
  * @param created The timestamp of creation of the immutable thing
  * @param id The unique storage identifier of the immutable thing
  * @tparam T The leaf class in which this `ImmutableThing` occurs.
  */
abstract class DescribedThing[T <: DescribedThing[T]] (
  override val name: Symbol,
  override val description: String,
  override val created : DateTime = DateTime.now(),
  override val id : Option[Long] = None
) extends ImmutableThing[T](name, created, id) with Describable
{
  override def canEqual(other: Any) : Boolean = other.isInstanceOf[DescribedThing[T]]
  override def equals(other: Any) : Boolean = {
    other match {
      case that: DescribedThing[T] => { (this eq that) || (
        that.canEqual(this) && super.equals(that) &&
          ( this.isDescribed == that.isDescribed) &&
          ( ! this.isDescribed || this.description.equals(that.description))
        )
      }
      case _ => false
    }
  }
}

/** A Described, Immutable, and Unsavable thing.
  * This just adds a default definition for the forId method that throws an exception in case it gets invoked. The
  * idea her is that subclasses of UnsavableThing are memory only objects that cannot be saved elsewhere and
  * consequently have no need for implementing forId to obtain their identifier. This class also terminates the need
  * for having type parameters
  */
abstract class UnsavableThing[T <: UnsavableThing[T]] (
  override val name: Symbol,
  override val description: String,
  override val created : DateTime = DateTime.now(),
  override val id : Option[Long] = None
) extends DescribedThing[T](name, description, created, id) {
  override def forId(id: Long) : T = throw new IllegalAccessError("You shouldn't call forId on a type!")
}


/** The Basic `Thing` that we can model with Scrupal
  * Generally the things that are interesting are identifiable, creatable, modifiable,
  * named and described. These traits hold for most of the `Thing`s that Scrupal will manipulate.
  * @param name The name of the `Thing`
  * @param description A brief description of the `Thing`
  * @param modified The time of last modification of the `Thing`
  * @param created The time at which the `Thing` was created
  * @param id The unique identifier of the `Thing` within its storage realm.
  * @tparam T The type of the leaf class in which this Thing occurs
  */
abstract class Thing[T <: Thing[T]] (
  override val name: Symbol,
  override val description: String,
  override val modified : Option[DateTime] = Some(DateTime.now()),
  override val created : DateTime = DateTime.now(),
  override val id : Option[Long] = None
)  extends DescribedThing[T](name, description, created, id) with Modifiable {
  override def canEqual(other: Any) : Boolean = other.isInstanceOf[Thing[T]]
  override def equals(other: Any) : Boolean = {
    other match {
      case that: Thing[T] => { (this eq that) || (
        that.canEqual(this) && super.equals(that) &&
          ( this.isModified == that.isModified) &&
          ( ! this.isModified || this.modified.get.equals(that.modified.get))
        )
      }
      case _ => false
    }
  }
}

abstract class EnabledThing[T <: EnabledThing[T]] (
  override val name: Symbol,
  override val description: String,
  val enabled : Boolean = false,
  override val modified : Option[DateTime] = Some(DateTime.now()),
  override val created : DateTime = DateTime.now(),
  override val id : Option[Long] = None
) extends Thing[T](name, description, modified, created, id)  {
  override def canEqual(other: Any) : Boolean = other.isInstanceOf[EnabledThing[T]]
  override def equals(other: Any) : Boolean = {
    other match {
      case that: EnabledThing[T] => { (this eq that) || (
        that.canEqual(this) && super.equals(that) &&
          ( this.enabled == that.enabled)
        )
      }
      case _ => false
    }
  }
}
