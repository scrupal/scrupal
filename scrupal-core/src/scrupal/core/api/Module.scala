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

import scrupal.core.CoreModule
import scrupal.core.echo.EchoModule
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
  */
trait Module extends Registrable[Module]
                     with Authorable with Describable with Enablable with Settingsable
                     with Versionable with SelfValidator with Bootstrappable
{
  /** The name of the database your module's schema wants to live in
    *
    * Generally most modules want to live in the "scrupal" database along with Core and most everything else. However,
    * if you override this, your module's own content will be
    */
  val dbName: String = "scrupal"
  def registry = Module
  def asT = this

  def moreDetailsURL : URL

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

  /** The set of nodes this module defines.
    * A node is simply a dynamic content generator. It is a Function0 (no arguments, returns a result) and can be
    * used in templates and other places to generate dynamic content either from a template or directly from code.
    * @return The sequence of nodes defined by this module.
    */
  def nodes: Seq[Node]

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

  /** Load lazy instantiated objects into memory
    *   This is part of the bootstrapping mechanism
    */
  private[scrupal] def bootstrap() = {

    // Touch the various aspects of the module by by asking for it's id's length.
    // This just makes sure it gets instantiated & registered as well as not being null
    features foreach { feature ⇒ require(feature != null) ; require(feature.label.length > 0) ; feature.bootstrap }
    types    foreach { typ     ⇒ require(typ != null)     ; require(typ.label.length > 0)     ; typ.bootstrap }
    entities foreach { entity  ⇒ require(entity != null)  ; require(entity.label.length > 0)  ; entity.bootstrap }
    nodes    foreach { node    ⇒ require(node != null)    ; require(node._id.name.length > 0) ; node.bootstrap }
    // FIXME: What about handlers and schemas?
  }

  def validate() : ValidationResult = {
    // TODO: Write the module validator
    None
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

  /** Process Module Initialization
    * Modules are always defined as singleton objects. As such, they are not instantiated until referenced. Instantiation
    * causes them to be registered. So, we need to reference the modules to get them registered. And, in turn, they need
    * to reference all the objects they use so they can be registered. This logic is handled in this function which is
    * only called by Scrupal.beforeStart after the configuration has been obtained.
    */
  private[scrupal] def bootstrap(modules_to_bootstrap: Seq[String] ) : Unit = {
    // First of all, nothing happens without the CoreModule. Bootstrap it first so we have CoreSchema and other
    // things available
    CoreModule.bootstrap

    // Now that core is done, let's do the standard modules that come with the Core
    EchoModule.bootstrap

    // In case this is a brand new installation with no sites in the database,
    // create the default site by poking it
    require(DefaultSite.label.size > 0)

    // Now we go through the configured modules
    for (class_name ← modules_to_bootstrap) {
      findModuleOnClasspath(class_name) match {
        case Some(module) ⇒
        case None ⇒ log.warn("Could not locate module with class name: " + class_name)
      }
    }
  }

  private[scrupal] def findModuleOnClasspath(name: String) : Option[Module] = {
    None // TODO: Write ClassLoader code to load foreign modules on the classpath - maybe use OSGi ?
  }

  private[scrupal] def installSchemas(implicit context: DBContext) : Unit = {
    // For each module ...
    all foreach { case mod: Module =>
      // In a database session ...
      context.withDatabase(mod.dbName) { implicit db =>
        // For each schema ...
        mod.schemas.foreach { schema: Schema =>
          // Create the schema
          schema.validate
        }
      }
    }
  }

}
