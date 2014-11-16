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

package scrupal.http.controllers

import scrupal.core.Scrupal
import scrupal.core.api.{Identifier, Type, Module}
import scrupal.utils.{Registrable, Registry}
import spray.routing.{Route, Directives}

/** Abstract Controller
  *
  * A controller in scrupal is a route provider and action generator. It provides a route object which
  * should separate valid from invalid routes and generate Actions for the valid ones.
  *
  * Created by reidspencer on 10/29/14.
  */
trait Controller extends /* TwirlSupport with */ Registrable[Controller] with Directives {

  /** The priority of this controller for routing
    * This affects the controller's placement in the list of controllers that form the route processing.
    * Lower numbers increase the priority of the controller and move it towards the start of the list.
    * Scrupal's own controllers work within a -100 to 100 range and it is recommended that other
    * controllers operate within that same range. Note that this just helps optimize the processing
    * of routes.
    */
  def priority: Int

  /** The routes that this controller provides
    * Note that this does not include checking of the context path. That will have already done before the
    * routes here are invoked. So, this should only check within that 'context"
    */
  def routes(scrupal: Scrupal): Route

  /** Required method for registration */
  def registry: Registry[Controller] = Controller
  def asT: Controller = this

  def spaces2underscores(what: String) = what.replaceAll(" ","_")

  def modules = Module.values
  def moduleNames : Seq[String]  = Module.values map { module: Module => module.label }
  def moduleTypeNames(mod:Module)  : Seq[String] = mod.types map { typ => typ.label }

  def types       : Seq[Type]    = Module.values flatMap { module => module.types }
  def typeNames   : Seq[String]  = types map { typ : Type => typ.label }
}

abstract class BasicController(val id : Identifier, val priority: Int = 0) extends Controller

object Controller extends Registry[Controller] {
  override val registryName: String = "Controllers"
  override val registrantsName: String = "Controller"


}
