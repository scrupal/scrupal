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

import org.joda.time.DateTime

/** A correlaton between a site name and the database where it can be found.
  * While this information may be in a table in the same database as the site's database,
  * it doesn't have to be. This allows us to have a small bootstrap database that perhaps is really just a
  * configuration file that's written in its entirety on each update :)
  * @param id
  * @param jdbcUrl
  */
case class SiteBootstrapInfo(
  val id: Symbol,
  val jdbcUrl: String
) extends SymbolicIdentifiable

/** Information about one site that Scrupal is serving.
  * Sites are associated with a port number that Play! is configured to listen on. We configure play's ports by
  * scanning this table and collecting all the port numbers that are configured for active sites.
  * @param name The name of the `Thing`
  * @param description A brief description of the `Thing`
  * @param listenPort The port number that Play! should listen on for this site
  * @param urlDomain The domain name to use in generated urls
  * @param urlPort The port number to use in generated urls
  * @param urlHttps The HTTP method to use in generated urls (e.g. https or http)
  * @param enabled Whether the site is enabled for serving or not
  * @param modified Modification time, optional
  * @param created Creation time, optional
  * @param id Identifier, optional
  */
case class Site (
  override val name: Symbol,
  override val description: String,
  listenPort: Short,
  urlDomain: String,
  urlPort: Short,
  urlHttps: Boolean = false,
  override val enabled: Boolean = false,
  override val modified: Option[DateTime] = None,
  override val created: Option[DateTime] = None,
  override val id: Option[Identifier] = None
) extends EnablableThing(name, description, enabled, modified, created, id) {
}
