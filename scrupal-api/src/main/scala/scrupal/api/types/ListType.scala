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
import scrupal.utils.Validation.{Results, IndexedLocation}

/** A Homogenous List type allows a non-exclusive list of elements of other types to be constructed
  *
  * @param id
  * @param description
  * @param elemType
  */
case class ListType[EType](
  id : Identifier,
  description : String,
  elemType : Type[EType])(implicit val scrupal: Scrupal) extends IndexableType[EType, Seq[EType]] {
  override def kind = 'List
  def toSeq(st : Seq[EType]) : Seq[EType] = st
  def validateElement(ref : IndexedLocation, v : EType) : Results[EType]= {
    elemType.validate(ref, v)
  }
}
