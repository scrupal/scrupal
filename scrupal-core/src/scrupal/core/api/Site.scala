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
import reactivemongo.bson.{BSONDocument, BSONHandler, BSONDocumentWriter, BSONDocumentReader}
import reactivemongo.bson.Macros._

import scrupal.db.{BSONDocumentHandler, DataAccessObject}
import scrupal.utils.Registry

/** Site Top Level Object
  * Scrupal manages sites.
 * Created by reidspencer on 11/3/14.
 */
case class Site (
  id: Identifier,
  name: String,
  description: String,
  host: String,
  var enabled: Boolean = false,
  siteIndex: Option[Identifier] = None,
  requireHttps: Boolean = false,
  modified: Option[DateTime] = None,
  created: Option[DateTime] = None
) extends StorableRegistrable[Site] with Nameable with Describable with Enablable with Modifiable {
  def registry = Site
  def asT = this
}

object Site extends Registry[Site] {
  val registrantsName: String = "site"
  val registryName: String = "Sites"

//  implicit val dtHandler = DateTimeBSONHandler

  case class SiteDao(db: DefaultDB) extends DataAccessObject[Site,Symbol](db, "sites") {
    implicit val modelHandler : BSONDocumentReader[Site] with BSONDocumentWriter[Site] with BSONHandler[BSONDocument,Site] = handler[Site]
    implicit val idHandler = (id: Symbol) ⇒ reactivemongo.bson.BSONString(id.name)
    override def indices : Traversable[Index] = super.indices ++ Seq(
      Index(key = Seq("host" -> IndexType.Ascending), name = Some("Host"))
    )
  }
}
