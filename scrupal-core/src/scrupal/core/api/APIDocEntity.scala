/**********************************************************************************************************************
 * Copyright © 2014 Reactific Software LLC                                                                            *
 *                                                                                                                    *
 * This file is part of Scrupal, an Opinionated Web Application Framework.                                            *
 *                                                                                                                    *
 * Scrupal is free software: you can redistribute it and/or modify it under the terms                                 *
 * of the GNU General Public License as published by the Free Software Foundation,                                    *
 * either version 3 of the License, or (at your option) any later version.                                            *
 *                                                                                                                    *
 * Scrupal is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;                               *
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                          *
 * See the GNU General Public License for more details.                                                               *
 *                                                                                                                    *
 * You should have received a copy of the GNU General Public License along with Scrupal.                              *
 * If not, see either: http://www.gnu.org/licenses or http://opensource.org/licenses/GPL-3.0.                         *
 **********************************************************************************************************************/

package scrupal.core.api

import scrupal.core.types.BundleType


/** API Documentation Helper For An Entity
  *
  * Entities provide an API by which their functionality is invoked. Being able to utilize the API depends on
  * documentation that corresponds to the Entity API and is accurate and up to date. To this end, this class provides
  * the means to auto-generated API Documentation for entities and by extending it enhance that documentation.
  *
  * @tparam E - The Entity type this EntityAPIDoc documents
  */
class APIDocEntity[E <: Entity](id: Symbol) extends Entity(id) {
  def instanceType: BundleType = ???

  def kind: Symbol = ???

  def description: String = ???

}
