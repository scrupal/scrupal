package scrupal.http.directives

import scrupal.core.api.Feature
import spray.routing._
import spray.routing.Directives._

/** Spray Directives For Scrupal Features
 *
 */
trait FeatureDirectives {

  def feature(theFeature: Feature) : Directive0 = {
    if (theFeature.implemented) {
      if (theFeature.isEnabled) {
        pass
      } else {
        reject(ValidationRejection(s"Feature '${theFeature.name}' of module '${theFeature.moduleOf}' is not enabled."))
      }
    } else {
      reject(ValidationRejection(s"Feature '${theFeature.name}' of module '${theFeature.moduleOf}' is not implemented."))
    }
  }

}
