package scrupal.fakes

import scrupal.core.api._
import scrupal.utils.Version

/** Make Module Creation More Light weight
  * This just just adds boilerplate and defaults to make instantiation easier
 */
abstract class AbstractFakeModule(
  id: Symbol,
  version: Version=Version(0,1,0),
  obsoletes: Version=Version(0,0,0),
  isEnabled : Boolean =true
)  extends Module(id, "Fake Module", version, obsoletes, isEnabled) {

  override def moreDetailsURL: Unit = "No URL, Fake Module"
  override def author: String = "No author, Fake Module"
  override def copyright: String = "No copyright, Fake Module"
  override def license: String = "No license, Fake Module"
}

/** Fake Module
  * The typical case where we just want to specify an id and override the few things we want to test.
 * Created by reidspencer on 11/7/14.
 */
case class FakeModule(
  override val id: Symbol,
  override val version: Version=Version(0,1,0),
  override val obsoletes: Version=Version(0,0,0),
  override val isEnabled: Boolean = true,
  features: Seq[Feature] = Seq(),
  types : Seq[Type] = Seq(),
  entities : Seq[Entity] = Seq(),
  handlers : Seq[HandlerFor[Event]] = Seq()
) extends AbstractFakeModule(id, version, obsoletes, isEnabled) {

}

