package scrupal.web

import java.util.concurrent.TimeUnit

import akka.actor.{ActorSystem, Props}
import akka.io.IO
import akka.pattern.ask
import akka.util.Timeout
import scrupal.utils.DateTimeHelpers
import spray.can.Http

import scala.compat.Platform.currentTime
import scala.concurrent.duration._

object Boot extends App {

  // we need an ActorSystem to host our application in
  implicit val system = ActorSystem("scrupal-web")

  // create and start our service actor
  val service = system.actorOf(Props[ScrupalServiceActor], "MetaService")

  implicit val timeout = Timeout(5.seconds)

  def runDuration = {
    val run_time = currentTime - executionStart
    val duration = Duration(run_time, TimeUnit.MILLISECONDS)
    DateTimeHelpers.makeDurationReadable(duration)
  }

  // start a new HTTP server on port 8080 with our service actor as the handler
  IO(Http) ? Http.Bind(service, interface = "localhost", port = 8888)
}
