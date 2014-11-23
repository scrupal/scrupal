/**********************************************************************************************************************
 * Copyright © 2014 Reactific Software, Inc.                                                                          *
 *                                                                                                                    *
 * This file is part of Scrupal, an Opinionated Web Application Framework.                                            *
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

import org.joda.time.DateTime
import reactivemongo.bson.BSONDocument
import scrupal.utils.{Configuration, OSSLicense, Patterns, Version}

import scala.concurrent.duration.Duration

trait Authorable {
  def author : String
  def copyright : String
  def license: OSSLicense
}

/** Something that can be created and keeps track of its modification time.
  * For reasons similar to [[scrupal.db.Storable]], the data provided by this trait is accessible to everyone
  * but mutable by only the scrupal package. This limits the impact of making the created_var a var. Creatable uses
  * the same justifications for this design as does [[scrupal.db.Storable]]
  */
trait Creatable  {
  def created : Option[DateTime]
  def isCreated = created.isDefined
  def exists = isCreated
  def canModify = false
}

/** Something that can be modified and keeps track of its times of modification and creation.
  * For reasons similar to [[scrupal.db.Storable]], the data provided by this trait is accessible to everyone
  * but mutable by only the scrupal package. This limits the impact of making the created_var a var. Modifiable uses
  * the same justifications for this design as does [[scrupal.db.Storable]]
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
trait Nameable  {
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
  def settingsType : BundleType = BundleType.Empty
  def settingsDefault : BSONDocument = BSONDocument()
  def settings : Settings = Settings(settingsType, settingsDefault, settingsDefault)
  def validate(doc: BSONDocument) : ValidationResult = settings.apply(doc)
  def validate(path: String) : ValidationResult = settings.validate(path)
  def getString(path: String) : Option[String] = settings.getString(path)
  def getBoolean(path: String) : Option[Boolean] = settings.getBoolean(path)
  def getInt(path: String) : Option[Int] = settings.getInt(path)
  def getLong(path: String) : Option[Long] = settings.getLong(path)
  def getDouble(path: String) : Option[Double] = settings.getDouble(path)
  def getNumber(path: String) : Option[Number] = settings.getNumber(path)
  def getDuration(path: String) : Option[Duration] = settings.getDuration(path)
  def getMilliseconds(path: String) : Option[Long] = settings.getMilliseconds(path)
  def getNanoseconds(path: String) : Option[Long] = settings.getNanoseconds(path)
  def setBoolean(path: String, value: Boolean) : Unit = settings.setBoolean(path, value)
}

/** Something that can be enabled or disabled */
// FIXME: THis is ill-contrived. Enablement could be different depending on context. A feature could be
// enabled in one application but not in another. We need a more general notion of enablement where this
// trait merely registers the object as being a thing that can be enabled or not, and in which contexts.
trait Enablable /* extends Settingsable */
{
  def isEnabled : Boolean = true


  /*
  protected def _enabled : Boolean
  protected def copy_enabled(new_value: Boolean) : this.type

  final val enabled_key = "enabled"
  def settingsType : BundleType = enableSettings
  def settingsDefault : BSONDocument = BSONDocument(Map(enabled_key → BSONBoolean(value=true)))
  def isEnabled = getBoolean(enabled_key).get
  def enable() : this.type = { copy_enabled(new_value=true) }
  def disable() : this.type = { copy_enabled(this }
  /** Determine if the thing is enabled or not with a dynamic value
    *
    * @param how How to check for enablement: true or false
    */
  def enabled(how: Boolean): Unit = isEnabled == how
   */
}

/** Something that contains a path component for a URL */
trait Pathable {
  def path : String
  def pathIsValid : Boolean = Patterns.anchored(Patterns.URLPathable).pattern.matcher(path).matches()
}


/** Something that has a version and can obsolete other version */
trait Versionable {
  def version: Version
  def obsoletes: Version
}

/** Something that has an expiration date */
trait Expirable {
  def expiry : Option[DateTime]
  def expires = expiry.isDefined
  def expired = expiry match {
    case None ⇒ false
    case Some(dt) ⇒ dt.isBeforeNow
  }
  def unexpired : Boolean = !expired

}

/** Something that contains facets */
trait Facetable {
  def facets : Map[String,Facet]
  def facet(name:String): Option[Facet]= facets.get(name)
}

/** Something that participates in runtime bootstrap at startup */
trait Bootstrappable {
  protected[scrupal] def bootstrap(config: Configuration) : Unit = {}
}