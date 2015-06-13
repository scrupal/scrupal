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
import scrupal.utils.Validation.{Results, IndexedLocation, SeqValidator}

/** A Set type allows an exclusive Set of elements of other types to be constructed
  *
  * @param id
  * @param description
  * @param elemType
  */
case class SetType[ET](
  id : Identifier,
  description : String,
  elemType : Type[ET]) extends IndexableType[ET,Set[ET]] {
  override type ValueType = Set[ET]
  override def kind = 'Set

  class SetTypeValidator extends SeqValidator[ET, Set[ET]] {
    override def toSeq(st: Set[ET]): Seq[ET] = st.toSeq
    override def validateElement(ref: IndexedLocation, v: ET): Results[ET] = {
      elemType.validate(ref, v)
    }
  }

  val validator : SeqValidator[ET, Set[ET]] = new SetTypeValidator
}
