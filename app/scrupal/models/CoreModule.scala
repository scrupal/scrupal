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

package scrupal.models

import scrupal.api._
import scala.collection.immutable.HashMap


/** Scrupal's Core Module.
  * This is the base module of all modules. It provides the various abstractions that permit other modules to extend
  * its functionality. The Core module defines the simple, trait, bundle and entity types
 * Further description here.
 */
object CoreModule extends Module (
  'Core,
  "Scrupal's Core module for basic site functionality.",
  Version(0,1,0),
  Version(0,0,0)
) {

  /** The core types that Scrupal provides to all modules */
  override val types = Seq[Type](
    Identifier_t, DomainName_t, TcpPort_t, URI_t, JDBC_URL_t, IPv4Address_t, EmailAddress_t, LegalName_t, SiteInfo_t
  )

  /** Settings for the core
    */
  override val settings = BundleType('Core, "Scrupal Core Module Settings", HashMap[Symbol,TraitType](
    'instance -> TraitType('instance, "Settings for the current instance of Scrupal", HashMap[Symbol,Type](
      'owner -> LegalName_t
    ))
  ))
}
