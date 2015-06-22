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

import play.api.libs.json.JsValue
import scrupal.api.types.BundleType
import scrupal.utils.Validation

import scala.collection.mutable
import scala.concurrent.duration.Duration
import scala.language.implicitConversions

import scrupal.utils.Validation._

import shapeless._

/** Interface To Settings
  * This defines the interface to value extraction from some cache of settings
  */
trait SettingsInterface extends MapValidator[String,Atom,mutable.HashMap[String,Atom]] {

  type MapType = mutable.HashMap[String,Atom]
  protected val settings : MapType = mutable.HashMap.empty[String,Atom]
  def settingsDefaults : Map[String,Atom]
  def settingsTypes : BundleType

  def toMap(mt: MapType): collection.Map[String, Atom] = mt.toMap

  def validate(name : String) : Results[mutable.HashMap[String,Atom]] = {
    validate(Validation.SimpleLocation(name), settings)
  }

  def validateElement(ref: SelectedLocation[String], k: String, v: Atom): Results[Atom] = {
    settingsTypes.fields.get(k) match {
      case Some(validator) ⇒
        validator.asInstanceOf[Validator[Atom]].validate(ref, v)
      case None ⇒
        StringFailure(ref, v, s"Type validator not found for key $k")
    }
  }

  def entrySet : Iterable[Atom] = settings.values

  def getString(path : String) : Option[String] = settings.get(path).flatMap { _.select[String] }
  def getBoolean(path : String) : Option[Boolean] = settings.get(path).flatMap { _.select[Boolean] }
  def getByte(path : String) : Option[Byte] = settings.get(path).flatMap {_.select[Byte] }
  def getShort(path : String) : Option[Short] = settings.get(path).flatMap {_.select[Short] }
  def getInt(path : String) : Option[Int] = settings.get(path).flatMap {_.select[Int] }
  def getLong(path : String) : Option[Long] = settings.get(path).flatMap {_.select[Long] }
  def getFloat(path : String) : Option[Float] = settings.get(path).flatMap {_.select[Float] }
  def getDouble(path : String) : Option[Double] = settings.get(path).flatMap {_.select[Double] }
  def getInstant(path : String) : Option[Instant] = settings.get(path).flatMap {_.select[Instant] }
  def getDuration(path : String) : Option[Duration] = settings.get(path).flatMap { _.select[Duration] }
  def getMilliseconds(path : String) : Option[Long] = settings.get(path).flatMap { _.select[Duration] } map { x ⇒ x.toMillis }
  def getMicroseconds(path : String) : Option[Long] = settings.get(path).flatMap { _.select[Duration] } map { x ⇒ x.toMicros }
  def getNanoseconds(path : String) : Option[Long]  = settings.get(path).flatMap { _.select[Duration] } map { x ⇒ x.toNanos  }

  def setValues(values : MapType) : Unit = settings.transform { case (k,v) ⇒ values.getOrElse(k,v) }
  def setString(path : String, value : String) : Unit = settings.put(path, value)
  def setBoolean(path : String, value : Boolean) : Unit = settings.put(path, value)
  def setByte(path : String, value : Byte) : Unit = settings.put(path, value)
  def setShort(path : String, value : Short) : Unit = settings.put(path, value)
  def setInt(path : String, value : Int) : Unit = settings.put(path, value)
  def setLong(path : String, value : Long) : Unit = settings.put(path, value)
  def setFloat(path : String, value : Float) : Unit = settings.put(path, value)
  def setDouble(path : String, value : Double) : Unit = settings.put(path, value)
  def setInstance(path : String, value : Instant) : Unit = settings.put(path, value)
  def setDuration(path : String, value : Duration) : Unit = settings.put(path, value)

  def toConfig : com.typesafe.config.Config = ???

}

/** Settings For Anything That Needs Them
  *
  * Settings define a structured, typesafe way of specifying configuration settings, and other information for some
  * object. Settings can be stored in the database
  * Created by reidspencer on 11/10/14.
  */
case class Settings(
  name : String,
  settingsTypes : BundleType,
  initialValue : Map[String,Atom] = Map.empty[String,Atom],
  override val settingsDefaults : Map[String,Atom]) extends SettingsInterface {
  require(settingsTypes.size == settingsDefaults.size)
  require(settingsTypes.size == initialValue.size)

  settings.transform { case (k,v) ⇒ initialValue.getOrElse(k,v) }
}

object Settings {

  def apply(cfg : com.typesafe.config.Config) : Settings = ???
  // TODO: Implement conversion of Configuration from Typesafe Config with "best guess" at Type from values
}
