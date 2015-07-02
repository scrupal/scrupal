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

import javax.inject.{Inject, Singleton}

import scrupal.api.Site
import scrupal.storage.api.StoreContext
import play.api.Configuration
import play.api.inject.ApplicationLifecycle

import scala.concurrent.Future

@Singleton
case class WelcomeScrupal @Inject()(
  override val name: String = "WelcomeToScrupal",
  config: Configuration,
  lifecycle: ApplicationLifecycle
  ) extends scrupal.core.http.CoreScrupal(name, config, lifecycle) {

  override protected def load(config: Configuration, context: StoreContext): Future[Seq[Site]] = {
    super.load(config, context).map { sites ⇒
      if (sites.isEmpty) {
        val ws = new WelcomeSite(Symbol(name + "-Welcome"))(this)
        ws.enable(this)
        // AdminApp.enable(ws)
        // CoreModule.enable(AdminApp)
        Seq(ws)
      } else {
        sites
      }
    }
  }
}
