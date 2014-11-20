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

import com.typesafe.config.{ConfigRenderOptions, ConfigValue}
import scrupal.api.{Site, Application, Scrupal}
import scrupal.core.welcome.WelcomeSite
import scrupal.utils.Configuration

class CoreScrupal extends Scrupal {

  override def open() = {
    // Make sure that we registered the CoreModule as 'Core just to make sure it is instantiated at this point
    require(CoreModule.id == 'Core)

    super.open()
  }

  override def getAppEntities : SiteAppEntityMap = {
    val apiAppEntities = super.getAppEntities
    if (apiAppEntities.isEmpty){
      val site = new WelcomeSite
      site.enable(this)
      Map ( site.name → ( site → site.getApplicationMap ) )
    }
    else
      apiAppEntities.toMap
  }

  override def onLoadConfig(config: Configuration): Configuration = {
    val new_config = super.onLoadConfig(config)

    // Make things from the configuration override defaults and database read settings
    // Features
    new_config.getBoolean("scrupal.developer.mode") map   { value => CoreFeatures.DevMode.enable(this, value) }
    new_config.getBoolean("scrupal.developer.footer") map { value => CoreFeatures.DebugFooter.enable(this, value) }
    new_config.getBoolean("scrupal.config.wizard") map    { value => CoreFeatures.ConfigWizard.enable(this, value) }

    // return the configuration
    new_config
  }

}
