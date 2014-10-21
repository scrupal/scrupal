package scrupal.utils

import org.specs2.mutable.Specification

/** Test Suite for ScrupalComponent
 */
class ScrupalComponentSpec extends Specification {
  "ScrupalComponent" should {
    "supply a logger" in {
      class test extends ScrupalComponent
      val t = new test
      t.log.info("Logger created")
      success
    }
  }
}
