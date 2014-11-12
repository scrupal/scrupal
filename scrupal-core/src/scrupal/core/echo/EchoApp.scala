package scrupal.core.echo

import org.joda.time.DateTime
import scrupal.core.api.{BasicApplication, Module, Application}

/** An Echoing Application
  * Almost the simplest of applications to construct this is mostly a test of the fundamentals but may have some
  * utility as heartbeat between servers. This application servers one entity, an echo entity, which takes in
  * requests, formats them into HTML and responds with the content. When you go to the app you see the GET
  * request for the page you requested as the response. It has one special page which allows you to submit a
  * form. Query args are returned if you provide them on the web page. This could also be used for benchmarking.
  * Created by reid on 11/11/14.
  */
object EchoApp extends Application {

  lazy val id: Symbol = 'Echo

  val name: String = "Echo Application"

  val description: String = "An Application For echoing web requests back to your browser"

  val kind: Symbol = 'Echo

  val requiresAuthentication = false

  def modules: Seq[Module] = Seq(EchoModule)

  def created: Option[DateTime] = Some(new DateTime(2014,11,11,5,53))

  def modified: Option[DateTime] = None
}
