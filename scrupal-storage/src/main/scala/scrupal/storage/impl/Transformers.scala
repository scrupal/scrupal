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

import java.io.{ObjectInputStream, ByteArrayInputStream, ObjectOutputStream, ByteArrayOutputStream}
import java.nio.charset.StandardCharsets

import play.api.libs.json._

import scrupal.storage.api.{Storable, StorableTransformer, StoredFormat}

import scala.pickling.Defaults._
import scala.pickling.{Pickler, Unpickler}
import scala.pickling.binary._

case class IdentityStoredFormat[S <:Storable](s: S) extends StoredFormat {
  def toBytes : Array[Byte] = Array.empty[Byte]
}

case class IdentityTransformer[S <: Storable]() extends StorableTransformer[IdentityStoredFormat[S], S] {
  def write(s : S) : IdentityStoredFormat[S] = IdentityStoredFormat(s)
  def read(data : IdentityStoredFormat[S]) : S = data.s
}

case class BytesStoredFormat(bytes: Array[Byte]) extends StoredFormat { def toBytes : Array[Byte] = bytes }

case class JavaSerializationTransformer[S <: Storable]()  extends StorableTransformer[BytesStoredFormat,S]
{
  def write(s : S) : BytesStoredFormat = {
    val out = new ByteArrayOutputStream()
    val writer = new ObjectOutputStream(out)
    try {
      writer.writeObject(s)
      BytesStoredFormat(out.toByteArray)
    } finally {
      writer.close()
      out.close()
    }
  }

  def read(data : BytesStoredFormat) : S = {
    val in = new ByteArrayInputStream(data.toBytes)
    val reader = new ObjectInputStream(in)
    try {
      reader.readObject().asInstanceOf[S]
    } finally {
      reader.close()
      in.close()
    }
  }
}

case class JsonStoredFormat(jsv: JsValue) extends StoredFormat {
  def toBytes: Array[Byte] = Json.stringify(jsv).getBytes(StandardCharsets.UTF_8)
}

case class JsonTransformer[S <: Storable](reader: Reads[S], writer: Writes[S])
  extends StorableTransformer[JsonStoredFormat, S]
{
  def write(s : S) : JsonStoredFormat = JsonStoredFormat(writer.writes(s))
  def read(data: JsonStoredFormat) : S = {
    val jsv = Json.parse(data.toBytes)
    reader.reads(jsv) match {
      case x: JsSuccess[S] ⇒ x.value;
      case x: JsError ⇒ throw new Exception(JsError.toFlatForm(x).toString())
    }
  }
}

case class PickleStoredFormat(pickled: BinaryPickle) extends StoredFormat {
  def toBytes: Array[Byte] = pickled.value
}

case class PickleTransformer[S <: Storable](writer: Pickler[S], reader: Unpickler[S])
  extends StorableTransformer[PickleStoredFormat, S]
{
  val format = new BinaryPickleFormat
  def write(s : S) : PickleStoredFormat = PickleStoredFormat(s.pickle(format, writer))
  def read(data: PickleStoredFormat) : S = data.pickled.unpickle[S](reader, format)
}

object Kryo {
  import com.esotericsoftware.kryo.Kryo
  import com.romix.scala.serialization.kryo._

  case class KryoStoredFormat() extends StoredFormat { def toBytes : Array[Byte] = Array.empty[Byte]}

  case class KryoTransformer[S <: Storable]() extends StorableTransformer[StoredFormat, S] {
    def write(s: S): StoredFormat = ???

    def read(data: StoredFormat): S = ???
  }

  class KryoInit {
    def customize(kryo: Kryo): Unit = {
      // Serialization of Scala enumerations
      kryo.addDefaultSerializer(classOf[scala.Enumeration#Value], classOf[EnumerationSerializer])
      kryo.register(Class.forName("scala.Enumeration$Val"))
      kryo.register(classOf[scala.Enumeration#Value])
      kryo.register(classOf[Storable])

      // Serialization of Scala Immutable Collections
      kryo.addDefaultSerializer(classOf[scala.collection.Map[_, _]], classOf[ScalaImmutableMapSerializer])
      kryo.addDefaultSerializer(classOf[scala.collection.Set[_]], classOf[ScalaImmutableSetSerializer])
      kryo.addDefaultSerializer(classOf[scala.collection.SortedSet[_]], classOf[ScalaImmutableSortedSetSerializer])
      kryo.addDefaultSerializer(classOf[scala.collection.SortedMap[_,_]], classOf[ScalaSortedMapSerializer])

      // Serialization of Scala Mutable Collections
      kryo.addDefaultSerializer(classOf[scala.collection.generic.MapFactory[scala.collection.Map]],
        classOf[ScalaMutableMapSerializer])
      kryo.addDefaultSerializer(classOf[scala.collection.generic.SetFactory[scala.collection.Set]],
        classOf[ScalaMutableSetSerializer])
      kryo.addDefaultSerializer(classOf[scala.collection.generic.SortedSetFactory[scala.collection.SortedSet]],
        classOf[ScalaMutableSortedSetSerializer])

      // Serialization of all Traversable Scala collections like Lists, Vectors, etc
      kryo.addDefaultSerializer(classOf[scala.collection.Traversable[_]], classOf[ScalaCollectionSerializer])

      // Serialization of all Product Scala classes like Tuple*
      kryo.addDefaultSerializer(classOf[scala.Product], classOf[ScalaProductSerializer])
    }
  }
}
