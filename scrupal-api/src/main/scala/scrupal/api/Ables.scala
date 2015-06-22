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

import org.joda.time.DateTime
import play.api.Configuration
import play.api.libs.json.{JsObject, JsValue}
import scrupal.api.types.{StructuredType, BundleType}
import scrupal.utils.{OSSLicense, Patterns, Version}

trait Authorable {
  def author : String
  def copyright : String
  def license : OSSLicense
}

/** Something that can be created and keeps track of its modification time.
  * For reasons similar to [[scrupal.storage.api.Storable]], the data provided by this trait is accessible to everyone
  * but mutable by only the scrupal package. This limits the impact of making the created_var a var. Creatable uses
  * the same justifications for this design as does [[scrupal.storage.api.Storable]]
  */
trait Creatable {
  def created : Option[DateTime]
  def isCreated = created.isDefined
  def exists = isCreated
  def canModify = false
}

/** Something that can be modified and keeps track of its times of modification and creation.
  * For reasons similar to [[scrupal.storage.api.Storable]], the data provided by this trait is accessible to everyone
  * but mutable by only the scrupal package. This limits the impact of making the created_var a var. Modifiable uses
  * the same justifications for this design as does [[scrupal.storage.api.Storable]]
  */
trait Modifiable extends Creatable {
  def modified : Option[DateTime]
  def isModified = modified.isDefined
  def changed = isModified
  override def canModify = true
}

trait ModuleOwned {
  def moduleOf : Option[Module]
}

/** Something that can be named with a String  */
trait Nameable {
  def name : String
  def isNamed : Boolean = name.nonEmpty
}

/** Something that has a short textual description */
trait Describable {
  def description : String
  def isDescribed : Boolean = description.nonEmpty
}

/** Something that has settings that can be specified and changed */
trait Settingsable extends SettingsInterface {
  def settingsTypes : BundleType
  def settingsDefaults : Map[String,Atom]  = Map.empty[String,Atom]
}

/** Something that contains a path component for a URL */
trait Pathable {
  def path : String
  def pathIsValid : Boolean = Patterns.anchored(Patterns.URLPathable).pattern.matcher(path).matches()
}

/** Something that has a version and can obsolete other version */
trait Versionable {
  def version : Version
  def obsoletes : Version
}

/** Something that has an expiration date */
trait Expirable {
  def expiry : Option[DateTime]
  def expires = expiry.isDefined
  def expired = expiry match {
    case None     ⇒ false
    case Some(dt) ⇒ dt.isBeforeNow
  }
  def unexpired : Boolean = !expired

}

/** Something that contains facets */
trait Facetable {
  def facets : Map[String, Facet]
  def facet(name : String) : Option[Facet] = facets.get(name)
}

/** Something that participates in runtime bootstrap at startup */
trait Bootstrappable {
  protected[scrupal] def bootstrap(config : Configuration) : Unit = {}
}
