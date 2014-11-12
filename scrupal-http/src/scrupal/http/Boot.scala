package scrupal.http

import java.util.concurrent.TimeUnit

import akka.actor.{ActorSystem, Props}
import akka.io.IO
import akka.pattern.ask
import akka.util.Timeout
import scrupal.core.Scrupal
import scrupal.core.api.Site
import scrupal.http.actors.ScrupalServiceActor
import scrupal.utils.{ScrupalComponent, DateTimeHelpers}
import spray.can.Http

import scala.compat.Platform
import scala.concurrent.duration._

/** Boot Main
  * This is the main entry point to Scrupal as it contains the "Main" function provided by the App Scrupal library class.
  * We don't override that class but instead just start whatever is necessary in the constructor of this object.
  * Since we are Spray based that only consists of creating the actor system, the top leel Actor, and binding that
  * actor to the correct HTTP interface and port.
  */
object Boot extends App with ScrupalComponent
{

  // Instantiate the Scrupal object
  val scrupal = new Scrupal

  // Ask Scrupal to do its initialization .. lots of things can go wrong ;)
  val (config,dbContext) = scrupal.beforeStart()

  // Check to make sure everything is ready to run.
  checkReady()

  // we need an ActorSystem to host our application in
  implicit val system = ActorSystem("Scrupal-Http")

  implicit val timeout = Timeout(5.seconds)

  // create and start our service actor
  val service = system.actorOf(Props(classOf[ScrupalServiceActor], scrupal, timeout), ScrupalServiceActor.name)

  val interface = config.getString("scrupal.http.interface").getOrElse("localhost")
  val port = config.getInt("scrupal.http.port").getOrElse(8888)

  // start a new HTTP server on port 8080 with our service actor as the handler
  IO(Http) ? Http.Bind(service, interface, port)

  scrupal.onStart()

  def runDuration = {
    val run_time = Platform.currentTime - executionStart
    val duration = Duration(run_time, TimeUnit.MILLISECONDS)
    DateTimeHelpers.makeDurationReadable(duration)
  }

  def checkReady() = {
    for (s ← Site.all if s.isEnabled) {
      val app_names = for (app ← s.applications if app.isEnabled) yield {
        for (mod ← app.modules if mod.isEnabled) yield {
          val paths = for (ent ← mod.entities if mod.isEnabled) yield {ent.path }
          val distinct_paths = paths.distinct
          if (paths.size != distinct_paths.size) {
            toss(
              s"Cowardly refusing to start with duplicate entity paths in module ${mod.label } in application ${mod
                .label } in site ${s.label }")
          }
        }
        app.label
      }
      val distinct_app_names = app_names.distinct
      if (app_names.size != distinct_app_names.size) {
        toss(s"Cowardly refusing to start with duplicate application names in site ${s.label }")
      }
    }
  }
}
