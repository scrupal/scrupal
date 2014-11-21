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

import org.joda.time.DateTime
import scrupal.api.{Entity, Module, Application}
import scrupal.core.CoreModule.PageEntity

/** Documentation Application For Marked
  *
  * This application allows packaged documentation formatted in marked (like markdown) notation to be presented
  * within a context. This allows module vendors or site owners to easily maintain documentation in plain marked
  * format and have it added easily to their site. Since the marked documents need no processing, they should be
  * placed somewhere under the "public" directory in your module. All this application needs is a name and a
  * relative path to the top level directory of your documentation and it will do the rest.
  */
case class MarkedDocApp(
  id: Symbol,
  name: String,
  description: String,
  document_root: String,
  modified: Option[DateTime] = Some(DateTime.now()),
  created: Option[DateTime] = Some(DateTime.now())
) extends Application {
  def kind : Symbol = 'MarkedDoc


}
