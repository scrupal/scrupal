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

package scrupal.storage.impl

import org.specs2.mutable.Specification
import play.api.libs.json.Json
import scrupal.storage.api._

case class DoubleVal(value: Double = 42.0) extends Storable
case class Simple(a: Double = 42.0, b: Long = 42, c: String = "42") extends Storable
object MyEnum extends Enumeration { type Kind = Value; val v1=Value; val v2=Value; val v3=Value }
case class Enum(e : MyEnum.Kind = MyEnum.v2) extends Storable
case class Collections(list :List[Int] = List(42,21), seq : Seq[Long] = Seq(42L,21L), map: Map[String,Int] = Map("foo"→23) ) extends Storable
case class Uri(scheme: String, rest: String) extends Storable
case class Complex(
  a: Double = 42.0,
  b: Seq[Any] = Seq("a",42.0,17),
  c: Map[Symbol,List[(String,Uri)]] = Map('a -> List("b" -> Uri("http", "://scrupal.github.io")))
  ) extends Storable


class StorageFormattersSpec extends Specification {

  def applyFormatter(formatter: StorageFormatter[StorageFormat,Storable], value: Storable) = {
    val result = formatter.read(formatter.write(value))
    result must beEqualTo(value)
  }

  import scrupal.storage.impl.KryoFormatter.StorableFormatter

  val kryoF = StorableFormatter

  "KryoTransformer" should {
    "handle DoubleVal" in {
      applyFormatter(kryoF.asInstanceOf[StorageFormatter[StorageFormat,Storable]], DoubleVal())
    }
    "handle Simple" in {
      applyFormatter(kryoF.asInstanceOf[StorageFormatter[StorageFormat,Storable]], Simple())
    }
    "handle Enum" in {
      val f = StorableFormatter
      applyFormatter(kryoF.asInstanceOf[StorageFormatter[StorageFormat,Storable]], Enum())
    }
    "handle Collections" in {
      applyFormatter(kryoF.asInstanceOf[StorageFormatter[StorageFormat,Storable]], Collections())
    }
    "handle Uri" in {
      applyFormatter(kryoF.asInstanceOf[StorageFormatter[StorageFormat,Storable]], Uri("scrupal-mem","foo"))
    }
    "handle Complex" in {
      applyFormatter(kryoF.asInstanceOf[StorageFormatter[StorageFormat,Storable]], Complex())

    }
  }
/*
  implicit val EnumKindFormat = Json.format[MyEnum.Kind]
  implicit val EnumFormat = Json.format[Enum]
  implicit val SeqAnyFormat = Json.format[Seq[Any]]
*/
  "JsonTransformer" should {
    "handle DoubleVal" in {
      val formatter = JsonFormatter(Json.format[DoubleVal])
      applyFormatter(formatter.asInstanceOf[StorageFormatter[StorageFormat, Storable]], DoubleVal())
    }
    "handle Simple" in {
      val formatter = JsonFormatter(Json.format[Simple])
      applyFormatter(formatter.asInstanceOf[StorageFormatter[StorageFormat, Storable]], Simple())
    }
    "handle Enum" in {
      pending("Figure out how to do enums with Json")
      /*
      val transformer = JsonTransformer(Json.reads[Enum], Json.writes[Enum])
      applyTransformer(transformer.asInstanceOf[StorableTransformer[StoredFormat, Storable]], Enum())
      */
    }
    "handle Collections" in {
      val formatter = JsonFormatter(Json.format[Collections])
      applyFormatter(formatter.asInstanceOf[StorageFormatter[StorageFormat, Storable]], Collections())
    }
    "handle Uri" in {
      val formatter = JsonFormatter(Json.format[Uri])
      applyFormatter(formatter.asInstanceOf[StorageFormatter[StorageFormat, Storable]], Uri("scrupal-mem", "foo"))
    }
    "handle Complex" in {
      pending("Figure out how to do Complex with Json")
      /*
      val transformer = JsonTransformer(Json.reads[Complex], Json.writes[Complex])
      applyTransformer(transformer.asInstanceOf[StorableTransformer[StoredFormat, Storable]], Complex())
      */
    }
  }

  "IdentityTransformer" should {
    "handle DoubleVal" in {
      val formatter = IdentityFormatter[DoubleVal]()
      applyFormatter(formatter.asInstanceOf[StorageFormatter[StorageFormat, Storable]], DoubleVal())
    }
    "handle Simple" in {
      val formatter = IdentityFormatter[Simple]()
      applyFormatter(formatter.asInstanceOf[StorageFormatter[StorageFormat, Storable]], Simple())
    }
    "handle Enum" in {
      val formatter = IdentityFormatter[Enum]()
      applyFormatter(formatter.asInstanceOf[StorageFormatter[StorageFormat, Storable]], Enum())
    }
    "handle Collections" in {
      val formatter = IdentityFormatter[Collections]()
      applyFormatter(formatter.asInstanceOf[StorageFormatter[StorageFormat, Storable]], Collections())
    }
    "handle Uri" in {
      val formatter = IdentityFormatter[Uri]()
      applyFormatter(formatter.asInstanceOf[StorageFormatter[StorageFormat, Storable]], Uri("scrupal-mem", "foo"))
    }
    "handle Complex" in {
      val formatter = IdentityFormatter[Complex]()
      applyFormatter(formatter.asInstanceOf[StorageFormatter[StorageFormat, Storable]], Complex())
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

