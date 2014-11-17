/**********************************************************************************************************************
 * Copyright © 2014 Reactific Software, Inc.                                                                          *
 *                                                                                                                    *
 * This file is part of Scrupal, an Opinionated Web Application Framework.                                            *
 *                                                                                                                    *
 * Scrupal is free software: you can redistribute it and/or modify it under the terms                                 *
 * of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License,   *
 * or (at your option) any later version.                                                                             *
 *                                                                                                                    *
 * Scrupal is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied      *
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more      *
 * details.                                                                                                           *
 *                                                                                                                    *
 * You should have received a copy of the GNU General Public License along with Scrupal. If not, see either:          *
 * http://www.gnu.org/licenses or http://opensource.org/licenses/GPL-3.0.                                             *
 **********************************************************************************************************************/

package scrupal.core.api

import org.joda.time.DateTime
import reactivemongo.api.DefaultDB
import reactivemongo.api.indexes.{IndexType, Index}
import reactivemongo.bson._
import scrupal.db.{VariantIdentifierDAO, VariantStorableRegistrable}
import scrupal.utils._

/** A Scrupal Application
  * Applications are a fundamental unit of organization in Scrupal. A Site defines the set of applications that are to
  * run on that site. Each application gets a top level context and configures which modules are relevant for it
 * Created by reid on 11/6/14.
 */
trait Application
  extends VariantStorableRegistrable[Application]
          with Nameable with Describable with Modifiable with Enablement[Application] with Enablee {
  def registry: Registry[Application] = Application
  def asT : Application = this

  /** Application Context Path
    * This is the path at the start of the Site's URL that this application uses.
    * @return
    */
  def path: String = id.name.toLowerCase.replaceAll(Patterns.NotAllowedInUrl.pattern.pattern,"-")

  /** Applicable Modules
    * This modules are assigned (enabled) within this application
    */
  def modules = forEachEnabled[Module,Module] { e ⇒ e }

  def entities = forEachEnabled[Entity,Entity] { e ⇒ e }

  def isChildScope(e: Enablement[_]) : Boolean = entities.contains(e)

  type EntityMap = Map[String,(String,Entity)]

  def getEntityMap() : EntityMap = {
    for (
      entity ← entities if entity.isEnabled(this) ;
      name ← Seq(entity.path, entity.plural_path)
    ) yield {
      name → (name → entity)
    }
  }.toMap


}

case class BasicApplication(
  id : Identifier,
  name: String,
  description: String,
  modified : Option[DateTime] = None,
  created : Option[DateTime] = None
) extends Application {
  final val kind = 'BasicApplication
}

object BasicApplication {
  import BSONHandlers._

  implicit val BasicApplicationHandler = Macros.handler[BasicApplication]
}

object Application extends Registry[Application] {
  def registryName = "Applications"
  def registrantsName = "application"

  private[this] val _bypath = new AbstractRegistry[String, Application] {
    def reg(app:Application) = _register(app.path,app)
    def unreg(app:Application) = _unregister(app.path)
  }

  override def register(app: Application) : Unit = {
    _bypath.reg(app)
    super.register(app)
  }

  override def unregister(app: Application) : Unit = {
    _bypath.unreg(app)
    super.unregister(app)
  }

  def forPath(path: String) = _bypath.lookup(path)

  import BSONHandlers._


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
