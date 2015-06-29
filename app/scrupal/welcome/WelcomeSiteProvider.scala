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

package scrupal.welcome

import org.joda.time.DateTime
import play.api.routing.sird._
import scrupal.api.{NodeReactor, Provider}
import scrupal.core.nodes.HtmlNode

case class WelcomeSiteProvider() extends Provider {

  val WelcomeSiteRoot = NodeReactor(
    HtmlNode("WelcomeSiteRoot", "Main index page for Welcome To Scrupal Site",
      WelcomePageTemplate,
      modified = Some(DateTime.now),
      created = Some(new DateTime(2014, 11, 18, 18, 0))
    )
  )

  def provide: ReactionRoutes = {
    case GET(p"/") ⇒ WelcomeSiteRoot
  }
}
