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

abstract class DataCache {

  def update(scrupal: Scrupal, schema: Schema)
}

object DataCache extends DataCache {

  private var _themes = Seq.empty[String]

  def themes : Seq[String] =  _themes

  def update(scrupal: Scrupal, schema: Schema) = {
    _themes = Seq("amelia", "cyborg", "default", "readable") // FIXME: allow new themes from theme providers in modules
    _sites = Site.values.map { site ⇒ site.name }
  }

  private var _sites = Seq.empty[String]
  def sites : Seq[String] = _sites
}
