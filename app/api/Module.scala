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
import scala.collection.immutable.HashMap

/** A named value for configuring a Module */
case class Setting(
  override val name: Symbol,
  valueType : Type,
  override val description: String
) extends Thing(name, description) {
  def validate(value : JsValue) = valueType.validate(value)
}

/** A named group of settings.
  * This allows settings to be categorized and clustered for better managability.
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

/** Version numbering for Scrupal Modules.
  * Versions are ordered by major and minor number; update is not factored into the ordering as it is intended to be
  * used only for those changes that do not add features but just fix bugs. Consequently if major and minor version
  * number are the same, the two versions are considered equivalent. It is important for module developers to use the
  * version numbering scheme correctly so that incompatibilities between modules are not created. In particular, any
  * time a new version of a module alters or removes functionality, the major version number must be incremented. Also,
  * whenever a new feature is added, not previously available in prior releases, the minor version number must be
  * incremented as long as no functionality has been removed or changed (major change). If a change is neither major (
  * breaks backwards compatibility) nor minor (retains backwards compatibility but adds features), then it is simply an
  * update that fixes a bug or otherwise improves a stable release so only the update number need be incremented.
  * @param major The major version number that identifies a release that provides new major features and breaks
  *              backwards compatibility with prior releases either by changing functionality or removing functionality.
  * @param minor The minor version number that identifies a release that maintains backwards compatibility with
  *              prior releases but provides additional features.
  * @param update The update version number that identifies the fix/patch level without introducing new features
  *               nor breaking backwards compatibility.
  */
case class Version(
  val major : Int,
  val minor : Int,
  val update: Int
) extends Ordered[Version] {
  override def compare(that: Version) : Int = {
    if (this.major != that.major)
      this.major - that.major
    else
      this.minor - that.minor
  }
  override def equals(other: Any) = {
    if (other.isInstanceOf[Version]) {
      val that = other.asInstanceOf[Version]
      (this.major == that.major) && (this.minor == that.minor)
    }
    else
      false
  }
  override def toString = major + "." + minor + "." + update
}


/** A modular plugin to Scrupal to extend its functionality.
  * A module is an object that provides information (data) and functionality (behavior) to Scrupal so that Scrupal can
  * be extended to do things it was not originally invented to do. In fact, all functionality in Scrupal is implemented
  * in this way, even the core part of Scrupal. Only the meta-model to keep track of the information that modules
  * provide is fixed within Scrupal.
  *
  */
abstract class Module(name: Symbol, description: String)
  extends Thing(name, description) with Registrable {

  /** Provide the registration identifier for Registrable
    * Modules are registrable with the [[scrupal.api.Modules]] object. In all cases the identifier they are
    * registered with is the same as the name with which the Module is constructed. Module developers must choose
    * names that do not conflict with each other.
    */
  override val registration_id = name


  /** The version of this module.
    * Whenever a module is changed and released publicly, it must have a new Version. How the Version numbers change
    * depends on the kind of change that was made. See the [[scrupal.api.Version]] class for details on how this
    * should be utilized.
    */
  val version: Version

  /** The version of this module that this version of it obsoletes.
    * A module is not expected to maintain backwards compatibility for all previous versions. Instead, it must maintain
    * backwards compatibility with some previous set of released versions. The `obsoletes` value, then, identifies the
    * latest version which is obsoleted by this version of the module. All module versions subsequent to `obsoletes`
    * are considered to be compatible with `version`. All modules versions prior to and including `obsoletes` are
    * considered to be incompatible with `version` (although, depending on the feature in question,
    * some compatibility may remain).
    */
  val obsoletes: Version

  /** A mapping of the Module's dependencies.
    * The dependencies map provides the version for each named module this module depends on.
    */
  val dependencies : Map[Symbol,Version] = HashMap[Symbol,Version]()

  /** The set of data types this module defines.
    * There should be no duplicate types as there is overhead in managing them. Always prefer to depend on the module
    * that defines the type rather than respecify it here.
    */
  val types = Seq[Type]()

  /** The set of traits this module defines.
    * Traits are the unit of information storage.
    */
  val traits = Seq[Trait]()

  /** The set of entities this module defines.
    * Entities are the unit of information access and composed of traits and actions. The REST API allows users to
    * invoke the actions of an Entity for CRUD and other operations on the entity. The main job of a module is to
    * provide the definition of entities, their traits and actions, and the supporting infrastructure to
    */
  val entities = Seq[Entity]()

  /** The set of handlers for the events this module is interested in.
    * Interest is expressed by providing a handler for each event the module wishes to intercept. When the event occurs
    * the module's handler will be invoked. Multiple modules can register for interest in the same event but there is
    * no defined order in which the handlers are invoked.
    */
  val handlers = Seq[HandlerFor[Event]]()

  /** The set of configuration settings for the Module grouped into named sections */
  val settings = Seq[SettingsGroup]()

  /** Determine compatibility between `this` [[scrupal.api.Module]] and `that`.
    * This module is compatible with `that` if either `that` does not depend on `this` or the version `that` requires
    * comes after the `obsoletes` version of `this`
    * @param that The module that purports to depend on `this` for which compatibility is being checked
    * @return Whether `this` is compatible with `that`
    */
  def isCompatibleWith(that: Module) : Boolean = {
    !that.dependencies.contains(this.name) || {
      val required_version = that.dependencies.get(this.name).get
      required_version > this.obsoletes
    }
  }
}

object Modules extends Registry[Module] {
}
