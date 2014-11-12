package scrupal.core.echo

import java.net.URL

import scrupal.core.api._
import scrupal.utils.{OSSLicense, Version}

/** The Echo Module
  * This is the module the accompanies the EchoApp. It provides the EchoEntity that implements the main features of
  * the application
  * Created by reid on 11/11/14.
 */
object EchoModule extends Module {
  lazy val id: Symbol = 'Echo
  val description: String = "A module that provides the EchoEntity which echos HTTP request details"
  val version: Version = Version(0, 1, 0)
  val obsoletes: Version = Version(0, 0, 0)
  val author: String = "Reid Spencer'"
  val copyright: String = "Copyright © 2014, 2015 Reid Spencer. All Rights Reserved."
  val license = OSSLicense.GPLv3

  def moreDetailsURL: URL = new URL("http://scrupal.org/modules/Echo")

  /** The entities that this module supports.
    * An entity combines together a BundleType for storage, a set of REST API handlers,
    * additional operations that can be requested, and
    */
  def entities: Seq[Entity] = Seq(EchoEntity)

  /** The set of nodes this module defines.
    * A node is simply a dynamic content generator. It is a Function0 (no arguments, returns a result) and can be
    * used in templates and other places to generate dynamic content either from a template or directly from code.
    * @return The sequence of nodes defined by this module.
    */
  def nodes: Seq[Node] = Seq()

  /** The set of Features that this Module provides.
    * These features can be enabled and disabled through the admin interface and the module can provide its own
    * functionality for when those events occur. See [[scrupal.core.api.Feature]]
    */
  def features: Seq[Feature] = Seq()

  /** The set of data types this module defines.
    * There should be no duplicate types as there is overhead in managing them. Always prefer to depend on the module
    * that defines the type rather than respecify it here. This sequence includes all the Trait and Entity types that
    * the module defines.
    */
  def types: Seq[Type] = Seq()

  /** The set of handlers for the events this module is interested in.
    * Interest is expressed by providing a handler for each event the module wishes to intercept. When the event occurs
    * the module's handler will be invoked. Multiple modules can register for interest in the same event but there is
    * no defined order in which the handlers are invoked.
    */
  def handlers: Seq[HandlerFor[Event]] = Seq()

  override private[scrupal] def bootstrap() = {
    // Do what every module does
    super.bootstrap
    // Most applications are not Objects and thus are loaded from database but EchoApp, built-in, is different
    // We need to "touch" in order to get it loaded
    require(EchoApp.label.length > 0)
  }
}

