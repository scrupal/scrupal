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

import scrupal.utils.{ Enablee, Enablement, Registrable, Registry }

abstract class AbstractFeature extends Describable with Enablee with ModuleOwned with Bootstrappable {
  def implemented : Boolean
  def moduleOf = { Module.values.find(mod ⇒ mod.features.contains(this)) }
  override def parent = moduleOf
  override def isEnabled(scope : Enablement[_]) : Boolean = implemented && scope.isEnabled(this)
}

/** A Feature of a Module.
  * Features are things that can be enabled or disabled that affect how a Module does its work. Scrupal handles the
  * administration of features for the Module. Modules simply declare the list of features they have and Scrupal
  * deals with the rest. To check for a feature being enabled, just use the `apply` method like so:
  * `if (Feature('FeatureName))` Because the if-expression requires a Boolean, the implicit featureToBool will be
  * used. This makes accessing the enabled state of a feature simple.
  */
case class Feature(
  id : Symbol,
  description : String,
  override val parent : Option[Module],
  implemented : Boolean = true) extends AbstractFeature with Registrable[Feature] {
  def registry : Registry[Feature] = Feature
  /** Get the name of the feature */
  def name = id.name

  /** Determine if the feature is enabled
    * When you have an object reference for a feature, you can determine its enablement by just using this
    * apply function like so:
    * {{{
    * if (MyFeature()) { /* feature is enabled */ } else { /* feature is disabled */ }
    * }}}
    * @return
    */
  def apply(scope : Enablement[_]) : Boolean = scope.isEnabled(this)
}

/** Feature Registry and companion
  * This object provides the registry of feature objects, the default NotAFeature object, and implicit conversions
  * to boolean.
  */
object Feature extends Registry[Feature] {
  val registrantsName = "feature"
  val registryName = "Features"

  def enabled(name : Symbol, scope : Enablement[_]) : Boolean = {
    super.apply(name) match {
      case Some(feature) ⇒ scope.isEnabled(feature)
      case None ⇒ false
    }
  }

  def apply(name : Symbol, scope : Enablement[_]) : Boolean = enabled(name, scope)
  def apply(f : Feature, scope : Enablement[_]) : Boolean = enabled(f.id, scope)

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
