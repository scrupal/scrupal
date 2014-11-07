/**********************************************************************************************************************
 * This file is part of Scrupal a Web Application Framework.                                                          *
 *                                                                                                                    *
 * Copyright (c) 2014, Reid Spencer and viritude llc. All Rights Reserved.                                            *
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

package scrupal.core.api

import scrupal.core.{EmptyMutableConfiguration, MutableConfiguration}
import scrupal.db.{DBContext, Schema}
import scrupal.utils.{Registrable, Registry, Version}

import scala.collection.immutable.HashMap


/** A modular plugin to Scrupal to extend its functionality.
  * A module is an object that provides information (data) and functionality (behavior) to Scrupal so that Scrupal can
  * be extended to do things it was not originally invented to do. In fact, all functionality in Scrupal is implemented
  * in this way, even the core part of Scrupal. Only the meta-model to keep track of the information that modules
  * provide is fixed within Scrupal.
  * @param id The name of the module
  * @param description A brief description of the module (purpose
  * @param version The version of this module. Whenever a module is changed and released publicly,
  *                it must have a new Version. How the Version numbers change depends on the kind of change that was
  *                made. See the [[scrupal.utils.Version]] class for details on how this should be utilized.
  * @param obsoletes The version of this module that this version of it obsoletes. A module is not expected to
  *                  maintain backwards compatibility for all previous versions. Instead,
  *                  it must maintain backwards compatibility with some previous set of released versions. The
  *                  `obsoletes` value, then, identifies the latest version which is obsoleted by this version of the
  *                  module. All module versions subsequent to `obsoletes` are considered to be compatible with
  *                  `version`. All modules versions prior to and including `obsoletes` are considered to be
  *                  incompatible with `version` (although, depending on the feature in question, some compatibility
  *                  may remain).
  */
abstract class Module(
  val id : Identifier,
  val description: String,
  val version: Version,
  val obsoletes: Version,
  var enabled: Boolean = true
) extends Registrable[Module] with Describable with Enablable with Versionable {
  def registry = Module
  def asT = this

  def moreDetailsURL

  def author : String

  def copyright : String

  def license : String

  /** The set of configuration settings for the Module grouped into named sections.
    * We use the Play! Configuration class here. It is a Scala wrapper around the very lovely Typesafe Configuration
    * library written in Java. We provide, elsewhere, our own way to fetch and restore these things from the database
    * via Json serialization.
    * Java*/
  def config : MutableConfiguration = EmptyMutableConfiguration

  /** A mapping of the Module's dependencies.
    * The dependencies map provides the version for each named module this module depends on. The default value lists
    * the primary dependency upon the most recent version of the `Core` module ([[scrupal.core.CoreModule]]. New
    * modules should always depend on the latest version of Scrupal available at the time of their writing to ensure
    * the longest future lifespan before they become obsoleted.
    */
  def dependencies : Map[Identifier,Version] = HashMap('Core -> Version(0,1,0))

  /** The set of Features that this Module provides.
    * These features can be enabled and disabled through the admin interface and the module can provide its own
    * functionality for when those events occur. See [[scrupal.core.api.Feature]]
    */
  def features : Seq[Feature]

  /** The set of data types this module defines.
    * There should be no duplicate types as there is overhead in managing them. Always prefer to depend on the module
    * that defines the type rather than respecify it here. This sequence includes all the Trait and Entity types that
    * the module defines.
    */
  def types: Seq[Type]

  /** The entities that this module supports.
    * An entity combines together a BundleType for storage, a set of REST API handlers,
    * additional operations that can be requested, and
    */
  def entities : Seq[Entity]

  /** The set of handlers for the events this module is interested in.
    * Interest is expressed by providing a handler for each event the module wishes to intercept. When the event occurs
    * the module's handler will be invoked. Multiple modules can register for interest in the same event but there is
    * no defined order in which the handlers are invoked.
    */
  def handlers : Seq[HandlerFor[Event]]

  /** The set of Database Schemas that this Module defines.
    * Modules may need to have their own special database tables. This is where a module tells Scrupal about those
    * schemas.
    */
  def schemas(implicit dbc: DBContext) : Seq[Schema] = Seq( )


  /** Determine compatibility between `this` [[scrupal.core.api.Module]] and `that`.
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
}

/** Amalgamated information about all registered Modules
  * This object is the Registry of modules. When a [[scrupal.core.api.Module]] is instantiated,
  * it will register itself with this module. Upon registration, the information it provides about the module is
  * amalgamated into this object for use by the rest of Scrupal.
  */
object Module extends Registry[Module] {

  override val registryName = "Modules"
  override val registrantsName = "module"

  private[scrupal] def processModules() : Unit = {
    // For each module ...
    all foreach { case (mod: Module) =>

      // For each type in the module ...
      mod.types foreach { case typ: Type =>
        // Touch the type by asking for it's id's length. This just makes sure it gets instantiated and thus registered
        val symbol: Symbol = typ.id
        require(symbol.name.length > 0)
      }
      mod.features foreach { case feature: Feature =>
        // Touch the feature by asking for it's id's length. This just makes sure it gets instantiated & registered
        require(feature.id.name.length > 0)
      }
      mod.entities foreach { case entity : Entity =>
        require(entity.id.name.length > 0)
      }
    }
  }

  private[scrupal] def installSchemas(implicit context: DBContext) : Unit = {
    // For each module ...
    all foreach { case mod: Module =>
      // In a database session ...
      context.withDatabase { implicit db =>
        // For each schema ...
        mod.schemas.foreach { schema: Schema =>
          // Create the schema
          schema.validate
        }
      }
    }
  }

}
