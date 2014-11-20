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

package scrupal.test

import java.net.URL

import scrupal.api._
import scrupal.utils.{OSSLicense, Version}

/** Make Module Creation More Light weight
  * This just just adds boilerplate and defaults to make instantiation easier
 */
abstract class AbstractFakeModule(
  id: Symbol,
  dbName: String
)  extends Module {
  val description = "Fake Module"
  val version = Version(0,1,0)
  val obsoletes =Version(0,0,0)
  def handlers : Seq[HandlerFor[Event]] = Seq()

  override def moreDetailsURL: URL = new URL("No URL, Fake Module")
  override def author: String = "No author, Fake Module"
  override def copyright: String = "No copyright, Fake Module"
  override def license = OSSLicense.GPLv3
}

/** Fake Module
  * The typical case where we just want to specify an id and override the few things we want to test.
 * Created by reidspencer on 11/7/14.
 */
case class FakeModule(
  override val id: Symbol,
  override val dbName: String,
  override val version: Version=Version(0,1,0),
  override val obsoletes: Version=Version(0,0,0),
  features: Seq[Feature] = Seq(),
  types : Seq[Type] = Seq(),
  entities : Seq[Entity] = Seq(),
  nodes: Seq[Node] = Seq(),
  override val handlers : Seq[HandlerFor[Event]] = Seq()
) extends AbstractFakeModule(id, dbName) {

}
