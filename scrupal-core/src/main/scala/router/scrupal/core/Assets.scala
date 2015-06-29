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

import controllers.Assets.Asset
import play.api.http.HttpErrorHandler
import play.api.mvc.{Action, AnyContent}

@Singleton
class Assets @Inject()(errHandler: HttpErrorHandler) extends controllers.Assets(errHandler) {

  override def versioned(path: String, file: Asset): Action[AnyContent] = super.versioned(path, file)

  def js(file: String) = super.at("/public/javascripts", file, aggressiveCaching = true)

  def img(file: String) = super.at("/public/images", file, aggressiveCaching = true)

  def css(file: String) = super.at("/public/stylesheets", file, aggressiveCaching = true)

  def thm(theme: String, file: String) = super.at("/public/lib", s"bootswatch-$theme/$file", aggressiveCaching = true)

  def ac(path: String, file: String) = super.at(path, file, aggressiveCaching = true)

}
