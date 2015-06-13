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

package scrupal.core.http

import scrupal.core.api.Feature
import scrupal.utils.Enablement
import spray.routing.Directives._
import spray.routing._

/** Spray Directives For Scrupal Features
 *
 */
trait FeatureDirectives {

  def feature(theFeature: Feature, scope: Enablement[_]) : Directive0 = {
    if (theFeature.implemented) {
      if (theFeature.isEnabled(scope)) {
        pass
      } else {
        reject(ValidationRejection(s"Feature '${theFeature.name}' of module '${theFeature.moduleOf}' is not enabled."))
      }
    } else {
      reject(ValidationRejection(s"Feature '${theFeature.name}' of module '${theFeature.moduleOf}' is not implemented."))
    }
  }

}
