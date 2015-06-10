/** ********************************************************************************************************************
  * This file is part of Scrupal, a Scalable Reactive Content Management System.                                       *
  *                                                                                               *
  * Copyright © 2015 Reactific Software LLC                                                                            *
  *                                                                                               *
  * Licensed under the Apache License, Version 2.0 (the "License");  you may not use this file                         *
  * except in compliance with the License. You may obtain a copy of the License at                                     *
  *                                                                                               *
  * http://www.apache.org/licenses/LICENSE-2.0                                                                  *
  *                                                                                               *
  * Unless required by applicable law or agreed to in writing, software distributed under the                          *
  * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,                          *
  * either express or implied. See the License for the specific language governing permissions                         *
  * and limitations under the License.                                                                                 *
  * ********************************************************************************************************************
  */

package scrupal.storage.api

import play.api.libs.json._
import scrupal.utils.ScrupalComponent

import scala.pickling.binary.BinaryPickleFormat

/** A Storable Object
  *
  * All entities placed in storage derive from Storable.
  * @tparam S The end type of the storable
  */
trait Storable[S <: Storable[S]] { self : S ⇒
  var primary_id : Long = Storable.undefined_primary_id
  def indexables : Iterable[Indexable[_, S]] = Iterable.empty[Indexable[_, S]]
  def indexable(name : String) : Option[Indexable[_, S]] = {
    indexables.find { p : Indexable[_, S] ⇒ p.name == name }
  }
}

trait StorableTransformer[OUT, S <: Storable[S]] {
  def write(s : S) : OUT
  def read(data : OUT) : S
}

class IdentityTransformer[S <: Storable[S]] extends StorableTransformer[S, S] {
  def write(s : S) : S = s
  def read(data : S) : S = data
}

trait JsonTransformer[S <: Storable[S]] extends StorableTransformer[JsValue, S] {
  def write(s : S) : JsValue
  def intermediate_read(v : JsValue) : JsResult[S]
  def read(v : JsValue) : S = intermediate_read(v) match {
    case x : JsSuccess[S] ⇒ x.value;
    case x : JsError ⇒ throw new Exception(JsError.toFlatForm(x).toString())
  }
}

import scala.pickling.binary._

trait BinaryTransformer[S <: Storable[S]] extends StorableTransformer[BinaryPickleFormat#OutputType, S] {
  def pickler : BinaryPickleFormat#PickleType
}

object Storable extends ScrupalComponent {
  val undefined_primary_id : Long = Long.MinValue

  def apply[OUT, S <: Storable[S]](data : OUT)(implicit trans : StorableTransformer[OUT, S]) : S = {
    trans.read(data)
  }

  implicit class StorablePimps[S <: Storable[S]](storable : S) {
    final def payload[OUT](implicit trans : StorableTransformer[OUT, S]) : OUT = trans.write(storable)
  }
}

