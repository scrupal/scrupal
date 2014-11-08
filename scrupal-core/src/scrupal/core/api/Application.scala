package scrupal.core.api

import org.joda.time.DateTime
import reactivemongo.api.DefaultDB
import reactivemongo.bson.Macros._
import reactivemongo.bson.{BSONHandler, BSONDocumentWriter, BSONDocumentReader, BSONDocument}
import scrupal.db.DataAccessObject
import scrupal.utils.Registry

/** A Scrupal Application
  * Applications are a fundamental unit of organization in Scrupal. A Site defines the set of applications that are to
  * run on that site. Each application gets a top level context and configures which modules are relevant for it
 * Created by reid on 11/6/14.
 */
case class Application(
  id : Identifier,
  name: String,
  description: String,
  modules: Seq[Symbol] = Seq.empty[Symbol],
  modified : Option[DateTime] = None,
  created : Option[DateTime] = None
) extends StorableRegistrable[Application] with Nameable with Describable with Modifiable {
  def registry: Registry[Application] = Application
  def asT : Application = this
}

object Application extends Registry[Application] {
  def registryName = "Applications"
  def registrantsName = "application"

  /** Data Access Object For Applications
    * This DataAccessObject sublcass represents the "applications" collection in the database and permits management of
    * that collection as well as conversion to and from BSON format.
    * @param db A [[reactivemongo.api.DefaultDB]] instance in which to find the collection
    */
  case class ApplicationDao(db: DefaultDB) extends DataAccessObject[Application,Symbol](db, "applications") {
    implicit val modelHandler  : BSONDocumentReader[Application]
      with BSONDocumentWriter[Application]
      with BSONHandler[BSONDocument,Application] = handler[Application]

    implicit val idHandler = (id: Symbol) ⇒ reactivemongo.bson.BSONString(id.name)
  }
}
