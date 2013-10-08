/**********************************************************************************************************************
 * This file is part of Scrupal a Web Application Framework.                                                          *
 *                                                                                                                    *
 * Copyright (c) 2013, Reid Spencer and viritude llc. All Rights Reserved.                                            *
 *                                                                                                                    *
 * Scrupal is free software: you can redistribute it and/or modify it under the terms                                 *
 * of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License,   *
 * or (at your option) any later version.                                                                             *
 *                                                                                                                    *
 * Scrupal is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied      *
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more      *
 * details.                                                                                                           *
 *                                                                                                                    *
 * You should have received a copy of the GNU General Public License along with Scrupal. If not, see either:          *
 * http://www.gnu.org/licenses or http://opensource.org/licenses/GPL-3.0.                                             *
 **********************************************************************************************************************/

package scrupal.api

import scrupal.utils.{Registry, Registrable}

import play.api.libs.json._
import play.api.libs.json.JsObject
import play.api.data.validation.ValidationError

/** A named value for configuring a Module */
case class Setting(
  override val name: Symbol,
  valueType : Type,
  override val description: String
) extends Thing(name, description) {
  def validate(value : JsValue) = valueType.validate(value)
}

trait ObjectValidator {
  protected def validateObj( value: JsObject)(implicit structure: Map[Symbol,Type]) : JsResult[Boolean] = {
    if (value.value exists {
      case (key,data) => !(structure.contains(Symbol(key)) &&
                           structure.get(Symbol(key)).get.validate(data).asOpt.isDefined)
    })
      JsError(JsPath(), ValidationError("Not all elements validate"))
    else
      JsSuccess(true)
  }
}

/**
 * One line sentence description here.
 * Further description here.
 */
case class SettingsGroup(
  override val name: Symbol,
  override val description: String,
  settings : Seq[Setting]
) extends Thing(name, description) with ObjectValidator {
  def validate( value : JsObject ) : JsResult[Boolean] = {
    val result : JsResult[Boolean] = ( value \ name.name ) match {
      case a: JsObject => {
        implicit val structure : Map[Symbol,Type] = settings map { s => (s.name, s.valueType) } toMap  ;
        validateObj(a)
      }
      case x: JsUndefined => JsError("Entry " + name.name + " was not found")
      case x => JsError("JsArray was expected, not " + x.getClass.getSimpleName )
    }
    result
  }
}


/**
 * A modular plugin to Scrupal to extend its functionality.
 * A module is an object that provides information (data) and functionality (behavior) to Scrupal so that Scrupal can
 * be extended to do things it was not originally invented to do. In fact, all functionality in Scrupal is implemented
 * in this way, even the core part of Scrupal. Only the meta-model to keep track of the information that modules
 * provide is fixed within Scrupal.
 *
 */
abstract class Module(name: Symbol, description: String)
  extends Thing(name, description) with Registrable {

  val majorVersion : Int
  val minorVersion : Int
  val updateVersion : Int
  val majorIncompatible : Int
  val minorIncompatible : Int

  private var enabled : Boolean = false

  /** The set of configuration settings for the Module grouped into named sections */
  val settings = Seq[SettingsGroup]()
  val types = Seq[Type]()
  val traits = Seq[Trait]()
  val entities = Seq[Entity]()
  val events = Events.ValueSet()

}

object Modules extends Registry[Module] {
}
