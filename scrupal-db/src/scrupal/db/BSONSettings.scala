/**********************************************************************************************************************
 * Copyright © 2014 Reactific Software LLC                                                                            *
 *                                                                                                                    *
 * This file is part of Scrupal, an Opinionated Web Application Framework.                                            *
 *                                                                                                                    *
 * Scrupal is free software: you can redistribute it and/or modify it under the terms                                 *
 * of the GNU General Public License as published by the Free Software Foundation,                                    *
 * either version 3 of the License, or (at your option) any later version.                                            *
 *                                                                                                                    *
 * Scrupal is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;                               *
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                          *
 * See the GNU General Public License for more details.                                                               *
 *                                                                                                                    *
 * You should have received a copy of the GNU General Public License along with Scrupal.                              *
 * If not, see either: http://www.gnu.org/licenses or http://opensource.org/licenses/GPL-3.0.                         *
 **********************************************************************************************************************/

package scrupal.db

/**
 * Created by reidspencer on 11/11/14.
 */

import java.util.concurrent.TimeUnit

import org.joda.time.Instant

import scala.concurrent.duration.Duration

import reactivemongo.bson._
import scrupal.utils.{PathWalker, ScrupalComponent}

/** Interface To Settings
  * This defines the interface to value extraction from some cache of settings
  *
  * FIXME: Extract the BSON Specific parts of the implementations and put in scrupal-db
  */
trait BSONSettingsInterface {

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

  def keySet : Set[String] = ???
  def entrySet: Set[BSONValue] = ???

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
class BSONSettings(
  val values: BSONDocument,
  val defaults: BSONDocument = BSONDocument()
) extends ScrupalComponent with BSONSettingsInterface with DefaultBSONHandlers {

  implicit val bsonStringReader = BSONStringHandler.asInstanceOf[BSONReader[BSONValue,String]]

  def getString(path: String) : Option[String] = BSONPathWalker(path,values).map{ s ⇒ s.asInstanceOf[BSONString].as[String] }
  def getBoolean(path: String) : Option[Boolean] = BSONPathWalker(path, values).map { b ⇒ b.asInstanceOf[BSONBoolean].as[Boolean] }
  def getByte(path: String) : Option[Byte] = BSONPathWalker(path, values).map { b ⇒
    b.asInstanceOf[BSONInteger].value.toByte
  }

  def getInt(path: String) : Option[Int] = BSONPathWalker(path,values).map { i ⇒ i.asInstanceOf[BSONInteger].as[Int] }
  def getLong(path: String) : Option[Long] = BSONPathWalker(path, values).map { l ⇒ l.asInstanceOf[BSONLong].as[Long] }
  def getDouble(path: String) : Option[Double] = BSONPathWalker(path, values).map { l ⇒ l.asInstanceOf[BSONDouble].as[Double] }
  def getNumber(path: String) : Option[Number] = BSONPathWalker(path, values).map {
    case BSONInteger(i) ⇒ Integer.valueOf(i)
    case BSONLong(l) ⇒ java.lang.Long.valueOf(l)
    case BSONDouble(d) ⇒ java.lang.Double.valueOf(d)
    case BSONBoolean(b) ⇒ java.lang.Integer.valueOf(if (b) 1 else 0)
  }
  def getInstant(path: String) : Option[Instant] = BSONPathWalker(path, values).map { i ⇒
    new Instant(i.asInstanceOf[BSONLong].value)
  }
  def getDuration(path: String) : Option[Duration] = BSONPathWalker(path, values).map { d ⇒
    Duration(d.asInstanceOf[BSONLong].value, TimeUnit.NANOSECONDS)
  }
  def getMilliseconds(path: String) : Option[Long] = getDuration(path).map { d ⇒ d.toMillis }
  def getMicroseconds(path: String) : Option[Long] = getDuration(path).map { d ⇒ d.toMicros }
  def getNanoseconds(path: String) : Option[Long] = getDuration(path).map { d ⇒ d.toNanos }


  override def keySet : Set[String] = values.elements.map { x ⇒ x._1 } toSet
  override def entrySet: Set[BSONValue] = values.elements.map { x ⇒ x._2 } toSet

  // TODO: Implement remaining methods of the BSONSettingsInterface trait
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

  def apply(values: BSONDocument, defaults: BSONDocument = BSONDocument()) = new BSONSettings(values, defaults)
  def unapply(bs: BSONSettings) : Option[(BSONDocument, BSONDocument)] = Some(bs.values → bs.defaults)

  implicit val ConfigurationHandler = Macros.handler[BSONSettings]

  val Empty = BSONSettings(BSONDocument(), BSONDocument())
}
