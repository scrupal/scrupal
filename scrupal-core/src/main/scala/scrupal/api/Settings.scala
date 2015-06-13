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

import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

import org.joda.time.Instant
import play.api.libs.json._
import scrupal.api.types._

import scrupal.api.types.StructuredType
import scrupal.utils.Validation._
import scrupal.utils.{ ScrupalComponent, PathWalker, JsonPathWalker }

import scala.annotation.tailrec
import scala.concurrent.duration.Duration

/** Interface To Settings
  * This defines the interface to value extraction from some cache of settings
  */
trait SettingsInterface {

  type ObjectType
  type ArrayType
  type ValueType

  def settingsDefault : ObjectType
  def settings : ObjectType

  def keySet : Set[String] = ???
  def entrySet : Set[ValueType] = ???

  def getString(path : String) : Option[String]
  def getBoolean(path : String) : Option[Boolean]
  def getByte(path : String) : Option[Byte]
  def getInt(path : String) : Option[Int]
  def getLong(path : String) : Option[Long]
  def getDouble(path : String) : Option[Double]
  def getNumber(path : String) : Option[Number]
  def getInstant(path : String) : Option[Instant]
  def getDuration(path : String) : Option[Duration]
  def getMilliseconds(path : String) : Option[Long]
  def getMicroseconds(path : String) : Option[Long]
  def getNanoseconds(path : String) : Option[Long]

  def getSettings(path : String) : Option[SettingsInterface] = ???

  def getStrings(path : String) : Option[Seq[String]] = ???
  def getBooleans(path : String) : Option[Seq[Boolean]] = ???
  def getBytes(path : String) : Option[Seq[Long]] = ???
  def getInts(path : String) : Option[Seq[Int]] = ???
  def getLongs(path : String) : Option[Seq[Long]] = ???
  def getDoubles(path : String) : Option[Seq[Double]] = ???
  def getNumbers(path : String) : Option[Seq[Number]] = ???
  def getInstants(path : String) : Option[Seq[Instant]] = ???
  def getDurations(path : String) : Option[Seq[Duration]] = ???

  def setValues(values : ObjectType) : Unit = ???
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

trait JsonSettingsInterface extends SettingsInterface {
  type ObjectType = JsObject
  type ArrayType = JsArray
  type ValueType = JsValue
}

/** Created by reidspencer on 11/10/14.
  */
abstract class JsonSettingsImpl extends ScrupalComponent with JsonSettingsInterface {

  private val settingsValue : AtomicReference[JsObject] = new AtomicReference[JsObject](JsObject(Seq()))

  def settings : JsObject = settingsValue.get()

  def getString(path : String) : Option[String] =
    JsonPathWalker(path, settingsValue.get()).map{ s ⇒ s.asInstanceOf[JsString].as[String] }
  def getBoolean(path : String) : Option[Boolean] =
    JsonPathWalker(path, settingsValue.get()).map { b ⇒ b.asInstanceOf[JsBoolean].as[Boolean] }
  def getByte(path : String) : Option[Byte] =
    JsonPathWalker(path, settingsValue.get()).map { b ⇒ b.asInstanceOf[JsNumber].value.toByte }

  def getInt(path : String) : Option[Int] =
    JsonPathWalker(path, settingsValue.get()).map { i ⇒ i.asInstanceOf[JsNumber].as[Int] }
  def getLong(path : String) : Option[Long] =
    JsonPathWalker(path, settingsValue.get()).map { l ⇒ l.asInstanceOf[JsNumber].as[Long] }
  def getDouble(path : String) : Option[Double] =
    JsonPathWalker(path, settingsValue.get()).map { l ⇒ l.asInstanceOf[JsNumber].as[Double] }
  def getNumber(path : String) : Option[Number] =
    JsonPathWalker(path, settingsValue.get()).map {
      case x : JsNumber ⇒ x.value
    }
  def getInstant(path : String) : Option[Instant] =
    JsonPathWalker(path, settingsValue.get()).map { i ⇒ new Instant(i.asInstanceOf[JsNumber].value) }
  def getDuration(path : String) : Option[Duration] =
    JsonPathWalker(path, settingsValue.get()).map { d ⇒
      Duration(d.asInstanceOf[JsNumber].value.toLong, TimeUnit.NANOSECONDS)
    }
  def getMilliseconds(path : String) : Option[Long] = getDuration(path).map { d ⇒ d.toMillis }
  def getMicroseconds(path : String) : Option[Long] = getDuration(path).map { d ⇒ d.toMicros }
  def getNanoseconds(path : String) : Option[Long] = getDuration(path).map { d ⇒ d.toNanos }

