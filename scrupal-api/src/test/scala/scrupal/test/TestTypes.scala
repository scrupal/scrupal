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

package scrupal.test

import akka.http.scaladsl.model.MediaTypes
import scrupal.api._
import scrupal.utils.Patterns._
import scrupal.utils.Validation.{DefaultLocation, Location}

/** Title Of Thing.
  *
  * Description of thing
  */
case class TestTypes(scrpl: Scrupal) extends {
  val name = "TestTypes"
  implicit val scrupal: Scrupal = scrpl
} with FakeContext[TestTypes] {
  /** The Scrupal Type for Uniform Resource Identifiers per http://tools.ietf.org/html/rfc3986 */
  object MiddlePeriod extends AnyType(sym("MiddlePeriod"), "A type for validating URI strings.") {
    override def validate(ref: Location, value: Any) = simplify(ref, value, "String") {
      case v: String => {
        val a = v.split('.')
        if (a.size > 2)
          Some("Too many periods")
        else if (a.size < 2)
          Some("Must have at least one period")
        else if (a(0).length != a(1).length)
          Some("Strings on each side of . must have same length")
        else
          None
      }
      case _ => Some("")
    }
  }

  val vLoc = DefaultLocation

  object rangeTy extends RangeType(sym("aRange"), "Ten from 10", 10, 20)
  object realTy extends RealType(sym("aReal"), "Ten from 10", 10.1, 20.9)
  object enumTy extends EnumType( sym("enumTy"), "Enum example", Map(
    'one -> 1, 'two -> 2, 'three -> 3, 'four -> 5, 'five -> 8, 'six -> 13
  ))

  object blobTy extends BLOBType(sym("blobTy"), "Blob example", MediaTypes.`application/octet-stream`, 4)
  object listTy extends ListType(sym("listTy"), "List example", enumTy)

  object setTy extends SetType(sym("setTy"), "Set example", rangeTy)

  object mapTy extends MapType[Atom](sym("mapTy"), "Map example", Seq("foo"), realTy)

  object emailTy extends StringType(sym("EmailAddress"), "An email address", anchored(EmailAddress), 253)

  object trait1 extends BundleType(sym("trait1"), "Trait example 1",
    fields = Map (
      "even" -> MiddlePeriod,
      "email" -> emailTy,
      "range" -> rangeTy,
      "real" -> realTy,
      "enum" -> enumTy
    )
  )

  object trait2 extends BundleType(sym("trait2"), "Trait example 2",
    fields = Map(
      "list" -> listTy,
      "set" -> setTy,
      "map" -> mapTy
    )
  )

  object AnEntity extends BundleType(sym("AnEntity"), "Entity example",
    fields = Map("trait1" -> trait1, "trait2" -> trait2)
  )

  val js1 = Map(
    "even" -> "foo.bar",
    "email" -> "somebody@example.com",
    "range" -> 17,
    "real" -> 17.0,
    "enum" -> "three"
  )

  val js2 = Map(
    "list" -> Seq("one", "three", "five"),
    "set" -> Seq(17, 18),
    "map" -> Map("foo" -> 17.0)
  )
}
