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

package scrupal

import java.nio.charset.StandardCharsets
import java.time.Instant

import akka.http.scaladsl.model.{StatusCode, StatusCodes}
import play.api.libs.json.JsObject
import shapeless._

import scala.concurrent.duration.Duration

/** Scrupal API Library.
  * This package provides all the abstract type definitions that Scrupal provides. These are the main abstractions
  * needed to write an application with Scrupal.
  */
package object api {

  lazy val utf8 = StandardCharsets.UTF_8

  /** The typical type of identifier.
    * We use Symbol because they are memoized by the compiler which means we only pay for the memory of a given
    * identifier once. They aren't as easily mistaken for a string either.
    */
  type Identifier = Symbol

  val emptyJsObject = JsObject(Seq())

  import scala.language.implicitConversions

  /** Atom Type
    * This Shapeless type is used to provide a union of the basic atomic types in Scala. Various data structures in
    * Scrupal use Atom as the value type in the structure so that the elements of the structure can have a variety of
    * types. In particular, the Type validator uses Atom as the value type frequently so that numbers can be represented
    * as strings, etc.
    */
  type Atom = Boolean :+: Byte :+: Short :+: Int :+: Long :+: Float :+: Double :+:
    String :+: Symbol :+: Instant :+: Duration :+: CNil

  implicit def atomFromBoolean(b: Boolean) : Atom = Coproduct[Atom](b)
  implicit def atomFromByte(b : Byte) : Atom = Coproduct[Atom](b)
  implicit def atomFromShort(s : Short) : Atom = Coproduct[Atom](s)
  implicit def atomFromInt(i: Int) : Atom = Coproduct[Atom](i)
  implicit def atomFromLong(l: Long) : Atom = Coproduct[Atom](l)
  implicit def atomFromFloat(f: Float) : Atom = Coproduct[Atom](f)
  implicit def atomFromDouble(d: Double) : Atom = Coproduct[Atom](d)
  implicit def atomFromString(str : String) : Atom = Coproduct[Atom](str)
  implicit def atomFromSymbol(sym : Symbol) : Atom = Coproduct[Atom](sym)
  implicit def atomFromInstant(inst : Instant) : Atom = Coproduct[Atom](inst)
  implicit def atomFromDuration(dur: Duration) : Atom = Coproduct[Atom](dur)

  val emptyAtom : Atom = Coproduct[Atom](false)


  type AtomMap = Map[String, Atom]
  type AtomList = List[Atom]

  type Atoms = Atom :+: AtomMap :+: AtomList :+: CNil

  type AtomsMap = Map[String,Atoms]
  type AtomsList = List[Atoms]

  implicit class Disposition2StatusCode(disposition : Disposition) {
    def toStatusCode: StatusCode = {
      disposition match {
        case Successful ⇒ StatusCodes.OK
        case Received ⇒ StatusCodes.Accepted
        case Pending ⇒ StatusCodes.OK
        case Promise ⇒ StatusCodes.OK
        case Unspecified ⇒ StatusCodes.InternalServerError
        case TimedOut ⇒ StatusCodes.GatewayTimeout
        case Unintelligible ⇒ StatusCodes.BadRequest
        case Unimplemented ⇒ StatusCodes.NotImplemented
        case Unsupported ⇒ StatusCodes.NotImplemented
        case Unauthorized ⇒ StatusCodes.Unauthorized
        case Unavailable ⇒ StatusCodes.ServiceUnavailable
        case Unacceptable ⇒ StatusCodes.NotAcceptable
        case NotFound ⇒ StatusCodes.NotFound
        case Ambiguous ⇒ StatusCodes.Conflict
        case Conflict ⇒ StatusCodes.Conflict
        case TooComplex ⇒ StatusCodes.Forbidden
        case Exhausted ⇒ StatusCodes.ServiceUnavailable
        case Exception ⇒ StatusCodes.InternalServerError
        case _ ⇒ StatusCodes.InternalServerError
      }
    }
  }

}
