/**********************************************************************************************************************
 * This file is part of Scrupal, a Scalable Reactive Content Management System.                                       *
 *                                                                                                                    *
 * Copyright © 2015 Reactific Software LLC                                                                            *
 *                                                                                                                    *
 * Licensed under the Apache License, Version 2.0 (the "License");  you may not use this file                         *
 * except in compliance with the License. You may obtain a copy of the License at                                     *
 *                                                                                                                    *
 *        http://www.apache.org/licenses/LICENSE-2.0                                                                  *
 *                                                                                                                    *
 * Unless required by applicable law or agreed to in writing, software distributed under the                          *
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,                          *
 * either express or implied. See the License for the specific language governing permissions                         *
 * and limitations under the License.                                                                                 *
 **********************************************************************************************************************/

package scrupal.store.reactivemongo

/**
 * Created by reidspencer on 11/11/14.
 */

import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

import org.joda.time.Instant
import reactivemongo.bson.DefaultBSONHandlers

import scala.annotation.tailrec
import scala.concurrent.duration.Duration

import rxmongo.bson._
import scrupal.utils.{PathWalker, ScrupalComponent}

/** Interface To Settings
  * This defines the interface to value extraction from some cache of settings
  */
trait BSONSettingsInterface {

  def settingsDefault : BSONObject
  def settings : BSONObject

  def keySet : Set[String] = ???
  def entrySet: Set[BSONValue] = ???


  def getString(path: String) : Option[String]
  def getBoolean(path: String) : Option[Boolean]
  def getByte(path: String) : Option[Byte]
  def getInt(path: String) : Option[Int]
  def getLong(path: String) : Option[Long]
  def getDouble(path: String) : Option[Double]
  def getNumber(path: String) : Option[Number]
  def getInstant(path: String) : Option[Instant]
  def getDuration(path: String) : Option[Duration]
  def getMilliseconds(path: String) : Option[Long]
  def getMicroseconds(path: String) : Option[Long]
  def getNanoseconds(path: String) : Option[Long]

  def getSettings(path: String): Option[BSONSettings] = ???

  def getStrings(path: String): Option[Seq[String]] = ???
  def getBooleans(path: String): Option[Seq[Boolean]] = ???
  def getBytes(path: String) : Option[Seq[Long]] = ???
  def getInts(path: String): Option[Seq[Int]] = ???
  def getLongs(path: String): Option[Seq[Long]] = ???
  def getDoubles(path: String): Option[Seq[Double]] = ???
  def getNumbers(path: String): Option[Seq[Number]] = ???
  def getInstants(path: String): Option[Seq[Instant]] = ???
  def getDurations(path: String): Option[Seq[Duration]] = ???

  def setValues(values: BSONDocument) : Unit = ???
  def setString(path: String, value: String) : Unit = ???
  def setBoolean(path: String, value: Boolean) : Unit = ???
  def setByte(path: String, value: Byte) : Unit = ???
  def setInt(path: String, value: Int) : Unit = ???
  def setLong(path: String, value: Long) : Unit = ???
  def setDouble(path: String, value: Double) : Unit = ???
  def setNumber(path: String, value: Number) : Unit = ???
  def setInstance(path: String, value: Instant) : Unit = ???
  def setDuration(path: String, value: Duration) : Unit = ???

  def setStrings(path: String, value: Seq[String]) : Unit = ???
  def setBooleans(path: String, value: Seq[Boolean]) : Unit = ???
  def setBytes(path: String, value: Seq[Byte]) : Unit = ???
  def setDoubles(path: String, value: Seq[Double]) : Unit = ???
  def setInts(path: String, value: Seq[Int]) : Unit = ???
  def setLongs(path: String, value: Seq[Long]) : Unit = ???
  def setNumbers(path: String, value: Seq[Number]) : Unit = ???
  def setInstants(path: String, value: Seq[Instant]) : Unit = ???
  def setDurations(path: String, value: Seq[Duration]) : Unit = ???

  def toConfig : com.typesafe.config.Config = ???
}

/**
 * Created by reidspencer on 11/10/14.
 */
abstract class BSONSettingsImpl extends ScrupalComponent with BSONSettingsInterface with DefaultBSONHandlers {

//  implicit val bsonStringReader = BSONStringHandler.asInstanceOf[BSONReader[BSONValue,String]]

  private val settingsValue : AtomicReference[BSONObject] = new AtomicReference[BSONObject]( BSONObject() )

  def settings : BSONDocument = settingsValue.get()

  def getString(path: String) : Option[String] =
    BSONPathWalker(path,settingsValue.get()).map{ s ⇒ s.asInstanceOf[BSONString].as[String] }
  def getBoolean(path: String) : Option[Boolean] =
    BSONPathWalker(path, settingsValue.get()).map { b ⇒ b.asInstanceOf[BSONBoolean].as[Boolean] }
  def getByte(path: String) : Option[Byte] =
    BSONPathWalker(path, settingsValue.get()).map { b ⇒ b.asInstanceOf[BSONInteger].value.toByte }

