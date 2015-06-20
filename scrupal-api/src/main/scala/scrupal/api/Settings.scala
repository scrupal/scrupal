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

package scrupal.api

import java.time.Instant

import scala.collection.mutable
import scala.concurrent.duration.Duration
import shapeless._

/** Interface To Settings
  * This defines the interface to value extraction from some cache of settings
  */
trait Settings {

  type ValueType = Boolean :+: Byte :+: Int :+: Long :+: Double :+: String :+: Instant :+: Duration :+: Settings :+: CNil
  type ArrayType = Seq[ValueType]
  type ElemType = Settings :+: ArrayType :+: ValueType
  type DataType = mutable.Map[String,ElemType]
  val settings : DataType = mutable.HashMap.empty[String,ElemType]
  val defaults : DataType = mutable.HashMap.empty[String,ElemType]

  def entrySet : Iterable[ElemType] = settings.values

  def get(path: String) : Option[ElemType] = {
    val index = path.indexOf('.')
    if (index == 0)
      return None
    if (index < 0) {
      settings.get(path)
    } else {
      val path_element = path.substring(0, index)
      val remainder = path.substring(index+1)
      settings.get(path_element) flatMap { e: ElemType ⇒
        e.select[Settings] flatMap { s: Settings ⇒ s.get(remainder) }
      }
    }
  }

  def getString(path : String) : Option[String] = get(path).flatMap { _.select[String] }
  def getBoolean(path : String) : Option[Boolean] = get(path).flatMap { _.select[Boolean] }
  def getByte(path : String) : Option[Byte] = get(path).flatMap {_.select[Byte] }
  def getInt(path : String) : Option[Int] = get(path).flatMap {_.select[Int] }
  def getLong(path : String) : Option[Long] = get(path).flatMap {_.select[Long] }
  def getDouble(path : String) : Option[Double] = get(path).flatMap {_.select[Double] }
  def getInstant(path : String) : Option[Instant] = get(path).flatMap {_.select[Instant] }
  def getDuration(path : String) : Option[Duration] = get(path).flatMap { _.select[Duration] }
  def getMilliseconds(path : String) : Option[Long] = get(path).flatMap { _.select[Duration] } map { x ⇒ x.toMillis }
  def getMicroseconds(path : String) : Option[Long] = get(path).flatMap { _.select[Duration] } map { x ⇒ x.toMicros }
  def getNanoseconds(path : String) : Option[Long]  = get(path).flatMap { _.select[Duration] } map { x ⇒ x.toNanos  }

  def getSettings(path : String) : Option[Settings] = get(path).flatMap { _.select[Settings] }

  def getStrings(path : String) : Option[Seq[String]] = ???
  def getBooleans(path : String) : Option[Seq[Boolean]] = ???
  def getBytes(path : String) : Option[Seq[Long]] = ???
  def getInts(path : String) : Option[Seq[Int]] = ???
  def getLongs(path : String) : Option[Seq[Long]] = ???
  def getDoubles(path : String) : Option[Seq[Double]] = ???
  def getNumbers(path : String) : Option[Seq[Number]] = ???
  def getInstants(path : String) : Option[Seq[Instant]] = ???
  def getDurations(path : String) : Option[Seq[Duration]] = ???

  def setValues(values : DataType) : Unit = ???
  def setString(path : String, value : String) : Unit = ???
  def setBoolean(path : String, value : Boolean) : Unit = ???
  def setByte(path : String, value : Byte) : Unit = ???
  def setInt(path : String, value : Int) : Unit = ???
  def setLong(path : String, value : Long) : Unit = ???
  def setDouble(path : String, value : Double) : Unit = ???
  def setNumber(path : String, value : Number) : Unit = ???
  def setInstance(path : String, value : Instant) : Unit = ???
  def setDuration(path : String, value : Duration) : Unit = ???

  def setStrings(path : String, value : Seq[String]) : Unit = ???
  def setBooleans(path : String, value : Seq[Boolean]) : Unit = ???
  def setBytes(path : String, value : Seq[Byte]) : Unit = ???
  def setDoubles(path : String, value : Seq[Double]) : Unit = ???
  def setInts(path : String, value : Seq[Int]) : Unit = ???
  def setLongs(path : String, value : Seq[Long]) : Unit = ???
  def setNumbers(path : String, value : Seq[Number]) : Unit = ???
  def setInstants(path : String, value : Seq[Instant]) : Unit = ???
  def setDurations(path : String, value : Seq[Duration]) : Unit = ???

  def toConfig : com.typesafe.config.Config = ???
}
