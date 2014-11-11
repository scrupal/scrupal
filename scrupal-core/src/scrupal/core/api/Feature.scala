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
package scrupal.core.api

import reactivemongo.api.DefaultDB
import reactivemongo.api.indexes.{IndexType, Index}
import reactivemongo.bson._
import scrupal.db.{StorableRegistrable, IdentifierDAO}
import scrupal.utils.Registry

/** A Feature of a Module.
  * Features are things that can be enabled or disabled that affect how a Module does its work. Scrupal handles the
  * administration of features for the Module. Modules simply declare the list of features they have and Scrupal
  * deals with the rest. To check for a feature being enabled, just use the [[Feature.apply]] method like so:
  * ```if (Feature('FeatureName))``` Because the if-expression requires a Boolean, the implicit featureToBool will be
  * used. This makes accessing the enabled state of a feature simple.
  */
case class Feature(
  id: Symbol,
  description: String,
  implemented: Boolean = true
) extends StorableRegistrable[Feature] with Describable with Enablable with ModuleOwned {
  def registry = Feature
  def asT = this

  def moduleOf = { Module.all.find(mod ⇒ mod.features.contains(this)) }

  /** Get the name of the feature */
  def name = id.name

  /** Determine if the feature is enabled
    * When you have an object reference for a feature, you can determine its enablement by just using this
    * apply function like so:
    * {{{
    *   if (MyFeature()) { /* feature is enabled */ } else { /* feature is disabled */ }
    * }}}
    * @return
    */
  def apply() : Boolean = isEnabled

}

/** Feature Registry and companion
  * This object provides the registry of feature objects, the default NotAFeature object, and implicit conversions
  * to boolean.
  */
object Feature extends Registry[Feature] {
  override val registrantsName = "feature"
  override def logger_identity = s"${registryName}Registry"
  override val registryName = "Features"
  def enabled(name: Symbol) : Boolean = super.apply(name).getOrElse(NotAFeature).isEnabled

  implicit def featureToBool(f : Feature) : Boolean = f.isEnabled
  implicit def featureToBool(f : Option[Feature]) : Boolean = f.getOrElse(NotAFeature).isEnabled

  case class FeatureDAO(db: DefaultDB) extends IdentifierDAO[Feature] {
    final def collectionName = "features"
    implicit val reader : IdentifierDAO[Feature]#Reader = Macros.reader[Feature]
    implicit val writer : IdentifierDAO[Feature]#Writer  = Macros.writer[Feature]
    override def indices : Traversable[Index] = super.indices ++ Seq(
      Index(key = Seq("module" -> IndexType.Ascending), name = Some("Module"))
    )
  }

  object NotAFeature extends Feature('NotAFeature, "This is not a feature", false) {
    override def apply() = toss(description)
    override def enable() = toss(description)
    override def disable() = toss(description)
    override def enabled(how: Boolean) = toss(description)
  }
}
