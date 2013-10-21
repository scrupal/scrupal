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

/** How Scrupal things are identified.
  * Objects stored in This works for both memory and database objects. The
  * So, in case we ever have to change this, we're calling it an Identifier throughout the api
  */
trait Identifiable[ID] {
  val id : ID
  def isIdentified : Boolean = true
}

trait SymbolicIdentifiable extends Identifiable[Symbol]
trait NumericIdentifiable extends Identifiable[Option[Identifier]] {

  /** The identifier for this Identifiable.
    * Note that it is optional and private and a variable. There's a reason for that. The storage system, not the
    * creator of this object, gets to specify the ID that works for that storage system. If this was a public value
    * then updating the ID after it was created in the database means we would have to create a whole new object,
    * which could be HUGE, just to update on Identifier integer. The cost of that purely functional copying is just too
    * high in this case and the clutter it introduces in the constructors of subclasses is unforgiving. So,
    * we make it an optional var. Optional so it doesn't have to be specified at construction time. A var so it can
    * be updated by the storage system once the thing is created in the DB. Private so that nobody but this class can
    * do the modification -- i.e. if we're going to break the mutability rule, let's constrain the heck out of it!
    * This is our attempt to not spam server memory with lots of database object duplication.
    */
  val id : Option[Identifier] = None

  /** Mutation test.
    * @return true if the object has been identified (had its id value set)
    */
  override def isIdentified = id.isDefined

  /** All things are inherently convertible to Json.
    * We allow each subclass to define the most efficient way to convert itself to Json. Only JsObject may be
    * returned.
    * This default implementation yields a NotImplemented exception .. by design.
    * @return The JsObject representing this "thing"
    */
  // def toJson : JsObject = ???
}

/** Some Thing that is storable and identifiable by an Identifier (long integer) as a unique ID within some storage
  * realm (e.g. adatabase).
  */
trait Storable  extends NumericIdentifiable

/** Something that can be created and keeps track of its modification time.
  * For reasons similar to [[scrupal.api.Storable]], the data provided by this trait is accessible to everyone
  * but mutable by only the scrupal package. This limits the impact of making the created_var a var. Creatable uses
  * the same justifications for this design as does [[scrupal.api.Storable]]
  */
trait Creatable  {
  val created : Option[DateTime] = None;
  def isCreated = created.isDefined
  def exists = isCreated
}

trait NumericCreatable extends Creatable with NumericIdentifiable
trait SymbolicCreatable extends Creatable with SymbolicIdentifiable

/** Something that can be modified and keeps track of its time of modification
  * For reasons similar to [[scrupal.api.Storable]], the data provided by this trait is accessible to everyone
  * but mutable by only the scrupal package. This limits the impact of making the created_var a var. Modifiable uses
  * the same justifications for this design as does [[scrupal.api.Storable]]
  */
trait Modifiable  {
  val modified : Option[DateTime] = None
  def isModified = modified.isDefined
  def changed = isModified
}

trait NumericModifiable extends Modifiable with NumericIdentifiable
trait SymbolicModifiable extends Modifiable with SymbolicIdentifiable

/** Something that can be named with a Symbol  */
trait Nameable  {
  val name : Symbol
  def isNamed : Boolean = ! name.name.isEmpty
}

trait NumericNameable extends Nameable with NumericIdentifiable
trait SymbolicNameable extends Nameable with SymbolicIdentifiable

/** Something that has a short textual description */
trait Describable  {
  val description : String
  def isDescribed : Boolean = ! description.isEmpty
}

trait NumericDescribable extends Describable with NumericIdentifiable
trait SymbolicDescribable extends Describable with SymbolicIdentifiable

trait Enablable  {
  val enabled : Boolean
  def isEnabled : Boolean = enabled
}

trait NumericEnablable extends Enablable with NumericIdentifiable
trait SymbolicEnablable extends Enablable with SymbolicIdentifiable

trait NumericCreatableModifiableNameableDescribable
  extends NumericIdentifiable
          with Creatable
          with Modifiable
          with Nameable
          with Describable

trait SymbolicCreatableModifiableNameableDescribable
  extends SymbolicIdentifiable
          with Creatable
          with Modifiable
          with Nameable
          with Describable

trait SymbolicCreatableModifiableNameableDescribableEnablable
  extends SymbolicCreatableModifiableNameableDescribable
          with Enablable

trait NumericCreatableModifiableNameableDescribableEnablable
  extends NumericCreatableModifiableNameableDescribable
          with Enablable

