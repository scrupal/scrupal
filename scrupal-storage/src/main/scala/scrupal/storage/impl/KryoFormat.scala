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

import akka.util.ByteString
import akka.util.ByteStringBuilder

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.Serializer
import com.esotericsoftware.kryo.io.Input
import com.esotericsoftware.kryo.io.Output

import com.twitter.chill.ScalaKryoInstantiator

import scrupal.storage.api.Storable
import scrupal.storage.api.StorageFormat
import scrupal.storage.api.StorageFormatter
import scrupal.utils.{Registry, Registrable}

case class KryoFormat(kryo: Kryo, bldr : ByteStringBuilder = ByteString.newBuilder) extends StorageFormat {
  def output : Output = new Output(bldr.asOutputStream)
  def input : Input = {
    input(bldr.result())
  }

  def input(bs: ByteString) : Input = {
    new Input(bs.iterator.asInputStream)
  }
  def toBytes : Array[Byte] = {
    bldr.result().toArray
  }
}

abstract class KryoFormatter[S <: Storable](kryo: Kryo, val typ: Class[S], val idNum: Int)
  extends Serializer[S] with StorageFormatter[KryoFormat, S] with Registrable[KryoFormatter[_]] {
  def id : Symbol = { Symbol(typ.getCanonicalName) }
  def registry = KryoFormatter
  def write(s: S): KryoFormat = {
    val result = KryoFormat(kryo)
    val out = result.output
    try {
      write(kryo, out, s)
    } finally {
      out.close()
    }
    result
  }

  def read(data: KryoFormat): S = {
    val in = data.input
    try {
      read(kryo, in, typ)
    } finally {
      in.close()
    }
  }

  def write(kryo: Kryo, output: Output, obj: S) : Unit = {
    output.writeLong(obj.getPrimaryId())
    kryo.writeClassAndObject(output, obj)
  }

  def read(kryo: Kryo, input: Input, typ : Class[S]) : S = {
    val id = input.readLong
    val result = kryo.readClassAndObject(input).asInstanceOf[S]
    result.primaryId = id
    result
  }

}

object KryoFormatter extends Registry[KryoFormatter[_]] {
  def registryName: String = "KryoFormatters"
  def registrantsName: String = "kryoFormatter"

  val instantiator = new ScalaKryoInstantiator
  instantiator.setRegistrationRequired(false)

  val kryo : Kryo = {
    val k : Kryo = instantiator.newKryo()

    k.register(Class.forName("scala.Enumeration$Val"))
    k.register(classOf[scala.Enumeration#Value])
    k.register(classOf[Storable])

    // Serialization of the registered formatter's classes
    for (f <- KryoFormatter.values) {
      k.register(f.typ, f, f.idNum)
    }
    k
  }

  object StorableFormatter extends KryoFormatter[Storable](kryo, classOf[Storable], 99)
}

