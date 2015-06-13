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

package scrupal.storage.api

import java.net.URI

import play.api.libs.json.Json
import scrupal.storage.impl.Kryo.KryoTransformer

import scala.pickling.Defaults._
import scala.pickling.{Unpickler, Pickler}
import scala.pickling.binary._

import scrupal.test.ScrupalSpecification
import scrupal.storage.impl.{IdentityTransformer, JsonTransformer, PickleTransformer}

case class DoubleVal(value: Double = 42.0) extends Storable
case class Simple(a: Double = 42.0, b: Long = 42, c: String = "42") extends Storable
object MyEnum extends Enumeration { type Kind = Value; val v1=Value; val v2=Value; val v3=Value }
case class Enum(e : MyEnum.Kind = MyEnum.v2) extends Storable
case class Collections(list :List[Int] = List(42,21), seq : Seq[Long] = Seq(42L,21L) ) extends Storable
case class Uri(scheme: String, rest: String) extends Storable
case class Complex(
  a: Double = 42.0,
  b: Seq[Any] = Seq("a",42.0,17),
  c: Map[Symbol,List[(String,Uri)]] = Map('a -> List("b" -> Uri("http", "://scrupal.github.io")))
  ) extends Storable


class StorageTransformerSpec extends ScrupalSpecification("StorageTransformer") {

  implicit val MyEnumPickler = Pickler.generate[MyEnum.Kind]
  implicit val UriPickler = Pickler.generate[Uri]

  def applyTransformer(transformer: StorableTransformer[StoredFormat,Storable], value: Storable) = {
    val result = transformer.read(transformer.write(value))
    if (!result.equals(value))
      result must beEqualTo(value)
    success
  }

  "PicklerTransformer" should {
    "handle DoubleVal" in {
      val transformer = PickleTransformer(Pickler.generate[DoubleVal], Unpickler.generate[DoubleVal])
      applyTransformer(transformer.asInstanceOf[StorableTransformer[StoredFormat,Storable]], DoubleVal())
    }
    "handle Simple" in {
      val transformer = PickleTransformer(Pickler.generate[Simple], Unpickler.generate[Simple])
      applyTransformer(transformer.asInstanceOf[StorableTransformer[StoredFormat,Storable]], Simple())
    }
    "handle Enum" in {
      val transformer = PickleTransformer(Pickler.generate[Enum], Unpickler.generate[Enum])
      applyTransformer(transformer.asInstanceOf[StorableTransformer[StoredFormat,Storable]], Enum())
    }
    "handle Collections" in {
      val transformer = PickleTransformer(Pickler.generate[Collections], Unpickler.generate[Collections])
      applyTransformer(transformer.asInstanceOf[StorableTransformer[StoredFormat,Storable]], Collections())
    }
    "handle Uri" in {
      val transformer = PickleTransformer(Pickler.generate[Uri], Unpickler.generate[Uri])
      applyTransformer(transformer.asInstanceOf[StorableTransformer[StoredFormat,Storable]], Uri("scrupal-mem","foo"))
    }
    "handle Complex" in {
      val transformer = PickleTransformer(Pickler.generate[Complex], Unpickler.generate[Complex])
      applyTransformer(transformer.asInstanceOf[StorableTransformer[StoredFormat,Storable]], Complex())

    }
  }

  "KryoTransformer" should {
    "handle DoubleVal" in {
      val transformer = KryoTransformer()
      applyTransformer(transformer.asInstanceOf[StorableTransformer[StoredFormat,Storable]], DoubleVal())
    }
    "handle Simple" in {
      val transformer = KryoTransformer()
      applyTransformer(transformer.asInstanceOf[StorableTransformer[StoredFormat,Storable]], Simple())
    }
    "handle Enum" in {
      val transformer = KryoTransformer()
      applyTransformer(transformer.asInstanceOf[StorableTransformer[StoredFormat,Storable]], Enum())
    }
    "handle Collections" in {
      val transformer = KryoTransformer()
      applyTransformer(transformer.asInstanceOf[StorableTransformer[StoredFormat,Storable]], Collections())
    }
    "handle Uri" in {
      val transformer = KryoTransformer()
      applyTransformer(transformer.asInstanceOf[StorableTransformer[StoredFormat,Storable]], Uri("scrupal-mem","foo"))
    }
    "handle Complex" in {
      val transformer = KryoTransformer()
      applyTransformer(transformer.asInstanceOf[StorableTransformer[StoredFormat,Storable]], Complex())

    }

  }
/*
  val EnumKindWriter = Json.writes[MyEnum.Kind]
  val EnumKindReader = Json.reads[MyEnum.Kind]
  val EnumWriter = Json.writes[Enum]
  val EnumReader = Json.reads[Enum]
  val SeqAnyWriter = Json.writes[Seq[Any]]
  val SeqAnyReader = Json.reads[Seq[Any]]
*/

  "JsonTransformer" should {
    "handle DoubleVal" in {
      val transformer = JsonTransformer(Json.reads[DoubleVal], Json.writes[DoubleVal])
      applyTransformer(transformer.asInstanceOf[StorableTransformer[StoredFormat, Storable]], DoubleVal())
    }
    "handle Simple" in {
      val transformer = JsonTransformer(Json.reads[Simple], Json.writes[Simple])
      applyTransformer(transformer.asInstanceOf[StorableTransformer[StoredFormat, Storable]], Simple())
    }
    "handle Enum" in {
      pending("Figure out how to do enums with Json")
      /*
      val transformer = JsonTransformer(Json.reads[Enum], Json.writes[Enum])
      applyTransformer(transformer.asInstanceOf[StorableTransformer[StoredFormat, Storable]], Enum())
      */
    }
    "handle Collections" in {
      val transformer = JsonTransformer(Json.reads[Collections], Json.writes[Collections])
      applyTransformer(transformer.asInstanceOf[StorableTransformer[StoredFormat, Storable]], Collections())
    }
    "handle Uri" in {
      val transformer = JsonTransformer(Json.reads[Uri], Json.writes[Uri])
      applyTransformer(transformer.asInstanceOf[StorableTransformer[StoredFormat, Storable]], Uri("scrupal-mem", "foo"))
    }
    "handle Complex" in {
      pending("Figure out how to do scala collections wiht Json")
      /*
      val transformer = JsonTransformer(Json.reads[Complex], Json.writes[Complex])
      applyTransformer(transformer.asInstanceOf[StorableTransformer[StoredFormat, Storable]], Complex())
*/
    }
  }

  "IdentityTransformer" should {
    "handle DoubleVal" in {
      val transformer = IdentityTransformer[DoubleVal]()
      applyTransformer(transformer.asInstanceOf[StorableTransformer[StoredFormat, Storable]], DoubleVal())
    }
    "handle Simple" in {
      val transformer = IdentityTransformer[Simple]()
      applyTransformer(transformer.asInstanceOf[StorableTransformer[StoredFormat, Storable]], Simple())
    }
    "handle Enum" in {
      val transformer = IdentityTransformer[Enum]()
      applyTransformer(transformer.asInstanceOf[StorableTransformer[StoredFormat, Storable]], Enum())
    }
    "handle Collections" in {
      val transformer = IdentityTransformer[Collections]()
      applyTransformer(transformer.asInstanceOf[StorableTransformer[StoredFormat, Storable]], Collections())
    }
    "handle Uri" in {
      val transformer = IdentityTransformer[Uri]()
      applyTransformer(transformer.asInstanceOf[StorableTransformer[StoredFormat, Storable]], Uri("scrupal-mem", "foo"))
    }
    "handle Complex" in {
      val transformer = IdentityTransformer[Complex]()
      applyTransformer(transformer.asInstanceOf[StorableTransformer[StoredFormat, Storable]], Complex())
    }
  }

  "Transformers" should {
    "handle a class hierarchy with cycles" in {
      pending
      /*
      import scala.pickling.shareNothing._

      val transformer = new PickleTransformer[Complex] {
        def write(s: Complex): BinaryPickle = s.pickle
        def read(data: BinaryPickle): Complex= data.unpickle[Complex]
      }
      val result = transformer.read(transformer.write(Complex()))
      result must beEqualTo(Complex())
    }

    "handle an acyclic directed graph of classes in an inheritance hierarchy" in {
      import scala.pickling.shareNothing._
      trait Node  extends Storable { def value: String; def isRoot: Boolean }
      case class RootNode(value: String, kids: List[Node]) extends Node { val isRoot = true }
      case class BranchNode(value: String, kids: List[Node]) extends Node { val isRoot = false }
      case class LeafNode(value: String) extends Node { val isRoot = false }
      val transformer = new PickleTransformer[Node] {
        def write(s: Node): BinaryPickle = s.pickle
        def read(data: BinaryPickle): Node= data.unpickle[Node]
      }
      val tree = RootNode("r",List(BranchNode("b", List(LeafNode("1"), LeafNode("2")))))
      val copy = tree.copy()
      val result = transformer.read(transformer.write(tree))
      result must beEqualTo(copy)
    */
    }
  }
}

