/**********************************************************************************************************************
 * Copyright © 2015 Reactific Software LLC                                                                            *
 *                                                                                                                    *
 * This file is part of Scrupal, an Opinionated Web Application Framework.                                            *
 *                                                                                                                    *
 * Scrupal is free software: you can redistribute it and/or modify it under the terms                                 *
 * of the GNU General Public License as published by the Free Software Foundation,                                    *
 * either version 3 of the License, or (at your option) any later version.                                            *
 *                                                                                                                    *
 * Scrupal is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;                               *
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                          *
 * See the GNU General Public License for more details.                                                               *
 *                                                                                                                    *
 * You should have received a copy of the GNU General Public License along with Scrupal.                              *
 * If not, see either: http://www.gnu.org/licenses or http://opensource.org/licenses/GPL-3.0.                         *
 **********************************************************************************************************************/

package scrupal.core.sites

import org.joda.time.DateTime
import reactivemongo.bson.{BSONDocument, Macros}
import scrupal.core.actions.NodeAction
import scrupal.core.api._
import scrupal.db.VariantReaderWriter

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
  final override val kind = NodeSite.kind

  override def extractAction(context: Context) : Option[Action] = {
    super.extractAction(context) match {
      case Some(action) ⇒ Some(action)
      case None ⇒ Some( NodeAction(context, siteRoot) )
    }
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
  val kind = 'NodeSite
  Site.variants.register(kind, NodeSiteBRW)
}

