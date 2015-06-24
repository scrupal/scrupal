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

import java.net.URL

import play.api.Configuration
import scrupal.storage.api.{SchemaDesign, Store, StoreContext}
import scrupal.utils._

import scala.collection.immutable.HashMap
import scala.concurrent.ExecutionContext

/** A modular plugin to Scrupal to extend its functionality.
  * A module is an object that provides information (data) and functionality (behavior) to Scrupal so that Scrupal can
  * be extended to do things it was not originally invented to do. The scrupal-core module provides the abstractions,
  * such as Module, to make this possible. Everything else Scrupal provides is done as a module that extends the
  * functionality of the API.
  */
abstract class Module(scrpl : Scrupal) extends {
  implicit val scrupal : Scrupal = scrpl
} with Settingsable with Registrable[Module] with Authorable with Describable with Enablee
  with Enablement[Module] with Versionable with Bootstrappable {

  def registry = scrupal.Modules

  /** The name of the database your module's schema is stored to
    *
    * Generally most modules want to live in the "scrupal" database and most everything else. However,
    * if you override this, your module's own content will be stored in the correspondingly named database.
    */
  val dbName : String = "scrupal"
  def asT = this

  def moreDetailsURL : URL

  /** A mapping of the Module's dependencies.
    * The dependencies map provides the version for each named module this module depends on. The default value lists
    * the primary dependency upon the most recent version of the `Core` module. New
    * modules should always depend on the latest version of Scrupal available at the time of their writing to ensure
    * the longest future lifespan before they become obsoleted.
    */
  def dependencies : Map[Identifier, Version] = HashMap('Core -> Version(0, 1, 0))

  // TODO: Can modules provide modules? def modules : Seq[Module]

  // TODO: Can modules provide applications? def applications : Seq[Application]

  /** The set of nodes this module defines.
    * A node is simply a dynamic content generator. It is a Function0 (no arguments, returns a result) and can be
    * used in templates and other places to generate dynamic content either from a template or directly from code.
    * @return The sequence of nodes defined by this module.
    */
  def nodes : Seq[Node]

  // TODO: Can modules provide instances ?

  /** The set of Features that this Module provides.
    * These features can be enabled and disabled through the admin interface and the module can provide its own
    * functionality for when those events occur. See [[scrupal.api.Feature]]
    */
  def features : Seq[Feature]

  /** The entities that this module supports.
    * An entity combines together a BundleType for storage, a set of REST API handlers,
    * additional operations that can be requested, and
    */
  def entities : Seq[Entity]

  def entity(id: Symbol) = entities.find { e ⇒ e.id == id }

  // TODO: Can modules provide sites ?

  /** The set of handlers for the events this module is interested in.
    * Interest is expressed by providing a handler for each event the module wishes to intercept. When the event occurs
    * the module's handler will be invoked. Multiple modules can register for interest in the same event but there is
    * no defined order in which the handlers are invoked.
    */
  def handlers : Seq[EventHandlerFor[Event]]

  final def isChildScope(e : Enablement[_]) : Boolean = false

  /** The set of Database Schemas that this Module defines.
    * Modules may need to have their own special database tables. This is where a module tells Scrupal about those
    * schemas.
    */
  def schemas : Seq[SchemaDesign] = Seq()

  /** Determine compatibility between `this` [[scrupal.api.Module]] and `that`.
    * This module is compatible with `that` if either `that` does not depend on `this` or the version `that` requires
    * comes after the `obsoletes` version of `this`
    * @param that The module that purports to depend on `this` for which compatibility is being checked
    * @return Whether `this` is compatible with `that`
    */
  def isCompatibleWith(that : Module) : Boolean = {
    !that.dependencies.contains(this.id) || {
      val required_version = that.dependencies.get(this.id).get
      required_version > this.obsoletes
    }
  }

  /** Load lazy instantiated objects into memory
    * This is part of the bootstrapping mechanism
    */
  override protected[scrupal] def bootstrap(config : Configuration) = {

    // Touch the various aspects of the module by by asking for it's id's length.
    // This just makes sure it gets instantiated & registered as well as not being null
    features foreach { feature ⇒
      require(feature != null); require(feature.label.length > 0); feature.bootstrap(config)
    }
    entities foreach { entity ⇒
      require(entity != null); require(entity.label.length > 0); entity.bootstrap(config)
    }
    nodes foreach { node ⇒
      require(node != null); node.bootstrap(config)
    }
    // FIXME: What about handlers and schemas?
  }
}

/** The Registry of Modules for this Scrupal.
  *
  * This object is the registry of Module objects. When a [[scrupal.api.Module]] is instantiated, it will
  * register itself with this object.
  */
case class ModulesRegistry() extends Registry[Module] {

  val registryName = "Modules"
  val registrantsName = "module"

  private[scrupal] def installSchemas(implicit context : StoreContext, ec : ExecutionContext) : Unit = {
    // For each module ...
    values foreach { mod : Module ⇒
      // In a database session ...
      context.withStore { implicit store : Store ⇒
        // For each schema ...
        mod.schemas.foreach { design : SchemaDesign ⇒
          if (!store.hasSchema(design.name))
            store.addSchema(design)
          store.withSchema(design.name) { schema ⇒
            schema.construct
          }
        }
      }
    }
  }
}
