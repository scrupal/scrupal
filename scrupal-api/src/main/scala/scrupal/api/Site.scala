/**********************************************************************************************************************
 * This file is part of Scrupal, a Scalable Reactive Web Application Framework for Content Management                 *
 *                                                                                                                    *
 * Copyright (c) 2015, Reactific Software LLC. All Rights Reserved.                                                   *
 *                                                                                                                    *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance     *
 * with the License. You may obtain a copy of the License at                                                          *
 *                                                                                                                    *
 *     http://www.apache.org/licenses/LICENSE-2.0                                                                     *
 *                                                                                                                    *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed   *
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for  *
 * the specific language governing permissions and limitations under the License.                                     *
 **********************************************************************************************************************/

package scrupal.api

import scrupal.storage.api.Storable
import scrupal.utils._

import scala.util.matching.Regex

/** Site Top Level Object
  * Scrupal manages sites.
  * Created by reidspencer on 11/3/14.
  */
abstract class Site(sym : Identifier)(implicit scr : Scrupal) extends {
  val id : Identifier = sym
  implicit val scrupal: Scrupal = scr
} with Settingsable with SiteProvider[Site] with Storable with Registrable[Site]
  with Nameable with Describable with Modifiable {

  val kind = 'Site
  def registry = scrupal.Sites

  def requireHttps : Boolean = false // = getBoolean("requireHttps").get

  def hostnames : Regex // = getString("host").get

  def themeProvider : String = "bootswatch"

  def themeName : String = "default" // = getString("theme").get

  def applications = forEach[Application] { e ⇒
    e.isInstanceOf[Application] && isEnabled(e, this)
  } { e ⇒
    e.asInstanceOf[Application]
  }

  def isChildScope(e : Enablement[_]) : Boolean = applications.contains(e)
}

/** The Registry of Sites for this Scrupal.
  *
  * This object is the registry of Site objects. When a [[scrupal.api.Site]] is instantiated, it will
  * register itself with this object.
  */
case class SitesRegistry() extends Registry[Site] {
  import scala.language.reflectiveCalls
  val registrantsName : String = "site"
  val registryName : String = "Sites"

  def forHost(hostName : String) : Iterable[Site] = {
    for (
      (id, site) ← _registry if site.hostnames.findFirstMatchIn(hostName).nonEmpty
    ) yield {
      site
    }
  }
}