  @tailrec
  private def newValue(f : (JsObject) ⇒ JsObject) : Unit = {
    val orig = settingsValue.get()
    val newV = f(orig)
    if (!settingsValue.compareAndSet(orig, newV))
      newValue(f)
  }
  override def setValues(values : JsObject) : Unit = {
    settingsValue.set(values)
  }
  override def setString(path : String, value : String) : Unit = {
    newValue { orig ⇒ JsObject(orig.value.toSeq :+ (path → JsString(value))) }
  }
  override def setBoolean(path : String, value : Boolean) : Unit = {
    newValue { orig ⇒ JsObject(orig.value.toSeq :+ (path → JsBoolean(value))) }
  }
  override def setByte(path : String, value : Byte) : Unit = {
    newValue { orig ⇒ JsObject(orig.value.toSeq :+ (path → JsNumber(value.toInt))) }
  }
  override def setInt(path : String, value : Int) : Unit = {
    newValue { orig ⇒ JsObject(orig.value.toSeq :+ (path → JsNumber(value))) }
  }
  override def setLong(path : String, value : Long) : Unit = {
    newValue { orig ⇒ JsObject(orig.value.toSeq :+ (path → JsNumber(value))) }
  }
  override def keySet : Set[String] = settingsValue.get().value.keySet.toSet
  override def entrySet : Set[JsValue] = settingsValue.get().value.values.toSet

  // TODO: Implement remaining methods of the JsonSettingsInterface trait
}

trait JsonSettings extends JsonSettingsImpl with JsObjectValidator {
  def settingsType : StructuredType[JsValue]

  def validateElement(ref : SelectedLocation, path: String, v : JsValue) : Results[JsValue] = {
    JsonPathWalker(path, settings) match {
      case None ⇒ StringFailure(ref, settings, s"Path '$path' was not found amongst the values.")
      case Some(bv) ⇒
        TypePathWalker(path, settingsType) match {
          case None ⇒ StringFailure(ref, settings,
            s"Path '$path' exists in configuration but the configuration has no type for it")
          case Some(validator) ⇒ validator.asInstanceOf[Type[JsValue]].validate(ref, bv)
        }
    }
  }
}

/** Settings For Anything That Needs Them
  *
  * Settings define a structured, typesafe way of specifying configuration settings, and other information for some
  * object. Settings can be stored in the database
  * Created by reidspencer on 11/10/14.
  */
case class Settings(
  settingsType : StructuredType[JsValue],
  initialValue : JsObject,
  override val settingsDefault : JsObject = emptyJsObject) extends JsonSettings(initialValue, settingsDefault) with SettingsInterface {
  require(settingsType.size == settingsDefault.value.size)
}

object TypePathWalker extends PathWalker[JsObjectType, JsArrayType, Type[JsValue]] {
  protected def isDocument(v : Type[_]) : Boolean = v.isInstanceOf[JsObjectType]
  protected def isArray(v : Type[_]) : Boolean = v.isInstanceOf[JsArrayType]
  protected def asArray(v : Type[_]) : IndexableType = v.kind match {
    case 'List ⇒ v.asInstanceOf[ListType[_]]
    case 'Set  ⇒ v.asInstanceOf[SetType[_]]
    case _     ⇒ toss("Attempt to coerce a non-array type into an array type")
  }
  protected def asDocument(v : Type[_]) : DocumentType = v.kind match {
    case 'Bundle ⇒ v.asInstanceOf[BundleType]
    case 'Map ⇒ v.asInstanceOf[MapType]
    case 'Node ⇒ v.asInstanceOf[NodeType]
    case _ ⇒ toss("Attempt to coerce a non-array type into an array type")
  }
  protected def indexDoc(key : String, d : MapType[JsValue]) : Option[Type[JsValue]] = d.validatorFor(key)
  protected def indexArray(index : Int, a : JsArrayType) : Option[Type[JsValue]] = Some(a.elemType)
  protected def arrayLength(a : JsArrayType) : Int = Int.MaxValue // WARNING: Really? MaxValue?
  def apply(path : String, doc : DocumentType) : Option[Type[_]] = lookup(path, doc)
}

object Settings {
  // import BSONHandlers._

  //implicit val SettingsHandler = Macros.handler[Settings]

  def apply(cfg : com.typesafe.config.Config) : Settings = ???
  // TODO: Implement conversion of Configuration from Typesafe Config with "best guess" at Type from values

  val Empty = Settings(BundleType.Empty[JsValue], emptyJsObject, emptyJsObject)
}
