package scrupal.db

import org.specs2.mutable.Specification

/**
 * Created by reidspencer on 11/9/14.
 */
class DBUpDownSpec extends Specification {

  "DBContext" should {
    "startup and shutdown only once" in {
      val n = DBContext.numberOfStartups
      DBContext.startup()
      DBContext.startup()
      DBContext.numberOfStartups must beEqualTo(n+2)
      DBContext.isStartedUp must beTrue
      DBContext.shutdown()
      DBContext.numberOfStartups must beEqualTo(n+1)
      DBContext.isStartedUp must beTrue
      DBContext.shutdown()
      DBContext.numberOfStartups must beEqualTo(n+0)
      DBContext.isStartedUp must beEqualTo(n != 0)
    }
  }
}
