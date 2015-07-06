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

import java.time.Instant

import play.api.routing.sird._

import scrupal.api._
import scrupal.admin.AdminApp
import scrupal.config.ConfigWizard
import scrupal.core.nodes.HtmlNode
import scrupal.doc.DocumentationProvider

import scala.util.matching.Regex


case class WelcomeSite(sym : Identifier)(implicit scrpl: Scrupal) extends Site(sym) {
  val name : String = "Welcome To Scrupal"
  val description : String = "The default 'Welcome To Scrupal' site that is built in to Scrupal"
  val modified : Option[Instant] = Some(Instant.now)
  val created : Option[Instant] = Some(Instant.parse("2014-11-18T17:40:00.00Z"))
  override val themeName = "Cyborg"
  def hostNames : Regex = ".*".r

  val documentation = DocumentationProvider()
  // val adminApp = AdminApp()
  val configWizard = ConfigWizard()

  override def delegates : Iterable[Provider] = {
    super.delegates ++ Iterable(
      documentation, configWizard //, adminApp
    )
  }

  val coreModule = scrupal.Modules('Core)
  val echoEntity = coreModule.flatMap { m ⇒ m.entity('Echo) }
  enable(coreModule)
  enable(echoEntity)

  val WelcomeSiteRoot = NodeReactor(
    HtmlNode("WelcomeSiteRoot", "Main index page for Welcome To Scrupal Site",
      WelcomePageTemplate
    )
  )

  override def provide: ReactionRoutes = super.provide.orElse {
    case GET(p"/") ⇒
      WelcomeSiteRoot
  }
}
