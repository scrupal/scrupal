/**********************************************************************************************************************
 * This file is part of Scrupal, a Scalable Reactive Web Application Framework for Content Management                 *
 *                                                                                                                    *
 * Copyright (c) 2015, Reactific Software LLC. All Rights Reserved.                                                   *
 *                                                                                                                    *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance     *
 * with the License. You may obtain a copy of the License at                                                          *
 *                                                                                                                    *
 *     http://www.apache.org/licenses/LICENSE-2.0                                                                     *
 *                                                                                                                    *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed   *
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for  *
 * the specific language governing permissions and limitations under the License.                                     *
 **********************************************************************************************************************/

package scrupal.api.types

import scrupal.api._
import scrupal.api.IndexableType
import scrupal.utils.Validation.{Results, Failure, IndexedLocation}
import shapeless.Coproduct

/** A Homogenous List type allows a non-exclusive list of elements of other types to be constructed
  *
  * @param id
  * @param description
  * @param elemType
  */
case class ListType[EType](
  id : Identifier,
  description : String,
  elemType : Type[EType]) extends IndexableType[EType, Seq[EType]] {
  override def kind = 'List
  def toSeq(st : Seq[EType]) : Seq[EType] = st
  def validateElement(ref : IndexedLocation, v : EType) = {
    elemType.validate(ref, v).asInstanceOf[Failure[EType]]
  }
}

/** A Heterogenous List type  */
abstract class HeteroListType[EType <: Coproduct] extends IndexableType[EType, Seq[EType]] {
  override def kind = 'HeteroList
  def toSeq(st : Seq[EType]) : Seq[EType] = st
  def validateElement(ref : IndexedLocation, v : EType) : Results[EType]
}

/** A List Type that accepts any kind of numeric value */
case class NumbersListType(
  id : Identifier,
  description : String,
  minValue : Long,
  maxValue : Long) extends HeteroListType[NumbersType.Numbers] {
  override val elemType : Type[NumbersType.Numbers] = NumbersType(Symbol(id.name + "-Elem"), "", minValue, maxValue)
  def validateElement(ref : IndexedLocation, v : NumbersType.Numbers) : Results[NumbersType.Numbers] = {
    elemType.validate(ref, v)
  }
}
