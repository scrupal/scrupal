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

import java.net.URL

import scrupal.db.DBContext
import scrupal.utils._

import scala.collection.immutable.HashMap
import scala.concurrent.ExecutionContext


/** A modular plugin to Scrupal to extend its functionality.
  * A module is an object that provides information (data) and functionality (behavior) to Scrupal so that Scrupal can
  * be extended to do things it was not originally invented to do. The scrupal-core module provides the abstractions,
  * such as Module, to make this possible. Everything else Scrupal provides is done as a module that extends the
  * functionality of the API.
  */
trait Module extends Registrable[Module]
                     with Authorable with Describable with Enablee with Enablement[Module] with Settingsable
                     with Versionable with SelfValidator with Bootstrappable
{
  /** The name of the database your module's schema is stored to
    *
    * Generally most modules want to live in the "scrupal" database and most everything else. However,
    * if you override this, your module's own content will be stored in the correspondingly named database.
    */
  val dbName: String = "scrupal"
  def registry = Module
  def asT = this

  def moreDetailsURL : URL

  /** A mapping of the Module's dependencies.
    * The dependencies map provides the version for each named module this module depends on. The default value lists
    * the primary dependency upon the most recent version of the `Core` module. New
    * modules should always depend on the latest version of Scrupal available at the time of their writing to ensure
    * the longest future lifespan before they become obsoleted.
    */
  def dependencies : Map[Identifier,Version] = HashMap('Core -> Version(0,1,0))

  // TODO: Can modules provide modules? def modules : Seq[Module]

  // TODO: Can modules provide applications? def applications : Seq[Application]


  /** The set of nodes this module defines.
    * A node is simply a dynamic content generator. It is a Function0 (no arguments, returns a result) and can be
    * used in templates and other places to generate dynamic content either from a template or directly from code.
    * @return The sequence of nodes defined by this module.
    */
  def nodes: Seq[Node]

  // TODO: Can modules provide instances ?

  /** The set of Features that this Module provides.
    * These features can be enabled and disabled through the admin interface and the module can provide its own
    * functionality for when those events occur. See [[api.Feature]]
    */
  def features : Seq[Feature]


  /** The entities that this module supports.
    * An entity combines together a BundleType for storage, a set of REST API handlers,
    * additional operations that can be requested, and
    */
  def entities : Seq[Entity]

  // TODO: Can modules provide sites ?

  /** The set of data types this module defines.
    * There should be no duplicate types as there is overhead in managing them. Always prefer to depend on the module
    * that defines the type rather than respecify it here. This sequence includes all the Trait and Entity types that
    * the module defines.
    */
  def types: Seq[Type]

  /** The set of handlers for the events this module is interested in.
    * Interest is expressed by providing a handler for each event the module wishes to intercept. When the event occurs
    * the module's handler will be invoked. Multiple modules can register for interest in the same event but there is
    * no defined order in which the handlers are invoked.
    */
  def handlers : Seq[HandlerFor[Event]]

  final def isChildScope(e: Enablement[_]) : Boolean = false

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


  /** Load lazy instantiated objects into memory
    *   This is part of the bootstrapping mechanism
    */
  override protected[scrupal] def bootstrap(config: Configuration) = {

    // Touch the various aspects of the module by by asking for it's id's length.
    // This just makes sure it gets instantiated & registered as well as not being null
    features foreach { feature ⇒
      require(feature != null) ; require(feature.label.length > 0) ; feature.bootstrap(config)
    }
    types    foreach {
      typ     ⇒ require(typ != null)     ; require(typ.label.length > 0)     ; typ.bootstrap(config)
    }
    entities foreach { entity  ⇒
      require(entity != null)  ; require(entity.label.length > 0)  ; entity.bootstrap(config)
    }
    nodes    foreach { node    ⇒
      require(node != null)    ; require(node._id.code == 0x07) ; node.bootstrap(config)
    }
    // FIXME: What about handlers and schemas?
  }

  def validate() : ValidationResult = {
    // TODO: Write the module validator
    None
  }
}

case class BasicModule(
  id: Symbol,
  description: String = "",
  author: String = "",
  copyright: String = "",
  license: OSSLicense = OSSLicense.GPLv3,
  version: Version = Version(0,1,0),
  obsoletes: Version = Version(0,0,0),
  applications : Seq[Application] = Seq(),
  nodes: Seq[Node] = Seq(),
  features : Seq[Feature] = Seq(),
  entities : Seq[Entity] = Seq(),
  types: Seq[Type] = Seq(),
  handlers : Seq[HandlerFor[Event]] = Seq()
) extends Module {
  def moreDetailsURL = new URL("http://example.com")
}

/** Amalgamated information about all registered Modules
  * This object is the Registry of modules. When a [[scrupal.core.api.Module]] is instantiated,
  * it will register itself with this module. Upon registration, the information it provides about the module is
  * amalgamated into this object for use by the rest of Scrupal.
  */
object Module extends Registry[Module]  {

  val registryName = "Modules"
  val registrantsName = "module"


  private[scrupal] def installSchemas(implicit context: DBContext, ec: ExecutionContext) : Unit = {
    // For each module ...
    values foreach { case mod: Module =>
      // In a database session ...
      context.withDatabase(mod.dbName) { implicit db =>
        // For each schema ...
        mod.schemas.foreach { schema: Schema =>
          // Create the schema
          schema.validateSchema
        }
      }
    }
  }

}
