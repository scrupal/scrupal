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

import java.io.{ByteArrayInputStream, ByteArrayOutputStream}

import org.specs2.mutable.Specification
import scrupal.utils.{ScrupalComponent, ScrupalException}

/** Test Cases For Storage Codec */
class CodecSpec extends Specification with ScrupalComponent {

  case class TestStorable(data: String = "foo") extends Storable
  case class TestStorableCodec(
    id: Symbol, registry: CodecRegistry, regNum: Int, clazz : Class[TestStorable] = classOf[TestStorable]
  ) extends Codec[TestStorable]
  case class TestCodec[T <: Storable](
    id: Symbol, registry: CodecRegistry, regNum: Int, clazz : Class[T]
  ) extends Codec[T]
  case class BigClass(one: Boolean, two: Byte, three: Short, four: Int, five: Long, six: Float, seven: Double,
    eight: String, nine: (String,Long), ten: Char, eleven: Any, twelve: AnyRef, thirteen: Option[Int]) extends Storable

  "Codec" should {
    "encode/decode simple class" in {
      val reg = new CodecRegistry
      val c1 = TestStorableCodec('hundred, reg, reg.MinimumRegistrationNumber)
      val encodable = TestStorable("fooness")
      val bytes = c1.encode(encodable)
      val decoded : TestStorable = c1.decode(bytes)
      encodable.data must beEqualTo(decoded.data)
    }
    "encode/decode class with many fields" in {
      val reg = new CodecRegistry
      val codec = new TestCodec('big, reg, reg.MinimumRegistrationNumber, classOf[BigClass])
      val encodable = BigClass(true, 0x2.toByte, 3.toShort, 4, 5L, 6.0F, 7.0D, "8", "five" → 4, 10.toChar, "eleven", "twelve",
        Some(13))
      val bytes = codec.encode(encodable)
      val decoded : BigClass = codec.decode(bytes)
      encodable must beEqualTo(decoded)
    }
    "encode/decode big class to/from stream" in {
      val reg = new CodecRegistry
      val codec = new TestCodec('big, reg, reg.MinimumRegistrationNumber, classOf[BigClass])
      val encodable = BigClass(true, 0x2.toByte, 3.toShort, 4, 5L, 6.0F, 7.0D, "8", "five" → 4, 10.toChar, "eleven", "twelve",
        Some(13))
      val baos = new ByteArrayOutputStream
      codec.encode(encodable, baos)
      val bytes = baos.toByteArray
      val bais = new ByteArrayInputStream(bytes)
      val decoded : BigClass = codec.decode(bais)
      encodable must beEqualTo(decoded)
    }
  }

  "Codec Registry" should {
    "ensure registration numbers are not too small" in{
      val reg = new CodecRegistry
      val c1 = TestStorableCodec('ninetyNine, reg, reg.MinimumRegistrationNumber - 1)
      try {
        c1.encode(new TestStorable)
        failure("Should have thrown ScrupalException")
      } catch {
        case x: ScrupalException ⇒
          log.info(s"Got exception: ${x.getMessage}")
          (x.getMessage must contain("registration numbers too small")).toResult
        case x: Throwable ⇒ failure(s"Wrong Exception: $x")
      }
    }
    "prevent duplicate registrations" in {
      val reg = new CodecRegistry
      val c1 = TestStorableCodec('hundred, reg, reg.MinimumRegistrationNumber)
      val c2 = TestStorableCodec('oh_one, reg, reg.MinimumRegistrationNumber)
      try {
        c1.encode(new TestStorable)
        failure("Should have thrown ScrupalException")
      } catch {
        case x: ScrupalException ⇒
          log.info(s"Got exception: ${x.getMessage}")
          (x.getMessage must contain(s"#${reg.MinimumRegistrationNumber}: hundred:TestStorable, oh_one:TestStorable")).toResult
        case x: Throwable ⇒ failure(s"Wrong Exception: $x")
      }
    }

    "prevent overriding standard serializers" in {
      val reg = new CodecRegistry
      val c = TestStorableCodec('one, reg, 1)
      try {
        c.encode(new TestStorable)
        failure("Should have thorwn ScrupalException")
      } catch {
        case x: ScrupalException ⇒
          log.info(s"Got exception: ${x.getMessage}")
          (x.getMessage must contain("#1: TestStorable")).toResult
        case x: Throwable ⇒ failure(s"Wrong Exception: $x")
      }
    }
  }
}
