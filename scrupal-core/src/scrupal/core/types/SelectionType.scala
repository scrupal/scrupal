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

package scrupal.core.types

import reactivemongo.bson.{BSONString, BSONValue}
import scrupal.core.api._

case class SelectionType(
  id: Identifier,
  description: String,
  choices: Seq[String]
) extends Type {
  override type ScalaValueType = String
  require(choices.nonEmpty)
  def validate(ref: ValidationLocation, value: BSONValue) : VR = {
    simplify(ref, value, "BSONString") {
      case BSONString(s) if !choices.contains(s) ⇒ Some(s"Invalid choice")
      case BSONString(s) ⇒ None
      case x: BSONValue ⇒ Some("")
    }
  }
}


object Theme_t extends SelectionType('Theme, "Choice of themes", DataCache.themes)

object Site_t extends SelectionType('Site, "Choice of sites", DataCache.sites)

object UnspecificQuantity_t extends SelectionType('UnspecificQuantity,
  "A simple choice of quantities that do not specifically designate a number",
  Seq("None", "Some", "Any", "Both", "Few", "Several", "Most", "Many", "All")
)
