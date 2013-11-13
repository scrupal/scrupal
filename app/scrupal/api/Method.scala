package scrupal.api

import play.api.libs.json.{JsObject}
import play.api.mvc.Action

/** A method of providing an Action for an Entity
  * Methods bring behavior to entities. Each entity can declare a set of methods that its users can invoke from the
  * REST api, in addition to the standard REST api methods.
  */
trait Method {
  /** Objects mixing in this trait will define apply to implement the Action.
    * Note that the result type is fixed to return a JsObject because Methods are only invokable from the REST api
    * which requires results to be in the form of a JsObject
    * @return The Play Action that results in a JsObject to send to the client
    */
  def apply : Action[JsObject]
}
