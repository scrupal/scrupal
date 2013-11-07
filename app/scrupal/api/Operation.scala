package scrupal.api

import play.api.libs.json.{JsObject}
import play.api.mvc.Action

/** One line sentence description here.
  * Further description here.
  */
trait Operation {
  def doit : Action[JsObject]
}
