/**********************************************************************************************************************
 * This file is part of Scrupal a Web Application Framework.                                                          *
 *                                                                                                                    *
 * Copyright (c) 2013, Reid Spencer and viritude llc. All Rights Reserved.                                            *
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

import scala.collection.mutable
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success, Try}

import play.api.{Logger, Configuration}
import play.api.libs.json.{Json, JsObject}

import org.joda.time.DateTime

import reactivemongo.api.DefaultDB

import scrupal.utils.{Jsonic, Registry, Registrable}
import scrupal.models.CoreModule
import scrupal.db.{CoreSchema,DBContext}


/** Information about one site that Scrupal is serving.
  * Sites are associated with a port number that Play! is configured to listen on. We configure play's ports by
  * scanning this table and collecting all the port numbers that are configured for active sites.
  * @param _id The name of the `Thing`
  * @param description A brief description of the `Thing`
  * @param host The host (domain) name to match in the HTTP request
  * @param siteIndex The identifier of the index (main page) for this site.
  * @param requireHttps The HTTP method required for this site. When true, HTTP requests will be rejected
  * @param enabled Whether the site is enabled for serving or not
  * @param modified Modification time, optional
  * @param created Creation time, optional
  */

case class SiteData (
  _id: Identifier,
  override val name: Identifier,
  description: String,
  host: String,
  siteIndex: Option[Identifier] = None,
  requireHttps: Boolean = false,
  enabled: Boolean = false,
  override val modified: Option[DateTime] = None,
  override val created: Option[DateTime] = None
) extends EnablableThing with Jsonic {

  def toJson : JsObject = {
    SiteData.Format.writes(this).asInstanceOf[JsObject]
  }

  def fromJson(js: JsObject) = {
    SiteData.Format.reads(js)
  }

}

object SiteData {
  implicit val Format = Json.format[SiteData]

}

class Site(val data: SiteData, implicit val dbContext: DBContext) extends Registrable {
  val id = data._id

  // TODO: Implement this with a DB query
  lazy val modules : Seq[Module] = Seq(CoreModule)

  def withCoreSchema[TBD]( f: (CoreSchema) => TBD) : TBD = {
    val schema = new CoreSchema(dbContext)
    f(schema)
  }

}

object Site extends Registry[Site] {

  def apply(data: SiteData, dbc: DBContext) = new Site(data, dbc)

  val registrantsName: String = "site"
  val registryName: String = "Sites"

  /** Load the Sites from configuration
    * Site loading is based on the Play Database configuration. There should be a one-to-one correspondence between a
    * site name and its db url/driver pair per usual Play configuration. Note that multiple sites may utilize the
    * same database information. We utilize this to open the database and load the site objects they contain
    * @param config The Scrupal Configuration to use to determine the initial loading
    */
  def load(config: Configuration) : Map[String, Site] = {
    Try {
      val result: mutable.Map[String, Site] = mutable.Map()
      val context = DBContext.fromConfiguration()
      val schema = new CoreSchema(context)
      context.withDatabase { db =>
        schema.validate match {
          case Success(true) =>
            val sites = Await.result(schema.sites.fetchAll, 5.seconds)
            for (s <- sites) {
              Logger.debug("Loading site '" + s._id.name + "' for host " + s.host + ", " +"enabled: " + s.enabled)
              result.put(s.host, Site(s, context))
            }
          case Success(false) =>
            Logger.warn("Attempt to validate schema for '" + db.name + "' failed.")
          case Failure(x) =>
            Logger.warn("Attempt to validate schema for '" + db.name + "' failed.", x)

        }
      }
      Map(result.toSeq:_*)
    } match {
      case Success(x) => x
      case Failure(e) => Logger.warn("Error while loading sites: ", e); Map[String,Site]()
    }
  }

  def unload(config: Configuration) : Map[String,Site] = {
    all foreach { s: Site => super.unRegister(s) }
    Map[String,Site]()
  }
}
