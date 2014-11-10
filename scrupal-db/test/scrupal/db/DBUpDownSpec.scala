package scrupal.db

import org.specs2.mutable.Specification

/**
 * Created by reidspencer on 11/9/14.
 */
class DBUpDownSpec extends Specification {

  "DBContext" should {
    "startup and shutdown only once" in {
      DBContext.startup()
      DBContext.startup()
      DBContext.numberOfStartups must beEqualTo(2)
      DBContext.isStartedUp must beTrue
      DBContext.shutdown()
      DBContext.numberOfStartups must beEqualTo(1)
      DBContext.isStartedUp must beTrue
      DBContext.shutdown()
      DBContext.numberOfStartups must beEqualTo(0)
      DBContext.isStartedUp must beFalse
    }
  }
}
