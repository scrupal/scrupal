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

import akka.actor.{Props, ActorLogging, Actor}
import reactivemongo.bson.BSONDocument
import scrupal.core.BundleType
import scrupal.core.api._
import scrupal.utils.OSSLicense

/** The Echo Entity
  * This is really the heart of the EchoApp. All the requests that get echoed go through here.
  */

object EchoEntity extends Entity {

  def id: Symbol = 'Echo

  def kind: Symbol = 'Echo

  def path: String = "echo"

  def instanceType: BundleType = BundleType.Empty

  def author: String = "Reid Spencer"

  def copyright: String = "© 2014, 2015 Reid Spencer. All Rights Reserved."

  def license: OSSLicense = OSSLicense.GPLv3

  def description: String = "An entity that stores nothing and merely echos its requests"

  override protected val worker =
    system.actorOf(Props(classOf[EchoWorker]), "EchoWorker")

  class EchoWorker extends Actor with ActorLogging {
    def receive : Receive = {
      // TODO: Implement Entity.receive to process messages
      case a: Action[_, _] =>
      case Create(id: String, instance: BSONDocument, ctxt: Context) =>
      case Retrieve(id: String, ctxt: Context) =>
        val result = new HTMLResult(scrupal.core.echo.html.retrieve(id)(ctxt))
        sender ! result
      case Update(id: String, fields: BSONDocument, ctxt: Context) =>
      case Delete(id: String, ctxt: Context) =>
      case Query(fields: BSONDocument, ctxt: Context) =>
      case Option(id: String, option: String, ctxt: Context) =>
      case Get(id: String, what: String, data: BSONDocument, ctxt: Context) =>
      case Put(id: String, what: String, data: BSONDocument, ctxt: Context) =>
      case AddFacet(id: String, name: String, facet: Facet, ctxt: Context) =>
    }
  }


}
