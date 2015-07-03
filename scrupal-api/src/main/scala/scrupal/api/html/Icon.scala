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

package scrupal.api.html

import scrupal.api.Html._

import scalatags.Text.all._

case class Icon(name : String) extends SimpleGenerator {
  val h = i(cls := s"icon-${name.toLowerCase.replaceAll("[^a-z]","-")}")
  def apply() : Contents = { Seq(h) }
}


object Icons {

  lazy val ok = Icon("ok")
  lazy val info = Icon("info")
  lazy val exclamation = Icon("exclamation")
  lazy val exclamation_sign = Icon("exclamation-sign")
  lazy val remove = Icon("remove")
  lazy val warning_sign = Icon("warning_sign")
  lazy val heart = Icon("heart")
  lazy val long_arrow_left = Icon("long_arrow_left")
  lazy val align_center = Icon("align_center")
}