  def getInt(path: String) : Option[Int] =
    BSONPathWalker(path,settingsValue.get()).map { i ⇒ i.asInstanceOf[BSONInteger].as[Int] }
  def getLong(path: String) : Option[Long] =
    BSONPathWalker(path, settingsValue.get()).map { l ⇒ l.asInstanceOf[BSONLong].as[Long] }
  def getDouble(path: String) : Option[Double] =
    BSONPathWalker(path, settingsValue.get()).map { l ⇒ l.asInstanceOf[BSONDouble].as[Double] }
  def getNumber(path: String) : Option[Number] =
    BSONPathWalker(path, settingsValue.get()).map {
      case BSONInteger(i: Int) ⇒ Integer.valueOf(i)
      case BSONLong(l: Long) ⇒ java.lang.Long.valueOf(l)
      case BSONDouble(d) ⇒ java.lang.Double.valueOf(d)
      case BSONBoolean(b) ⇒ java.lang.Integer.valueOf(if (b) 1 else 0)
    }
  def getInstant(path: String) : Option[Instant] =
    BSONPathWalker(path, settingsValue.get()).map { i ⇒ new Instant(i.asInstanceOf[BSONLong].value) }
  def getDuration(path: String) : Option[Duration] =
    BSONPathWalker(path, settingsValue.get()).map { d ⇒ Duration(d.asInstanceOf[BSONLong].value, TimeUnit.NANOSECONDS) }
  def getMilliseconds(path: String) : Option[Long] = getDuration(path).map { d ⇒ d.toMillis }
  def getMicroseconds(path: String) : Option[Long] = getDuration(path).map { d ⇒ d.toMicros }
  def getNanoseconds(path: String) : Option[Long] = getDuration(path).map { d ⇒ d.toNanos }

  @tailrec
  private def newValue(f : (BSONDocument) ⇒ BSONDocument) : Unit = {
    val orig = settingsValue.get()
    val newV = f(orig)
    if (!settingsValue.compareAndSet(orig, newV))
      newValue(f)
  }
  override def setValues(values: BSONDocument) : Unit = {
    settingsValue.set(values)
  }
  override def setString(path: String, value: String) : Unit = {
    newValue { orig ⇒ orig.add(path → BSONString(value)) }
  }
  override def setBoolean(path: String, value: Boolean) : Unit = {
    newValue { orig ⇒ orig.add(path → BSONBoolean(value)) }
  }
  override def setByte(path: String, value: Byte) : Unit = {
    newValue { orig ⇒ orig.add(path → BSONInteger(value)) }
  }
  override def setInt(path: String, value: Int) : Unit = {
    newValue { orig ⇒ orig.add(path → BSONInteger(value)) }
  }
  override def setLong(path: String, value: Long) : Unit = {
    newValue { orig ⇒ orig.add(path → BSONLong(value)) }
  }
  override def keySet : Set[String] = settingsValue.get().value.keySet.toSet
  override def entrySet: Set[BSONValue] = settingsValue.get().value.values.toSet

  // TODO: Implement remaining methods of the BSONSettingsInterface trait
}

class BSONSettings(
  val initialValues: BSONObject,
  val settingsDefault: BSONObject = BSONObject()
  ) extends BSONSettingsImpl {
  setValues(initialValues)
}

object BSONPathWalker extends PathWalker[BSONDocument,BSONArray,BSONValue] {
  protected def isDocument(v: BSONValue) : Boolean = v.code == 0x03
  protected def isArray(v: BSONValue) : Boolean = v.code == 0x04
  protected def asArray(v: BSONValue) : BSONArray = v.asInstanceOf[BSONArray]
  protected def asDocument(v: BSONValue) : BSONDocument = v.asInstanceOf[BSONDocument]
  protected def indexDoc(key: String, d: BSONDocument) : Option[BSONValue] = d.get(key)
  protected def indexArray(index: Int, a: BSONArray) : Option[BSONValue] = {
    if (index < 0 || index > a.length) None else Some(a.values(index))
  }
  protected def arrayLength(a: BSONArray) : Int = a.length
  def apply(path: String, doc: BSONDocument) : Option[BSONValue] = lookup(path, doc)
}


object BSONSettings {

  def apply(values: BSONDocument, defaults: BSONDocument = BSONObject()) = new BSONSettings(values, defaults)
  def unapply(bs: BSONSettings) : Option[(BSONDocument, BSONDocument)] = Some(bs.settings → bs.settingsDefault)

  implicit val ConfigurationHandler = Macros.handler[BSONSettings]

  val Empty = BSONSettings(BSONObject(), BSONObject())
}
