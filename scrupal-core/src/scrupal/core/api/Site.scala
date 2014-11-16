/**********************************************************************************************************************
  * This file is part of Scrupal a Web Application Framework.                                                          *
  *                                                                                                                    *
  * Copyright (c) 2014, Reid Spencer and viritude llc. All Rights Reserved.                                            *
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
import scrupal.core.Node
import scrupal.core.echo.EchoApp

import scrupal.db.{VariantIdentifierDAO, VariantStorableRegistrable}
import scrupal.utils.{Enablee, Enablement, AbstractRegistry, Registry}

/** Site Top Level Object
  * Scrupal manages sites.
 * Created by reidspencer on 11/3/14.
 */
trait Site
  extends VariantStorableRegistrable[Site]
          with Nameable with Describable with Enablement[Site] with Enablee with Modifiable {
  def requireHttps: Boolean = false

  def host: String

  def siteRoot: Node

  def registry = Site

  def asT = this

  val kind = 'Site

  def applications : Seq[Application] = Seq.empty[Application]

  def isChildScope(e: Enablement[_]) : Boolean = applications.contains(e)

  /** Mapping of application paths to Applications and their corresponding EntityMap  */
  type ApplicationMap= Map[String,(Application,Application#EntityMap)]

  def getApplicationMap : ApplicationMap = {
    forEachEnabled { app: Application ⇒
      app.path → (app → app.getEntityMap())
    }
  }.toMap

}

case class BasicSite (
  id : Identifier,
  name: String,
  description: String,
  host: String,
  siteRoot: Node = Node.Empty,
  override val applications: Seq[Application] = Seq.empty[Application],
  override val requireHttps: Boolean = false,
  modified: Option[DateTime] = None,
  created: Option[DateTime] = None
) extends Site {
  override val kind = 'BasicSite
}

object BasicSite {
  implicit val nodeReader = Node.NodeReader
  implicit val nodeWriter = Node.NodeWriter
  implicit val BasicSiteHandler = Macros.handler[BasicSite]
}

object DefaultSite
  extends BasicSite('Default, "Default", "The Default Scrupal Site", "localhost", Node.Empty, Seq(EchoApp),
                    requireHttps = false, modified=Some(DateTime.now()), created=Some(DateTime.now()))

object Site extends Registry[Site] {
  val registrantsName: String = "site"
  val registryName: String = "Sites"

//  implicit val dtHandler = DateTimeBSONHandler
  private[this] val _byhost = new AbstractRegistry[String, Site] {
    def reg(site:Site) = _register(site.host,site)
    def unreg(site:Site) = _unregister(site.host)
  }

  override def register(site: Site) : Unit = {
    _byhost.reg(site)
    super.register(site)
  }

  override def unregister(site: Site) : Unit = {
    _byhost.unreg(site)
    super.unregister(site)
  }

  def forHost(hostName: String) = _byhost.lookup(hostName)


  implicit lazy val SiteReader = new VariantBSONDocumentReader[Site] {
    def read(doc: BSONDocument) : Site = {
      doc.getAs[BSONString]("kind") match {
        case Some(str) =>
          str.value match {
            case "Basic"  => BasicSite.BasicSiteHandler.read(doc)
            case _ ⇒ toss(s"Unknown kind of Site: '${str.value}")
          }
        case None => toss(s"Field 'kind' is missing from Node: ${doc.toString()}")
      }
    }
  }

  implicit val SiteWriter = new VariantBSONDocumentWriter[Site] {
    def write(site: Site) : BSONDocument = {
      site.kind match {
        case 'Basic  => BasicSite.BasicSiteHandler.write(site.asInstanceOf[BasicSite])
        case _ ⇒ toss(s"Unknown kind of Site: ${site.kind}")
      }
    }
  }

  /** Data Access Object For Sites
    * This DataAccessObject sublcass represents the "sites" collection in the database and permits management of
    * that collection as well as conversion to and from BSON format.
    * @param db A [[reactivemongo.api.DefaultDB]] instance in which to find the collection
    */

  case class SiteDAO(db: DefaultDB) extends VariantIdentifierDAO[Site] {
    final def collectionName: String = "sites"
    implicit val writer = new Writer(SiteWriter)
    implicit val reader = new Reader(SiteReader)

    override def indices : Traversable[Index] = super.indices ++ Seq(
      Index(key = Seq("path" -> IndexType.Ascending), name = Some("path")),
      Index(key = Seq("kind" -> IndexType.Ascending), name = Some("kind"))
    )
  }
}
