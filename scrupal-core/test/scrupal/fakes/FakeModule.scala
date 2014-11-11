package scrupal.fakes

import java.net.URL

import scrupal.core.api._
import scrupal.utils.{OSSLicense, Version}

/** Make Module Creation More Light weight
  * This just just adds boilerplate and defaults to make instantiation easier
 */
abstract class AbstractFakeModule(
  id: Symbol,
  dbName: String
)  extends Module {
  val description = "Fake Module"
  val version = Version(0,1,0)
  val obsoletes =Version(0,0,0)
  def handlers : Seq[HandlerFor[Event]] = Seq()

  override def moreDetailsURL: URL = new URL("No URL, Fake Module")
  override def author: String = "No author, Fake Module"
  override def copyright: String = "No copyright, Fake Module"
  override def license = OSSLicense.GPLv3
}

/** Fake Module
  * The typical case where we just want to specify an id and override the few things we want to test.
 * Created by reidspencer on 11/7/14.
 */
case class FakeModule(
  override val id: Symbol,
  override val dbName: String,
  override val version: Version=Version(0,1,0),
  override val obsoletes: Version=Version(0,0,0),
  features: Seq[Feature] = Seq(),
  types : Seq[Type] = Seq(),
  entities : Seq[Entity] = Seq(),
  nodes: Seq[Node] = Seq(),
  override val handlers : Seq[HandlerFor[Event]] = Seq()
) extends AbstractFakeModule(id, dbName) {

}
