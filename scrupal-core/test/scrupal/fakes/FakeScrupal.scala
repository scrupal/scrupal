package scrupal.fakes

import scrupal.core.CoreSchema
import scrupal.db.{DBContext, DBContextSpecification}

import scala.concurrent.duration.{Duration, FiniteDuration}

/**
 * One line sentence description here.
 * Further description here.
 */
abstract class ScrupalSpecification(specName: String, timeout: FiniteDuration = Duration(5,"seconds"))
  extends DBContextSpecification(specName, timeout) {

  // WARNING: Do NOT put anything but def and lazy val because of DelayedInit or app startup will get invoked twice
  // and you'll have a real MESS on your hands!!!! (i.e. no db interaction will work!)


  def withCoreSchema[T]( f: CoreSchema => T ) : T = {
    withDBContext { dbContext: DBContext =>
      val schema: CoreSchema = new CoreSchema(dbContext)
      schema.create(dbContext)
      f(schema)
    }
  }

}
