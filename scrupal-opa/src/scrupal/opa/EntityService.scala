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

package scrupal.opa

import scala.concurrent.Future
import scala.scalajs.concurrent.JSExecutionContext.Implicits.runNow
import scala.scalajs.js
import scala.scalajs.js.{ Date, JSON }
import scala.scalajs.js.Any.fromString
import scala.scalajs.js.annotation.JSExportAll
import scala.util.{ Failure, Success, Try }

import com.greencatsoft.angularjs.Factory
import com.greencatsoft.angularjs.core.HttpPromise.promise2future
import com.greencatsoft.angularjs.core.HttpService
import com.greencatsoft.angularjs.{ inject, injectable }

// import prickle.{ Pickle, Unpickle }

@JSExportAll
case class Entity(var title: String, var completed: Boolean = false, id: Long = -1)

/*
@injectable("$entityService")
class EntityService(val http: HttpService) {
  require(http != null, "Missing argument 'http'.")

  def query(): Future[Seq[Entity]] = flatten {
  }

  def create(task: Entity): Future[Entity] = flatten {
  }

  def retrieve(id: String) : Future[Entity] = flatten {

  }

  def update(task: Entity): Future[Entity] = flatten {
    require(task != null, "Missing argument 'task'.")
  }

  def delete(id: Long): Future[Unit] = http.delete(s"/api/todos/$id")

  def clearAll(): Future[Unit] = http.post("/api/todos/clearAll")

  def markAll(completed: Boolean): Future[Unit] =
    http.post(s"/api/todos/markAll?completed=${!completed}")

  protected def parameterizeUrl(url: String, parameters: Map[String, Any]): String = {
    require(url != null, "Missing argument 'url'.")
    require(parameters != null, "Missing argument 'parameters'.")

    parameters.foldLeft(url)((base, kv) =>
      base ++ { if (base.contains("?")) "&" else "?" } ++ kv._1 ++ "=" + kv._2)
  }

  protected def flatten[T](future: Future[Try[T]]): Future[T] = future flatMap {
    case Success(s) => Future.successful(s)
    case Failure(f) => Future.failed(f)
  }
}

object EntityServiceFactory extends Factory[EntityService] {

  override val name = "$entityService"

  @inject
  var http: HttpService = _

  override def apply(): EntityService = new EntityService(http)
}
*/
