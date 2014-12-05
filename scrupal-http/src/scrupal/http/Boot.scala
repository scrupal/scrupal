/**********************************************************************************************************************
 * Copyright © 2014 Reactific Software LLC                                                                            *
 *                                                                                                                    *
 * This file is part of Scrupal, an Opinionated Web Application Framework.                                            *
 *                                                                                                                    *
 * Scrupal is free software: you can redistribute it and/or modify it under the terms                                 *
 * of the GNU General Public License as published by the Free Software Foundation,                                    *
 * either version 3 of the License, or (at your option) any later version.                                            *
 *                                                                                                                    *
 * Scrupal is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;                               *
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                          *
 * See the GNU General Public License for more details.                                                               *
 *                                                                                                                    *
 * You should have received a copy of the GNU General Public License along with Scrupal.                              *
 * If not, see either: http://www.gnu.org/licenses or http://opensource.org/licenses/GPL-3.0.                         *
 **********************************************************************************************************************/

package scrupal.http

import java.util.concurrent.TimeUnit

import akka.actor.{ActorSystem, Props}
import akka.io.IO
import akka.pattern.ask
import akka.util.Timeout
import scrupal.api.{Scrupal, Site}
import scrupal.http.actors.ScrupalServiceActor
import scrupal.utils.{ScrupalComponent, DateTimeHelpers}
import spray.can.Http

import scala.compat.Platform
import scala.concurrent.duration._

/** Boot Main
  * This is the main entry point to Scrupal as it contains the "Main" function provided by the App Scrupal library class.
  * We don't override that class but instead just start whatever is necessary in the constructor of this object.
  * Since we are Spray based that only consists of creating the actor system, the top level Actor, and binding that
  * actor to the correct HTTP interface and port.
  */
case class Boot(scrupal: Scrupal) extends ScrupalComponent
{
  // Ask Scrupal to do its initialization .. lots of things can go wrong ;)
  val (config,dbContext) = scrupal.open()

  // Check to make sure everything is ready to run.
  checkReady()

  // we need an ActorSystem to host our application in
  implicit val system = ActorSystem("Scrupal-Http")

  implicit val timeout = Timeout(config.getMilliseconds("scrupal.timeout").getOrElse(8000), TimeUnit.MILLISECONDS)

  // create and start our service actor
  val service = system.actorOf(Props(classOf[ScrupalServiceActor], scrupal, timeout), ScrupalServiceActor.name)

  val interface = config.getString("scrupal.http.interface").getOrElse("0.0.0.0")
  val port = config.getInt("scrupal.http.port").getOrElse(8888)

  log.info(s"Scrupal HTTP starting up. Interface=$interface, Port=$port, Timeout=${timeout}ms")

  val executionStart = Platform.currentTime

  // start a new HTTP server on port 8080 with our service actor as the handler
  IO(Http) ? Http.Bind(service, interface, port)

  scrupal.onStart()

  def runDuration = {
    val run_time = Platform.currentTime - executionStart
    val duration = Duration(run_time, TimeUnit.MILLISECONDS)
    DateTimeHelpers.makeDurationReadable(duration)
  }

  def checkReady() = {
    for (site ← Site.values if site.isEnabled(scrupal)) {
      val app_names = for (app ← site.applications if app.isEnabled(site)) yield {
        for (mod ← app.modules if mod.isEnabled(app)) yield {
          val paths = for (ent ← mod.entities if ent.isEnabled(mod)) yield {ent.singularKey }
          val distinct_paths = paths.distinct
          if (paths.size != distinct_paths.size) {
            toss(
              s"Cowardly refusing to start with duplicate entity paths in module ${mod.label } in application ${mod
                .label } in site ${site.label }")
          }
        }
        app.label
      }
      val distinct_app_names = app_names.distinct
      if (app_names.size != distinct_app_names.size) {
        toss(s"Cowardly refusing to start with duplicate application names in site ${site.label }")
      }
    }
  }
}
