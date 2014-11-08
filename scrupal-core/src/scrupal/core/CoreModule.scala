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

import java.net.URL

import reactivemongo.bson.{BSONString, BSONValue}
import scrupal.db.{Schema,DBContext}
import scrupal.core.api._
import scrupal.utils.Version

import CoreFeatures._

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
  enabled = true
) {

  def moreDetailsURL = new URL("http://modules.scrupal.org/doc/" + label)

  def author : String = "Reid Spencer"

  def copyright : String = "(C) 2014 Reactific Systems, Inc. All Rights Reserved"

  def license : String = "GPLv3"

  override def config =   MutableConfiguration(
    Map(
      "Notes" → Markdown_t
    ),
    Map[String,BSONValue](
      "Notes" → BSONString("No notes.")
    )
  )

  override def features = Seq(
    DebugFooter, DevMode, ConfigWizard, RESTAPIAccess, RESTAPIDocumentation, OnePageApplications
  )


  /** The core types that Scrupal provides to all modules */
  override def types = Seq[Type](
    AnyType_t, AnyString_t, NonEmptyString_t, Password_t, AnyInteger_t, AnyReal_t, AnyTimestamp_t, Boolean_t,
    Identifier_t, Description_t, Markdown_t, DomainName_t, TcpPort_t, URL_t, IPv4Address_t, EmailAddress_t,
    LegalName_t, SiteInfo_t, PageBundle_t
  )

  val PageEntity = Entity(
    'Page,
    "An entity for simple HTML5 pages.",
    PageBundle_t,
    this
  )

  override def entities = Seq[Entity](
    PageEntity
  )

  override def nodes = Seq[Node]()

  override def handlers = Seq()

  override def schemas(implicit dbc: DBContext) : Seq[Schema] = Seq( new CoreSchema(dbc) )
}
