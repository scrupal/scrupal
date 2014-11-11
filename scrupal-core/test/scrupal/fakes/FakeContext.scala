package scrupal.fakes

import scrupal.core.Scrupal
import scrupal.core.api._

/**
 * Created by reidspencer on 11/9/14.
 */
class FakeContext extends Context {
  val scrupal = new Scrupal(scala.concurrent.ExecutionContext.Implicits.global)
  scrupal.beforeStart()
  override val site = Some(BasicSite('context_site, "ContextSite", "Just For Testing", "localhost"))
}
