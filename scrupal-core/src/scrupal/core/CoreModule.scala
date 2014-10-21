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

package scrupal.core

import java.net.{URI, URISyntaxException}

import scrupal.db.{Schema,DBContext}
import scrupal.core.api._
import scrupal.utils.{Configuration, Version}

import scala.collection.immutable.HashMap

import CoreFeatures._
import Patterns._

/*   id : Identifier,
  description: String,
  version: Version,
  obsoletes: Version,
  config: BSONObjectID,
  configVars: Map[String,Type] = Map.empty[String,Type],
  enabled: Boolean = true
*/
/** Scrupal's Core Module.
  * This is the base module of all modules. It provides the various abstractions that permit other modules to extend
  * its functionality. The Core module defines the simple, trait, bundle and entity types
 * Further description here.
 */
object CoreModule extends Module(
  Core,
  "Scrupal's Core module for core, essential functionality.",
  Version(0,1,0),
  Version(0,0,0),
  CoreConfigObjectId,
  Map(
    "Notes" → Markdown_t
  ),
  enabled = true
) {



  /** The core types that Scrupal provides to all modules */
  override def types = Seq[Type](
    Identifier_t, Description_t, Markdown_t, DomainName_t, TcpPort_t, URL_t, IPv4Address_t, EmailAddress_t,
    LegalName_t, SiteInfo_t, PageBundle_t
  )

  val PageEntity = Entity(
    'Page,
    "An entity for simple HTML5 pages.",
    PageBundle_t.id
  )

  override def entities = Seq[Entity](
    PageEntity
  )

  /** Settings for the core
    */
  override def settings = Configuration.empty


  override def features = Seq(
    DebugFooter, DevMode, ConfigWizard, RESTAPIAccess, RESTAPIDocumentation, OnePageApplications
  )

  override def schemas(implicit dbc: DBContext) : Seq[Schema] = Seq( new CoreSchema(dbc) )

 // val schema = new CoreSchema
}
