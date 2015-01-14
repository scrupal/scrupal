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

package scrupal.core

import scrupal.core.api.Scrupal
import scrupal.utils.Configuration

object Boot extends Scrupal with App {

  val (config, dbc) = open()

  val http = scrupal.core.http.Boot(this, config)

  http.run()

  override def open() = {
    // Make sure that we registered the CoreModule as 'Core just to make sure it is instantiated at this point
    require(CoreModule.id == 'Core)
    super.open()
  }

  override def onLoadConfig(config: Configuration): Configuration = {
    val new_config = super.onLoadConfig(config)
    CoreModule.bootstrap(new_config)
    new_config
  }

}
