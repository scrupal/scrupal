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

import scrupal.utils.Validation._
import scrupal.api._

/** A generic Document Type
  * Subclasses of this type represent types have named fields of a specific type
  *
  * @tparam ET The Element Type
  * @tparam MT The Document Type
  */
trait DocumentType[KT, ET, MT] extends Type[MT] with MapValidator[KT, ET, MT] {
  def validatorFor(id : KT) : Option[Type[_]]
  def fieldNames : Iterable[KT]
  def allowMissingFields : Boolean = false
  def allowExtraFields : Boolean = false
  def toMap(mt : MT) : scala.collection.Map[KT, ET]

  def validateElement(ref : SelectedLocation[KT], k: KT, v : ET) : Results[ET] = {
    validatorFor(k) match {
      case Some(validator) ⇒
        validator.asInstanceOf[Validator[ET]].validate(ref, v)
      case None ⇒ {
        if (!allowExtraFields)
          StringFailure[ET](ref, v, s"Field '$k' is spurious.")
        else
        // Don't validate or anything, spurious field
          Success(ref, v)
      }
    }
  }

  override def validate(ref : Location, value : MT) : Results[MT] = {
    super.validate(ref, value) match {
      case x: Failure[VResult] ⇒ x
      case r: Success[VResult] ⇒
        val elements = toMap(value) // Collect the values only once in case there is a cost for traversing it
      val missing_results: Iterable[Failure[ET]] = if (!allowMissingFields) {
          for (
            fieldName ← fieldNames if !elements.contains(fieldName)
          ) yield {
            StringFailure(ref.select(fieldName), null.asInstanceOf[ET], s"Field '$fieldName' is missing")
          }
        } else {
          Iterable.empty[Failure[ET]]
        }
        if (missing_results.isEmpty)
          Success(ref, value)
        else
          Failures(ref, value, missing_results.toSeq: _*)
    }
  }
}

trait StructuredType[ET] extends DocumentType[String, ET, Map[String, ET]] {
  override type ValueType = Map[String, ET]
  def fields : Map[String, Type[_]]
  def validatorFor(id : String) : Option[Type[_]] = fields.get(id)
  def fieldNames : Iterable[String] = fields.keys
  def size = fields.size
  def toMap(mt : Map[String, ET]) : Map[String, ET] = mt
}

/** A Map is a set whose elements are named with an arbitrary string
  *
  * @param id
  * @param description
  * @param elemType
  */
case class MapType[ET](
  override val id : Identifier,
  description : String,
  elemType : Type[ET]
  ) extends StructuredType[ET] {
  override def kind = 'Map

  def validatorFor(id: String): Option[Type[ET]] = Some(elemType)

  def fieldNames: Seq[String] = Seq.empty[String]

  def toMap(mt: Map[String, ET]): collection.Map[String, ET] = mt

  def fields : Map[String, Type[_]]
  def validatorFor(id : String) : Option[Type[_]] = fields.get(id)
  def fieldNames : Iterable[String] = fields.keys
  def size = fields.size
  def toMap(mt : Map[String, ET]) : Map[String, ET] = mt


}
