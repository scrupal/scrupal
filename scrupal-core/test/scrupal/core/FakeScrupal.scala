package scrupal.core

import org.specs2.execute.AsResult
import scrupal.db.{FakeDBContext, DBContext}
import scrupal.utils.ScrupalComponent

import scala.concurrent.duration.{FiniteDuration, Duration}

/**
 * One line sentence description here.
 * Further description here.
 */
abstract class FakeScrupal(testName: String  = "scrupal", timeout: FiniteDuration = Duration(5,"seconds"))
  extends FakeDBContext("test-" + testName, timeout) with ScrupalComponent {

  // WARNING: Do NOT put anything but def and lazy val because of DelayedInit or app startup will get invoked twice
  // and you'll have a real MESS on your hands!!!! (i.e. no db interaction will work!)

  def withCoreSchema[T : AsResult]( f: CoreSchema => T ) : T = {
    withDBContext { dbContext: DBContext =>
      val schema: CoreSchema = new CoreSchema(dbContext)
      schema.create(dbContext)
      f(schema)
    }
  }
}
