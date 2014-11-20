/**********************************************************************************************************************
 * Copyright © 2014 Reactific Software, Inc.                                                                          *
 *                                                                                                                    *
 * This file is part of Scrupal, an Opinionated Web Application Framework.                                            *
 *                                                                                                                    *
 * Scrupal is free software: you can redistribute it and/or modify it under the terms                                 *
 * of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License,   *
 * or (at your option) any later version.                                                                             *
 *                                                                                                                    *
 * Scrupal is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied      *
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more      *
 * details.                                                                                                           *
 *                                                                                                                    *
 * You should have received a copy of the GNU General Public License along with Scrupal. If not, see either:          *
 * http://www.gnu.org/licenses or http://opensource.org/licenses/GPL-3.0.                                             *
 **********************************************************************************************************************/

package scrupal.core.echo

import org.joda.time.DateTime
import scrupal.core.api.{Module, Application}

/** An Echoing Application
  * Almost the simplest of applications to construct this is mostly a test of the fundamentals but may have some
  * utility as heartbeat between servers. This application servers one entity, an echo entity, which takes in
  * requests, formats them into HTML and responds with the content. When you go to the app you see the GET
  * request for the page you requested as the response. It has one special page which allows you to submit a
  * form. Query args are returned if you provide them on the web page. This could also be used for benchmarking.
  * Created by reid on 11/11/14.
  */
object EchoApp extends Application {

  lazy val id: Symbol = 'Echo

  val name: String = "Echo Application"

  val description: String = "An Application For echoing web requests back to your browser"

  val kind: Symbol = 'Echo

  val requiresAuthentication = false

  override def modules: Seq[Module] = Seq(EchoModule)

  def created: Option[DateTime] = Some(new DateTime(2014,11,11,5,53))

  def modified: Option[DateTime] = None

  EchoEntity.enable(this)
  EchoModule.enable(this)

}
