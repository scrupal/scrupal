package scrupal.web

import akka.actor.Actor
import scrupal.core.Scrupal
import scrupal.web.controllers.{WebJarsController, AssetsController, Controller}
import scrupal.utils.ScrupalComponent
import spray.http.MediaTypes._
import spray.routing._
import spray.routing.Directives._

// we don't implement our route structure directly in the service actor because
// we want to be able to test it independently, without having to spin up an actor
class ScrupalServiceActor extends Actor with ScrupalService {

  // the HttpService trait defines only one abstract member, which
  // connects the services environment to the enclosing actor or test
  def actorRefFactory = context

  val assets = new AssetsController
  val webjars = new WebJarsController

  val the_router = createRouter

  log.warn("Router: " + the_router)

  // this actor only runs our route, but you could add
  // other things here, like request stream processing
  // or timeout handling
  def receive = runRoute(the_router)
}


/** The top level Service For Scrupal
  * This trait is mixed in to the ScrupalServiceActor and provides the means by which all the controller's routes are
  */
trait ScrupalService extends HttpService with ScrupalComponent  {

  def createRouter : Route = {

    // First, validate that there are no duplicate controller path contexts
    val routeNames = Controller.all.map { cntrlr => cntrlr.context_path }
    val distinct_names = routeNames.distinct

    if (distinct_names.size != routeNames.size)
      toss("Cowardly refusing to create router with conflicting controller context paths:" + routeNames)

    // Fold all the controller routes into one big one
    val sorted_controllers = Controller.all.sortBy{ c => c.controller_priority }

    val base_routing = check { Scrupal.isConfigured } {}

    sorted_controllers.foldLeft[Route](reject) { (route,ctrlr) =>
      route ~ pathPrefix(ctrlr.context_path) { ctrlr.routes }
    }
  }

  /**
   * Called when an HTTP request has been received.
   *
   * The default is to use the application router to find the appropriate action.
   *
   * @param request the HTTP request header (the body has not been parsed yet)
   * @return an action to handle this request - if no action is returned, a 404 not found result will be sent to client
   * @see onActionNotFound
   */
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


  val myRoute =
    path("") {
      get {
        respondWithMediaType(`text/html`) { // XML is marshalled to `text/xml` by default, so we simply override here
          complete {
            <html>
              <body>
                <h1>Hello, this is <i>Scrupal under spray-can</i>!</h1>
                <p>Running for: <i>{Boot.runDuration}</i></p>
              </body>
            </html>
          }
        }
      }
    }
}

