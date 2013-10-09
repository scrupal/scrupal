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


/**
 * One line sentence description here.
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
    Identifier_t, DomainName_t, EmailAddress_t, URI_t, IPv4Address_t, TcpPort_t
  )

  /** Settings for the core
    * */
  override val settings = Seq[SettingsGroup] (
    SettingsGroup('site, "Settings related to the site that Scrupal is providing.", Seq(
      Setting('name, Identifier_t, "The name of the site Scrupal will be serving"),
      Setting('domain, DomainName_t, "The domain name at which Scrupal will receive requests"),
      Setting('port, TcpPort_t, "The TCP Port number on which Scrupal should listen")
      )
    )
  )

}
