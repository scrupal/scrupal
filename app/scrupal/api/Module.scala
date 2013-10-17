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

import scrupal.utils.{Jsonic, Registry, Registrable}

import scala.collection.immutable.HashMap
import play.api.{Configuration, Logger}
import play.api.libs.json._
import scala.Some
import com.typesafe.config.ConfigRenderOptions
import org.joda.time.DateTime

case class EssentialModule(
  override val id: ModuleIdentifier,
  val description: String,
  val version: Version,
  val obsoletes: Version,
  val enabled: Boolean
  /* owner: String,
  contact_name: String,
  contact_url: String,
  contact_email: String,
  created: DateTime */
) extends Registrable
{

}

/** A modular plugin to Scrupal to extend its functionality.
  * A module is an object that provides information (data) and functionality (behavior) to Scrupal so that Scrupal can
  * be extended to do things it was not originally invented to do. In fact, all functionality in Scrupal is implemented
  * in this way, even the core part of Scrupal. Only the meta-model to keep track of the information that modules
  * provide is fixed within Scrupal.
  * @param id The name of the module
  * @param description A brief description of the module (purpose
  * @param version The version of this module. Whenever a module is changed and released publicly,
  *                it must have a new Version. How the Version numbers change depends on the kind of change that was
  *                made. See the [[scala.api.Version]] class for details on how this should be utilized.
  * @param obsoletes The version of this module that this version of it obsoletes. A module is not expected to
  *                  maintain backwards compatibility for all previous versions. Instead,
  *                  it must maintain backwards compatibility with some previous set of released versions. The
  *                  `obsoletes` value, then, identifies the latest version which is obsoleted by this version of the
  *                  module. All module versions subsequent to `obsoletes` are considered to be compatible with
  *                  `version`. All modules versions prior to and including `obsoletes` are considered to be
  *                  incompatible with `version` (although, depending on the feature in question, some compatibility
  *                  may remain).
  */
class Module(
  id: ModuleIdentifier,
  description: String,
  version: Version,
  obsoletes: Version,
  enabled : Boolean = false
) extends EssentialModule(id, description, version, obsoletes, enabled)  with Jsonic {

  /** A mapping of the Module's dependencies.
    * The dependencies map provides the version for each named module this module depends on. The default value lists
    * the primary dependency upon the most recent version of the `Core` module ([[scrupal.models.CoreModule]]. New
    * modules should always depend on the latest version of Scrupal available at the time of their writing to ensure
    * the longest future lifespan before they become obsoleted.
    */
  lazy val dependencies : Map[ModuleIdentifier,Version] = HashMap('Core -> Version(0,1,0))

  /** The set of data types this module defines.
    * There should be no duplicate types as there is overhead in managing them. Always prefer to depend on the module
    * that defines the type rather than respecify it here. This sequence includes all the Trait and Entity types that
    * the module defines.
    */
  lazy val types = Seq[Type]()

  /** The set of handlers for the events this module is interested in.
    * Interest is expressed by providing a handler for each event the module wishes to intercept. When the event occurs
    * the module's handler will be invoked. Multiple modules can register for interest in the same event but there is
    * no defined order in which the handlers are invoked.
    */
  lazy val handlers = Seq[HandlerFor[Event]]()


  /** The set of configuration settings for the Module grouped into named sections.
    * We use the Play! Configuration class here. It is a Scala wrapper around the very lovely Typesafe Configuration
    * library written in Java. We provide, elsewhere, our own way to fetch and restore these things from the database
    * via Json serialization.
    * Java*/
  lazy val settings : Configuration = Configuration.empty

  /** The set of Features that this Module provides.
    * These features can be enabled and disabled through the admin interface and the module can provide its own
    * functionality for when those events occur. See [[scrupal.api.Feature]]
    */
  lazy val features : Seq[Feature] = Seq()

  lazy val moreDetailsURL = "http://modules.scrupal.org/doc/" + label

  /** Register this module with the registry of modules */
  Module.register(this)

  /** Determine compatibility between `this` [[scrupal.api.Module]] and `that`.
    * This module is compatible with `that` if either `that` does not depend on `this` or the version `that` requires
    * comes after the `obsoletes` version of `this`
    * @param that The module that purports to depend on `this` for which compatibility is being checked
    * @return Whether `this` is compatible with `that`
    */
  def isCompatibleWith(that: Module) : Boolean = {
    !that.dependencies.contains(this.id) || {
      val required_version = that.dependencies.get(this.id).get
      required_version > this.obsoletes
    }
  }

  override def toJson : JsObject = {
    val d  = Json.toJson( dependencies map { case (n:ModuleIdentifier, v:Version) => (n.name, v.toString)} )
    val t = Json.toJson( types map { typ: Type => typ.label } )
    val e = Json.toJson  (handlers map { hdlr: HandlerFor[Event] => hdlr.category  + ":" + hdlr.label } )
    val s =  settings.underlying.root.render(ConfigRenderOptions.concise())
    Json.obj(
      "kind" -> "Module",
      "id" -> label,
      "description" -> description,
      // "created" -> created,
      "version" -> version.toString,
      "obsoletes" -> obsoletes.toString,
      "enabled" -> enabled,
      "dependencies" -> d,
      "types" -> t,
      "events" -> e,
      "settings" -> s
    )
  }

  override def fromJson(js: JsObject) = ???
}

/** Amalgamated information about all registered Modules
  * This object is the Registry of modules. When a [[scrupal.api.Module]] is instantiated,
  * it will register itself with this module. Upon registration, the information it provides about the module is
  * amalgamated into this object for use by the rest of Scrupal.
  */
object Module extends Registry[Module] {

  override val registryName = "Modules"
  override val registrantsName = "module"

  private[scrupal] def processModules() : Unit = registrants foreach { case (name: ModuleIdentifier, mod: Module) =>
/*    // Put all the types that are not already there into the types map.
    mod.types foreach { typ => Types(typ.id) match {
      case Some(t:Type) => {
        val m : Module = Modules(t.moduleId)

        Logger.warn(
        "Attempt to override type '" + t.label + "' from module " + m.label + " with version from " + "module " + mod
          .name)
      }
      case _ => { /* do nothing on purpose */ }
    }}

    // Register all the handlers, creating the category maps as we go
    // mod.handlers.foreach { handler => Handler(handler) }
  */
  }
  implicit lazy val moduleWrites : Writes[Module] = new Writes[Module]  {
    def writes(m: Module): JsValue = m.toJson
  }
}
