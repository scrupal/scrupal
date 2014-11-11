package scrupal.core.api

import org.joda.time.DateTime
import reactivemongo.api.DefaultDB
import reactivemongo.api.indexes.{IndexType, Index}
import reactivemongo.bson._
import scrupal.db.{VariantIdentifierDAO, VariantStorableRegistrable}
import scrupal.utils.Registry

/** A Scrupal Application
  * Applications are a fundamental unit of organization in Scrupal. A Site defines the set of applications that are to
  * run on that site. Each application gets a top level context and configures which modules are relevant for it
 * Created by reid on 11/6/14.
 */
trait Application
  extends VariantStorableRegistrable[Application]
          with Nameable with Describable with Modifiable with Enablable {
  def registry: Registry[Application] = Application
  def asT : Application = this

  /** Application Context Path
    * This is the path at the start of the Site's URL that this application uses.
    * @return
    */
  def path: String

  /** Applicable Modules
    * This modules are assigned (enabled) within this application
    */
  def modules: Seq[Module]

}

case class BasicApplication(
  id : Identifier,
  name: String,
  description: String,
  path : String,
  modules: Seq[Module] = Seq.empty[Module],
  modified : Option[DateTime] = None,
  created : Option[DateTime] = None
) extends Application {
  final val kind = 'Basic
}

object BasicApplication {
  implicit val BasicApplicationHandler = Macros.handler[BasicApplication]
}

object Application extends Registry[Application] {
  def registryName = "Applications"
  def registrantsName = "application"

  implicit lazy val ApplicationReader = new VariantBSONDocumentReader[Application] {
    def read(doc: BSONDocument) : Application = {
      doc.getAs[BSONString]("kind") match {
        case Some(str) =>
          str.value match {
            case "Basic"  => BasicApplication.BasicApplicationHandler.read(doc)
            case _ ⇒ toss(s"Unknown kind of Application: '${str.value}")
          }
        case None => toss(s"Field 'kind' is missing from Node: ${doc.toString()}")
      }
    }
  }

  implicit val ApplicationWriter = new VariantBSONDocumentWriter[Application] {
    def write(app: Application) : BSONDocument = {
      app.kind match {
        case 'Basic  => BasicApplication.BasicApplicationHandler.write(app.asInstanceOf[BasicApplication])
        case _ ⇒ toss(s"Unknown kind of Application: ${app.kind}")
      }
    }
  }

  /** Data Access Object For Applications
    * This DataAccessObject sublcass represents the "applications" collection in the database and permits management of
    * that collection as well as conversion to and from BSON format.
    * @param db A [[reactivemongo.api.DefaultDB]] instance in which to find the collection
    */

  case class ApplicationDAO(db: DefaultDB) extends VariantIdentifierDAO[Application] {
    final def collectionName: String = "applications"
    implicit val writer = new Writer(ApplicationWriter)
    implicit val reader = new Reader(ApplicationReader)

    override def indices : Traversable[Index] = super.indices ++ Seq(
      Index(key = Seq("path" -> IndexType.Ascending), name = Some("path")),
      Index(key = Seq("kind" -> IndexType.Ascending), name = Some("kind"))
    )
  }
}
