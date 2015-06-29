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
import scrupal.doc.DocumentationProvider
import scrupal.admin.AdminApp
import scrupal.config.ConfigWizard

import scala.util.matching.Regex

import scrupal.api._

case class WelcomeSite(sym : Identifier)(implicit scrpl: Scrupal) extends Site(sym) {
  val name : String = "Welcome To Scrupal"
  val description : String = "The default 'Welcome To Scrupal' site that is built in to Scrupal"
  val modified : Option[DateTime] = Some(DateTime.now)
  val created : Option[DateTime] = Some(new DateTime(2014, 11, 18, 17, 40))
  override val themeName = "cyborg"
  def hostNames : Regex = ".*".r

  val documentation = DocumentationProvider()
  val welcomeSite = WelcomeSiteProvider()
  // val adminApp = AdminApp()
  val configWizard = ConfigWizard()

  override def delegates : Iterable[Provider] = {
    super.delegates ++ Iterable(
      welcomeSite, documentation, configWizard //, adminApp
    )
  }

  val coreModule = scrupal.Modules('Core)
  val echoEntity = coreModule.flatMap { m ⇒ m.entity('Echo) }
  enable(coreModule)
  enable(echoEntity)
}
