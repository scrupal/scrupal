package scrupal.http.controllers

import scrupal.core.api.{Type, Module}
import scrupal.utils.{Registrable, Registry}
// import play.twirl.api._
// import spray.httpx.TwirlSupport
import spray.routing.Route

/** Abstract Controller
  *
  * A controller in scrupal is a route provider and action generator. It provides a route object which
  * should separate valid from invalid routes and generate Actions for the valid ones.
  *
  * Created by reidspencer on 10/29/14.
  */
trait Controller extends /* TwirlSupport with */ Registrable[Controller]  {

  /** The contextual path prefix
    * This is the first element of the path for any requests having to do with this controller.
    *
    */
  val context_path: String

  /** The priority of this controller for routing
    * This affects the controller's placement in the list of controllers that form the route processing.
    * Lower numbers increase the priority of the controller and move it towards the start of the list.
    * Scrupal's own controllers work within a -100 to 100 range and it is recommended that other
    * controllers operate within that same range. Note that this just helps optimize the processing
    * of routes.
    */
  val priority: Int

  /** The routes that this controller provides
    * Note that this does not include checking of the context path. That will have already done before the
    * routes here are invoked. So, this should only check within that 'context"
    */
  def routes: Route

  /** Required method for registration */
  def registry: Registry[Controller] = Controller
  def asT: Controller = this

  def spaces2underscores(what: String) = what.replaceAll(" ","_")

  def modules = Module.all
  def moduleNames : Seq[String]  = Module.all map { module: Module => module.label }
  def moduleTypeNames(mod:Module)  : Seq[String] = mod.types map { typ => typ.label }

  def types       : Seq[Type]    = Module.all flatMap { module => module.types }
  def typeNames   : Seq[String]  = types map { typ : Type => typ.label }
}

abstract class BasicController(val context_path: String, val controller_priority: Int = 0) extends Controller

object Controller extends Registry[Controller] {
  override val registryName: String = "Controllers"
  override val registrantsName: String = "Controller"


}
