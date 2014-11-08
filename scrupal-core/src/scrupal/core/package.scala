package scrupal

import akka.actor.ActorSystem
import reactivemongo.bson.BSONObjectID

/** Scrupal Utilities
  * This Scrupal library contains utilities and miscellaneous facilities that don't fit in with other libraries.
  * Inclusion in this library means it has no dependence on any other scrupal library.
  */
package object core {

  val Core: Symbol = 'Core

  implicit val system: ActorSystem = ActorSystem("Scrupal-Core")

}
