package scrupal.http.actors

import akka.actor.{Props, ActorRef, Actor}
import akka.util.Timeout
import scrupal.core.Scrupal
import scrupal.core.api.HttpContext
import scrupal.http.controllers.Controller
import scrupal.http.directives.SiteDirectives
import scrupal.utils.ScrupalComponent
import spray.http.MediaTypes._
import spray.routing.Route
import spray.routing._

import scala.util.{Failure, Success, Try}

object ScrupalServiceActor {
  def props(scrupal: Scrupal)(implicit askTimeout: Timeout): Props =
    Props(classOf[ScrupalServiceActor], scrupal, askTimeout)
  def name = "Scrupal-Service"
}

// we don't implement our route structure directly in the service actor because
// we want to be able to test it independently, without having to spin up an actor
class ScrupalServiceActor(val scrupal: Scrupal, implicit val askTimeout: Timeout) extends Actor with ScrupalService {

  // the HttpService trait defines only one abstract member, which
  // connects the services environment to the enclosing actor or test
  def actorRefFactory = context

  // val assets = new AssetsController
  // val webjars = new WebJarsController

  val the_router = createRouter(scrupal)

  log.warn("Router: " + the_router)

  // this actor only runs our route, but you could add
  // other things here, like request stream processing
  // or timeout handling
  def receive = runRoute(the_router)
}


/** The top level Service For Scrupal
  * This trait is mixed in to the ScrupalServiceActor and provides the means by which all the controller's routes are
  */
trait ScrupalService extends HttpService with ScrupalComponent with SiteDirectives {

  def createRouter(scrupal: Scrupal) (implicit ask_timeout: Timeout) : Route = {
    Try {
      // Fold all the controller routes into one big one, sorted by priority
      val sorted_controllers = Controller.all.sortBy { c => c.priority}

      if (sorted_controllers.isEmpty)
        toss("No controllers found.")

      // Very first thing we want to always do is make sure Scrupal Is Ready
      val base_routing = scrupalIsReady(scrupal)

      // Now construct the routes from the prioritized set of controllers we found
      sorted_controllers.foldLeft[Route](reject) { (route, ctrlr) =>
        route ~ pathPrefix(ctrlr.context_path) {
          ctrlr.routes(scrupal)
        }
      }
    } match {
      case Success(r) ⇒ r
      case Failure(xcptn) ⇒
        path("") {
          get {
            respondWithMediaType(`text/html`) { // XML is marshalled to `text/xml` by default, so we simply override here
              requestInstance { request =>
                complete {
                  val context = HttpContext(request)
                  _root_.scrupal.http.views.html.RouteConstructionFailure(xcptn)(context).toString()
                }
              }
            }
          }
        }
    }
  }

  /*
   * Called when an HTTP request has been received.
   *
   * The default is to use the application router to find the appropriate action.
   *
   * @param request the HTTP request header (the body has not been parsed yet)
   * @return an action to handle this request - if no action is returned, a 404 not found result will be sent to client
   * @see onActionNotFound
  override def onRouteRequest(request: RequestHeader): Option[play.api.mvc.Handler] = {
    if (ScrupalIsConfigured || pathsOkayWhenUnconfigured.findFirstMatchIn(request.path).isDefined ) {
      Logger.trace("Standard Routing for: " + request.path)
      DefaultGlobal.onRouteRequest(request)
    } else {
      Logger.trace("Configuration Routing for: " + request.path)
      Some(scrupal.controllers.ConfigWizard.configure())
    }
  }
  private val pathsOkayWhenUnconfigured = "^/(assets/|webjars/|configure|reconfigure|doc|scaladoc)".r
   */

}

