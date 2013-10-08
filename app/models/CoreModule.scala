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
import play.api.libs.json._
import java.net.{URISyntaxException, URI}
import scrupal.api.Setting
import scrupal.api.StringType
import scrupal.api.Trait
import play.api.libs.json.JsString
import play.api.libs.json.JsSuccess



/**
 * One line sentence description here.
 * Further description here.
 */
object CoreModule extends Module('Core, "Scrupal's Core module for basic site functionality.") {
  override val registration_id = 'Core
  override val majorVersion = 0
  override val minorVersion = 1
  override val updateVersion = 0
  val majorIncompatible = 0
  val minorIncompatible = 0

  override val types = Seq[Type](
    Identifier_t, DomainName_t, URI_t, IPv4Address_t
  )

  override val settings = Seq[SettingsGroup] (
    SettingsGroup('site, "Settings related to the site that Scrupal is providing.", Seq(
      Setting('siteName, DomainName_t, "The name of the site Scrupal will be serving"),
      Setting('port, TcpPort_t, "The TCP Port number on which Scrupal should listen")
      )
    )
  )

  override val traits = Seq[Trait]()
  override val entities = Seq[Entity]()
  override val events = Events.ValueSet ()

}
