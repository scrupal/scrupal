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

import scrupal.utils.Validation

import scala.collection.mutable
import scala.concurrent.duration.Duration
import scala.language.implicitConversions

import scrupal.utils.Validation._

/** Interface To Settings
  * This defines the interface to value extraction from some cache of settings
  */
trait SettingsInterface {

  type MapType = mutable.HashMap[String,Atom]

  def entries : Iterable[(String,Atom)]

  def getString(path : String) : Option[String]
  def getBoolean(path : String) : Option[Boolean]
  def getByte(path : String) : Option[Byte]
  def getShort(path : String) : Option[Short]
  def getInt(path : String) : Option[Int]
  def getLong(path : String) : Option[Long]
  def getFloat(path : String) : Option[Float]
  def getDouble(path : String) : Option[Double]
  def getInstant(path : String) : Option[Instant]
  def getDuration(path : String) : Option[Duration]
  def getMilliseconds(path : String) : Option[Long]
  def getMicroseconds(path : String) : Option[Long]
  def getNanoseconds(path : String) : Option[Long]

  def setValues(values : MapType) : Unit
  def setString(path : String, value : String) : Unit
  def setBoolean(path : String, value : Boolean) : Unit
  def setByte(path : String, value : Byte) : Unit
  def setShort(path : String, value : Short) : Unit
  def setInt(path : String, value : Int) : Unit
  def setLong(path : String, value : Long) : Unit
  def setFloat(path : String, value : Float) : Unit
  def setDouble(path : String, value : Double) : Unit
  def setInstance(path : String, value : Instant) : Unit
  def setDuration(path : String, value : Duration) : Unit
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
  settingsDefaults : Map[String,Atom] = Map.empty[String,Atom]
) extends SettingsInterface with MapValidator[String,Atom,mutable.HashMap[String,Atom]] {
  private val typeKeys = settingsTypes.fields.keys.toSeq
  require(settingsDefaults.keys.forall { key ⇒ typeKeys.contains(key) },
    "Failed to find a type for each settings default value" )
  require(initialValue.keys.forall { key ⇒ typeKeys.contains(key) },
    "Failed to find a type for each settings initial value")

  protected val settings : MapType = mutable.HashMap.empty[String,Atom]

  settings.transform { case (k,v) ⇒ initialValue.getOrElse(k,v) }

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

  def entries : Iterable[(String,Atom)] = settings.map { case (k,v) ⇒ k -> v }

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

}

/** A Trait for Delegating SettingsInterface to another */
trait SettingsDelegate extends SettingsInterface {
  protected val _settings : SettingsInterface = Settings.empty

  def entries : Iterable[(String,Atom)] = _settings.entries

  def getString(path : String) : Option[String] = _settings.getString(path)
  def getBoolean(path : String) : Option[Boolean] = _settings.getBoolean(path)
  def getByte(path : String) : Option[Byte] = _settings.getByte(path)
  def getShort(path : String) : Option[Short] = _settings.getShort(path)
  def getInt(path : String) : Option[Int] = _settings.getInt(path)
  def getLong(path : String) : Option[Long] = _settings.getLong(path)
  def getFloat(path : String) : Option[Float] = _settings.getFloat(path)
  def getDouble(path : String) : Option[Double] = _settings.getDouble(path)
  def getInstant(path : String) : Option[Instant] = _settings.getInstant(path)
  def getDuration(path : String) : Option[Duration] = _settings.getDuration(path)
  def getMilliseconds(path : String) : Option[Long] = _settings.getMilliseconds(path)
  def getMicroseconds(path : String) : Option[Long] = _settings.getMicroseconds(path)
  def getNanoseconds(path : String) : Option[Long]  = _settings.getNanoseconds(path)

  def setValues(values : MapType) : Unit = _settings.setValues(values)
  def setString(path : String, value : String) : Unit = _settings.setString(path, value)
  def setBoolean(path : String, value : Boolean) : Unit = _settings.setBoolean(path, value)
  def setByte(path : String, value : Byte) : Unit = _settings.setByte(path, value)
  def setShort(path : String, value : Short) : Unit = _settings.setShort(path, value)
  def setInt(path : String, value : Int) : Unit = _settings.setInt(path, value)
  def setLong(path : String, value : Long) : Unit = _settings.setLong(path, value)
  def setFloat(path : String, value : Float) : Unit = _settings.setFloat(path, value)
  def setDouble(path : String, value : Double) : Unit = _settings.setDouble(path, value)
  def setInstance(path : String, value : Instant) : Unit = _settings.setInstance(path, value)
  def setDuration(path : String, value : Duration) : Unit = _settings.setDuration(path, value)

}


object Settings {

  val empty = Settings("empty", BundleType.empty)

  def apply(cfg : com.typesafe.config.Config) : Settings = ???
  // TODO: Implement conversion of Configuration from Typesafe Config with "best guess" at Type from values
}
