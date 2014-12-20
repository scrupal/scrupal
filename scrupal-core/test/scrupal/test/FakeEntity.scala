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

package scrupal.test

import scrupal.core.api.Entity
import scrupal.core.types.BundleType
import scrupal.utils.OSSLicense

/** Mockup of an Entity for testing */
case class FakeEntity(name: String, instanceType: BundleType) extends Entity(Symbol(name)) {

  final val key: String = name

  final val kind: Symbol = 'FakeEntity


  val author: String = "author"

  val copyright: String = "copyright"

  val license: OSSLicense = OSSLicense.GPLv3

  val description: String = name
}
