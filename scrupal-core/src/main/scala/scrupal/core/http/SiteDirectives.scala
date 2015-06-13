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

package scrupal.core.http

import scrupal.core.api.{ Scrupal, Site }
import spray.routing.Directives._
import spray.routing._

/** Spray Routing Directives For Scrupal Sites
  * This provides a few routing directives that deal with sites being enabled and requiring a certain scheme
  */
trait SiteDirectives {

  def siteScheme(site : Site) = {
    scheme("http").hrequire { hnil ⇒ !site.requireHttps } |
      scheme("https").hrequire { hnil ⇒ site.requireHttps }
  }

  def siteEnabled(site : Site, scrupal : Scrupal) = {
    validate(site.isEnabled(scrupal), s"Site '${site.name}' is disabled.")
  }

  def scrupalIsReady(scrupal : Scrupal) = {
    validate(scrupal.isReady, s"Scrupal is not configured!")
  }

  /*

  schemeName { scheme ⇒
    reject(ValidationRejection(s"Site '${site._id.name}' does not support scheme'$scheme'"))
  }
}

    require
    validate(!site.requireHttps, s"Site '${site._id.name}' does not permit https.") {
      extract (ctx ⇒ provide(site) )
    }
  } ~
    scheme("https") {
      validate(site.requireHttps, s"Site '${site._id.name}' requires https.") { hnil ⇒
        extract(ctx ⇒ site)
      }
    } ~
}
*/

  def site(scrupal : Scrupal) : Directive1[Site] = {
    hostName.flatMap { host : String ⇒
      val sites = Site.forHost(host)
      if (sites.isEmpty)
        reject(ValidationRejection(s"No site defined for host '$host'."))
      else {
        val site = sites.head
        siteScheme(site) & siteEnabled(site, scrupal) & extract(ctx ⇒ site)
      }
    }
  }
}

