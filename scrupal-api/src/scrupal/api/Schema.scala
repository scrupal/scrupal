/**********************************************************************************************************************
 * Copyright © 2014 Reactific Software LLC                                                                            *
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

package scrupal.api

import scrupal.db.{ScrupalDB, DataAccessInterface, DBContext}

/** Database Schema For API
  *
  * This is the Database Schema for the scrupal.api module. It defines the kinds of collections that are found in
  * the schema and provides the means for validating them.
  */
class Schema(dbc: DBContext, dbName: String) extends scrupal.db.Schema(dbc, dbName) {

  val (sites,nodes,instances,alerts, principals) = withDB { db : ScrupalDB ⇒
    (
      Site.SiteDAO(db),
      Node.NodeDAO(db),
      Instance.InstanceDAO(db),
      Alert.AlertDAO(db),
      Principal.PrincipalDAO(db)
    )
  }

  // case class TokenDao(db: DB) extends JsonDao[String,BSONObjectID](db,"tokens") with DataAccessObject[String]
  // val aliases = dbc.withDatabase { db => new AliasDao(db) }
  // val tokens = dbc.withDatabase { db => new TokenDao(db) }


  def daos : Seq[DataAccessInterface[_,_]] = {
    Seq( sites, nodes, instances, alerts, principals )
  }

  def validateDao(dao: DataAccessInterface[_,_]) : Boolean = {
    // FIXME: this needs to be written properly
    dao.collection.name match {
      case "sites" ⇒ true
      case "nodes" ⇒ true
      case "instances" ⇒ true
      case "alerts" ⇒ true
      case "principals" ⇒ true
      case _ ⇒ false
    }
  }
}
