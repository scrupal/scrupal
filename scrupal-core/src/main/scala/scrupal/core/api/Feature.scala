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
package scrupal.core.api

import scrupal.utils.{Enablee, Enablement, Registrable, Registry}

abstract class AbstractFeature extends Describable with Enablee with ModuleOwned with Bootstrappable {
  def implemented : Boolean
  def moduleOf = { Module.values.find(mod ⇒ mod.features.contains(this)) }
  override def parent = moduleOf
  override def isEnabled(scope: Enablement[_]) : Boolean = implemented && scope.isEnabled(this)
}

/** A Feature of a Module.
  * Features are things that can be enabled or disabled that affect how a Module does its work. Scrupal handles the
  * administration of features for the Module. Modules simply declare the list of features they have and Scrupal
  * deals with the rest. To check for a feature being enabled, just use the `apply` method like so:
  * `if (Feature('FeatureName))` Because the if-expression requires a Boolean, the implicit featureToBool will be
  * used. This makes accessing the enabled state of a feature simple.
  */
case class Feature(
  id: Symbol,
  description: String,
  override val parent: Option[Module],
  implemented: Boolean = true
) extends AbstractFeature with Registrable[Feature] {
  def registry: Registry[Feature] = Feature
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
  def apply(scope: Enablement[_]) : Boolean = scope.isEnabled(this)
}

/** Feature Registry and companion
  * This object provides the registry of feature objects, the default NotAFeature object, and implicit conversions
  * to boolean.
  */
object Feature extends Registry[Feature] {
  val registrantsName = "feature"
  val registryName = "Features"

  def enabled(name: Symbol, scope: Enablement[_]) : Boolean = {
    super.apply(name) match {
      case Some(feature) ⇒ scope.isEnabled(feature)
      case None ⇒ false
    }
  }

  def apply(name: Symbol, scope: Enablement[_]) : Boolean = enabled(name, scope)
  def apply(f: Feature, scope: Enablement[_]) : Boolean = enabled(f.id, scope)


  /*
  case class FeatureDAO(db: DefaultDB) extends IdentifierDAO[Feature] {
    final def collectionName = "features"
    implicit val reader : IdentifierDAO[Feature]#Reader = Macros.reader[Feature]
    implicit val writer : IdentifierDAO[Feature]#Writer  = Macros.writer[Feature]
    override def indices : Traversable[Index] = super.indices ++ Seq(
      Index(key = Seq("module" -> IndexType.Ascending), name = Some("Module"))
    )
  }
*/
}
