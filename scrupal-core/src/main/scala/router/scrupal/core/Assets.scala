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

package router.scrupal.core

import javax.inject.{Inject, Singleton}

import play.api.http.HttpErrorHandler
import play.api.mvc.{RequestHeader, Action}
import scrupal.api.DataCache
import scrupal.utils.ScrupalUtilsInfo


@Singleton
class Assets @Inject()(errHandler: HttpErrorHandler) extends controllers.Assets(errHandler) {

  def mkPrefix(subdir: String, lib: String = "scrupal-core", version : String = ScrupalUtilsInfo.version) = {
    s"/META-INF/resources/webjars/$lib/$version$subdir"
  }

  def at(file: String) = super.at(mkPrefix(""), file, aggressiveCaching=true)

  def js(file: String) = super.at(mkPrefix("/javascripts"), file)

  def img(file: String) = super.at(mkPrefix("/images"), file)

  def css(file: String) = super.at(mkPrefix("/stylesheets"), file)

  def bsjs(file: String)= {
    versionMap.get("bootswatch") match {
      case Some(version) ⇒
        val path = s"/META-INF/resources/webjars/bootswatch/$version/2/js"
        super.at(path, file)
      case None ⇒
        Action { req: RequestHeader ⇒ NotFound(s"Bootswatch version") }

    }
  }

  def theme(theme: String) = {
    DataCache.themes.get(theme) match {
      case Some(thm) ⇒
        val path = s"/META-INF/resources/webjars/bootswatch/${ScrupalUtilsInfo.bootswatch_version}/${theme.toLowerCase}"
        super.at(path, "bootstrap.min.css")
      case None ⇒
        Action { req: RequestHeader ⇒ NotFound(s"Theme '$theme'") }
    }
  }

  val versionMap = Map(
    "font-awesome" → "4.3.0-3", "marked" → "0.3.2", "jquery" → "2.1.4", "bootswatch" → "3.3.1+2"
  )

  def webjar(library: String, file: String) = {
    versionMap.get(library) match {
      case Some(version) ⇒
        val path = s"/META-INF/resources/webjars/$library/$version"
        super.at(path, file)
      case None ⇒
        Action { req: RequestHeader ⇒ NotFound(s"WebJar '$library'") }
    }
  }

}
