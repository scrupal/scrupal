package scrupal.http.controllers

import scrupal.utils.{Registrable, Registry}
// import play.twirl.api._
// import spray.httpx.TwirlSupport
import spray.routing.Route

/**
 * Created by reidspencer on 10/29/14.
 */
trait Controller extends /* TwirlSupport with */ Registrable[Controller]  {
  val context_path: String
  val priority: Int
  def routes: Route
  def registry: Registry[Controller] = Controller
  def asT: Controller = this
}

abstract class BasicController(val context_path: String, val controller_priority: Int = 0) extends Controller

object Controller extends Registry[Controller] {
  override val registryName: String = "Controllers"
  override val registrantsName: String = "Controller"


}
