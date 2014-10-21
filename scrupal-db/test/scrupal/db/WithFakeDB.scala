package scrupal.db

import org.specs2.execute.{AsResult, Result}
import reactivemongo.api.DefaultDB

import scala.concurrent.Await
import scala.concurrent.duration.{Duration, FiniteDuration}
import scala.concurrent.ExecutionContext.Implicits.global


/**
 * Created by reidspencer on 10/21/14.
 */
abstract class WithFakeDB(val dbName: String, val timeout: FiniteDuration = Duration(5,"seconds")) extends scala.AnyRef
  with org.specs2.mutable.Around with org.specs2.specification.Scope
{
  var dbContext : Option[DBContext] = None

  override def around[T: AsResult](t: => T): Result = {

    DBContext.startup()
    val uri = "mongodb://localhost:27017/" + dbName
    val dbc = DBContext.fromURI(Symbol(dbName), uri)
    try {
      val future = dbc.emptyDatabase().map { emptyResults ⇒
        dbContext = Some(dbc)
        AsResult.effectively(t)
      }
      Await.result(future, timeout)
    }
    finally {
      dbc.close()
      DBContext.shutdown()
      dbContext = None
    }
  }

  def withDBContext[T]( f: (DBContext) => T ) : T = {
    assert(dbContext.isDefined)
    val dbc = dbContext.get
    f(dbc)
  }

  def withDB[T]( f : (DefaultDB) ⇒ T) : T = {
    assert(dbContext.isDefined)
    val dbc = dbContext.get
    f(dbc.database)
  }

}
