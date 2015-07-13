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

import akka.util.{ByteString, ByteStringBuilder}
import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.Serializer
import com.esotericsoftware.kryo.serializers.CompatibleFieldSerializer
import com.esotericsoftware.kryo.pool._
import com.twitter.chill.{KryoBase, Input, Output, ScalaKryoInstantiator}
import scrupal.utils.{Registry, Registrable}

/** Encode/Decode Of Storable with Kryo/Chill Serialization
  *
  * Every storable class type needs to derive from Storable and have a Codec provided for it. The default Serializer,
  * CompatibleFieldSerializer supports backwards and forwards compatibility through code changes. That is appropriate
  * for most uses even though it is the most expensive option. Compatibility can be sacrificed for speed by using
  * one of the other Kryo Serialization classes or a hand crafted one.
  *
  * Each Storable class should make an implicit Codec object available in its companion like this:
  * {{{
  *   class Foo extends Storable { ... }
  *   object Foo {
  *     implicit object FooCodec extends Codec[Foo] {
  *       val regNum = 79
  *       val clazz = classOf[Foo]
  *       override def serializer(kryo: Kryo) : Serializer[T] = { ... } // optionally
  *     }
  *   }
  * }}}
  */
trait Codec[T <: Storable] extends Registrable[Codec[_]] {
  def registry : CodecRegistry
  def regNum : Int
  def clazz : Class[T]
  def serializer(kryo: Kryo) : Serializer[T] = new CompatibleFieldSerializer[T](kryo, clazz)
  final def encode(obj : T) : Array[Byte] = {
    registry.withKryo { kryo: Kryo ⇒
      val bldr : ByteStringBuilder = ByteString.newBuilder
      val output = new Output(bldr.asOutputStream)
      kryo.writeClassAndObject(output, obj)
      output.close()
      bldr.result().toArray
    }
  }

  final def decode(bytes: Array[Byte]) : T = {
    registry.withKryo { kryo: Kryo ⇒
      val bs = ByteString(bytes)
      val input = new Input(bs.iterator.asInputStream)
      kryo.readClassAndObject(input).asInstanceOf[T]
    }
  }
}

class CodecRegistry extends Registry[Codec[_]] {
  def registrantsName = "codec"
  def registryName = "Codecs"

  lazy val instantiator = new ScalaKryoInstantiator {
    val storables : Seq[Codec[_]] = {
      val numbers = _registry.map { case (k, v) ⇒ v.regNum }.toSeq
      val unique_numbers = numbers.distinct
      if (numbers.size != unique_numbers.size) {
        val duplicates = numbers.diff(unique_numbers)
        val duplicate_codecs = _registry.filter { case (sym, codec) ⇒
          duplicates.contains(codec.regNum)
        } groupBy { case (sym, codec) ⇒
          codec.regNum
        }
        val messages = duplicate_codecs.map { case (num, map) ⇒
          val names = map.map { case (sym, cod) ⇒ s"${cod.id.name}:${cod.clazz.getSimpleName}" }
          s"#$num: ${names.mkString(", ")}"
        }
        toss(s"Duplicate Storable Registration Numbers Found For: ${messages.mkString("; ")}")
      }
      val kryo = super.newKryo()
      val conflicts = for ( (sym,codec) ← _registry ;
                         reg = kryo.getRegistration(codec.regNum) if reg != null
      ) yield {
        s"#${codec.regNum}: ${codec.clazz.getSimpleName} & ${reg.getType.getSimpleName}"
      }
      if (conflicts.nonEmpty) {
        toss(s"Codec registrations conflict with standard serializers: ${conflicts.mkString(",\n")}.")
      }
      _registry.map { case (sym,codec) ⇒ codec }.toSeq
    }

    override def newKryo(): KryoBase = {
      val kryo = super.newKryo()
      kryo.setDefaultSerializer(classOf[CompatibleFieldSerializer[_]])
      kryo.setRegistrationRequired(true)
      for (s ← storables) {
        kryo.register(s.clazz, s.serializer(kryo), s.regNum)
      }
      kryo
    }
  }

  private lazy val factory = new KryoFactory {
    def create: Kryo = {
      instantiator.newKryo()
    }
  }

  // Build pool with SoftReferences enabled (optional)
  private val pool: KryoPool = new KryoPool.Builder(factory).softReferences().build()

  def withKryo[T](f : (Kryo) ⇒ T) : T = {
    val kryo: Kryo = pool.borrow()
    try {
      f(kryo)
    } finally {
      pool.release(kryo)
    }
  }
}
