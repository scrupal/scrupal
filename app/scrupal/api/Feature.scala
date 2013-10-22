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

import scrupal.utils.{Registry, Registrable}

/** A Feature of a Module.
  * Features are things that can be enabled or disabled that affect how a Module does its work. Scrupal handles the
  * administration of features for the Module. Modules simply declare the list of features they have and Scrupal
  * deals with the rest. To check for a feature being enabled, just use the FEature.apply method like so:
  * ```if (Feature('FeatureName))``` Because the if-expression requires a Boolean, the implicit featureToBool will be
  * used. This makes accessing the enabled state of a feature simple.
  */
class Feature(
  override val id: Symbol,
  val description: String,
  private var enabled: Boolean = true
) extends Registrable  {
  def enable() = enabled = true
  def disable() = enabled = false
  def isEnabled = enabled

  // Register ourself with the Feature Registry
  Feature.register(this)
}

object Feature extends Registry[Feature] {
  override val registrantsName = "feature"
  override val registryName = "Features"
  def apply(name: Symbol, description: String, enabled: Boolean) = new Feature(name, description, enabled)
  implicit def featureToBool(f : Feature) : Boolean = f.isEnabled
  implicit def featureToBool(f : Option[Feature]) : Boolean = f.getOrElse(NotAFeature).isEnabled

  object NotAFeature extends Feature('NotAFeature,"This is not a feature", false)
}

