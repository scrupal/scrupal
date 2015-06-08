/** ********************************************************************************************************************
  * This file is part of Scrupal, a Scalable Reactive Content Management System.                                       *
  *                                                                                                 *
  * Copyright © 2015 Reactific Software LLC                                                                            *
  *                                                                                                 *
  * Licensed under the Apache License, Version 2.0 (the "License");  you may not use this file                         *
  * except in compliance with the License. You may obtain a copy of the License at                                     *
  *                                                                                                 *
  * http://www.apache.org/licenses/LICENSE-2.0                                                                  *
  *                                                                                                 *
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
trait Storable[OUT, S <: Storable[OUT, S]] { self : S ⇒
  def id : Long
  final def payload(implicit trans : StorableTransformer[OUT, S]) : OUT = trans.write(self)
  def indexables : Iterable[Indexable[_, OUT, S]]
  def indexable(name : String) : Option[Indexable[_, OUT, S]] = {
    indexables.find { p : Indexable[_, OUT, S] ⇒ p.name == name }
  }
}

trait JsonStorable[S <: JsonStorable[S]] extends Storable[JsValue, S] { self : S ⇒ }

trait BinaryStorable[S <: BinaryStorable[S]] extends Storable[BinaryPickleFormat#OutputType, S] { self : S ⇒ }

trait StorableTransformer[OUT, S <: Storable[OUT, S]] {
  def write(s : S) : OUT
  def read(data : OUT) : S
}

trait JsonTransformer[S <: JsonStorable[S]] extends StorableTransformer[JsValue, S] {
  def write(s : S) : JsValue
  def intermediate_read(v : JsValue) : JsResult[S]
  def read(v : JsValue) : S = intermediate_read(v) match {
    case x : JsSuccess[S] ⇒ x.value;
    case x : JsError ⇒ throw new Exception(JsError.toFlatForm(x).toString())
  }
}

import scala.pickling.binary._

trait BinaryTransformer[S <: BinaryStorable[S]] extends StorableTransformer[BinaryPickleFormat#OutputType, S] {
  def writer : BinaryPickleFormat#PickleType
}

object Storable extends ScrupalComponent {

  def apply[OUT, S <: Storable[OUT, S]](data : OUT)(implicit trans : StorableTransformer[OUT, S]) : S = {
    trans.read(data)
  }

  implicit class StorablePimps[OUT, S <: Storable[OUT, S]](s : Storable[OUT, S]) {
  }
}

