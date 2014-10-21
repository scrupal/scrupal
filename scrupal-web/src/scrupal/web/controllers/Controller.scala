package scrupal.web.controllers

import scrupal.utils.{Registrable, Registry}
// import play.twirl.api._
// import spray.httpx.TwirlSupport
import spray.routing.Route

/**
 * Created by reidspencer on 10/29/14.
 */
trait Controller extends /* TwirlSupport with */ Registrable  {
  val context_path: String
  val controller_priority: Int
  def routes: Route
}

abstract class BasicController(val context_path: String, val controller_priority: Int = 0) extends Controller

object Controller extends Registry[Controller] {
  override protected val registryName: String = "Controllers"
  override protected val registrantsName: String = "Controller"


}
