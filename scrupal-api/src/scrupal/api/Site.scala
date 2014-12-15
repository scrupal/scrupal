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
package scrupal.api

import org.joda.time.DateTime

import reactivemongo.api.DefaultDB
import reactivemongo.api.indexes.{IndexType, Index}
import reactivemongo.bson._

import scrupal.db._
import scrupal.utils._
import spray.http.Uri

import scala.util.matching.Regex

/** Site Top Level Object
  * Scrupal manages sites.
 * Created by reidspencer on 11/3/14.
 */
abstract class Site(sym: Identifier) extends { val id: Symbol = sym ; val _id : Identifier = sym }
  with EnablementPathMatcherToActionProvider[Site] with VariantStorable[Identifier] with Registrable[Site]
          with Nameable with Describable with Modifiable {

  val kind = 'Site

  def requireHttps: Boolean = false

  def host: String

  def themeProvider : String = "bootswatch"

  def themeName : String = "default"

  def registry = Site

  def applications = forEach[Application] { e ⇒
    e.isInstanceOf[Application] && isEnabled(e, this)
  } { e ⇒
    e.asInstanceOf[Application]
  }

  def isChildScope(e: Enablement[_]) : Boolean = applications.contains(e)
}

case class NodeSite (
  override val id : Identifier,
  name: String,
  description: String,
  host: String,
  siteRoot: Node = Node.Empty,
  override val requireHttps: Boolean = false,
  modified: Option[DateTime] = None,
  created: Option[DateTime] = None
) extends Site(id) {
  final override val kind = 'NodeSite
  final val key : String = makeKey(id.name)

  override def actionFor(key: String, path: Uri.Path, context: Context) : Option[Action] = {
    Some( NodeAction(context, siteRoot) )
  }
}

object NodeSite {
  import BSONHandlers._

  implicit val nodeReader = Node.NodeReader
  implicit val nodeWriter = Node.NodeWriter
  object NodeSiteBRW extends VariantReaderWriter[Site,NodeSite] {
    implicit val NodeSiteHandler = Macros.handler[NodeSite]
    override def fromDoc(doc: BSONDocument): NodeSite = NodeSiteHandler.read(doc)
    override def toDoc(obj: Site): BSONDocument = NodeSiteHandler.write(obj.asInstanceOf[NodeSite])
  }
  Site.variants.register('NodeSite, NodeSiteBRW)
}

object Site extends Registry[Site] {

  val registrantsName: String = "site"
  val registryName: String = "Sites"

  object variants extends VariantRegistry[Site]("Site")


//  implicit val dtHandler = DateTimeBSONHandler
  private[this] val _byhost = new AbstractRegistry[String, Site] {
    def reg(site:Site) = _register(site.host,site)
    def unreg(site:Site) = _unregister(site.host)
    def registry = _registry
  }

  override def register(site: Site) : Unit = {
    _byhost.reg(site)
    super.register(site)
  }

  override def unregister(site: Site) : Unit = {
    _byhost.unreg(site)
    super.unregister(site)
  }

  def forHost(hostName: String) : Iterable[Site] = {
    for (
      (host, site) <- _byhost.registry ;
      regex = new Regex(host) if regex.pattern.matcher(hostName).matches()
    ) yield {
      site
    }
  }

  import BSONHandlers._

  implicit lazy val SiteReader : VariantBSONDocumentReader[Site] = new VariantBSONDocumentReader[Site] {
    def read(doc: BSONDocument) : Site = variants.read(doc)
  }

  implicit val SiteWriter : VariantBSONDocumentWriter[Site] = new VariantBSONDocumentWriter[Site] {
    def write(site: Site) : BSONDocument = variants.write(site)
  }

  /** Data Access Object For Sites
    * This DataAccessObject sublcass represents the "sites" collection in the database and permits management of
    * that collection as well as conversion to and from BSON format.
    * @param db A [[reactivemongo.api.DefaultDB]] instance in which to find the collection
    */
  case class SiteDAO(db: DefaultDB) extends VariantIdentifierDAO[Site] {
    final def collectionName: String = "sites"
    implicit val writer = new Writer(variants)
    implicit val reader = new Reader(variants)

    override def indices : Traversable[Index] = super.indices ++ Seq(
      Index(key = Seq("path" -> IndexType.Ascending), name = Some("path")),
      Index(key = Seq("kind" -> IndexType.Ascending), name = Some("kind"))
    )
  }
}
