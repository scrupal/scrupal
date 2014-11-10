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
package scrupal.core

import java.io.File
import java.net.URL
import java.nio.charset.Charset

import com.typesafe.config.{ConfigParseOptions, ConfigFactory, ConfigRenderOptions, Config}
import play.twirl.api.Html
import spray.http.{MediaTypes, MediaType}
import scala.util.matching.Regex

import org.joda.time.DateTime
import reactivemongo.bson._
import scrupal.utils.{ScrupalComponent, Configuration, Icons, AlertKind}

/** Scrupal Core API Library.
  * This package provides all the abstract type definitions that Scrupal provides. These are the core abstractions
  * needed to write an application with Scrupal. We use the Acronym *MANIFEST* to remember what the key types of objects
  * Scrupal defines:
  *
  * - M - Module: A container of functionality that defines Applications, Nodes, Entities, and Types
  *
  * - A - Application: A URL context and a set of enabled modules, entities and nodes
  *
  * - N - Node: A content generation function
  *
  * - I - Instance: An instance of an entity (essentially a document)
  *
  * - F - Facet: Something to add on to an instance's main payload
  *
  * - E - Entity: A type of instance with definitions for the actions that can be performed on it
  *
  * - S - Site: Site management data and a set of applications enabled for it.
  *
  * - T - Type: A fundamental data type used for validating BSONValue structured information (Instances and Node results)
  *
  * If you can grok these few concepts then you have understood the core concepts of Scrupal.
  *
  * At the package level we define mostly implicit BSON translaters needed throughout the core, for convenience.
  */
package object api extends ScrupalComponent {

  lazy val system = scrupal.core.system

  lazy val utf8 = Charset.forName("UTF-8")

  /** The typical type of identifer.
    * We use Symbol because they are memoized by the compiler which means we only pay for the memory of a given
    * identifier once. They aren't as easily mistaken for a string either.
    */
  type Identifier = Symbol

  type ValidationResult = Option[Seq[String]]

  implicit val IdentifierConverter = (id: Identifier) ⇒ reactivemongo.bson.BSONString(id.name)

  implicit val IdentifierBSONHandler = new BSONHandler[BSONString,Identifier] {
    override def write(t: Identifier): BSONString = BSONString(t.name)
    override def read(bson: BSONString): Identifier = Symbol(bson.value)
  }

  implicit val ShortBSONHandler = new BSONHandler[BSONInteger,Short] {
    override def write(t: Short): BSONInteger = BSONInteger(t.toInt)
    override def read(bson: BSONInteger): Short = bson.value.toShort
  }

  implicit val URLHandler = new BSONHandler[BSONString,URL] {
    override def write(t: URL): BSONString = BSONString(t.toString)
    override def read(bson: BSONString): URL = new URL(bson.value)
  }

  implicit val DateTimeBSONHandler = new BSONHandler[BSONDateTime,DateTime] {
    override def write(t: DateTime): BSONDateTime = BSONDateTime(t.getMillis)
    override def read(bson: BSONDateTime): DateTime = new DateTime(bson.value)
  }

  implicit val AlertKindHandler = new BSONHandler[BSONString,AlertKind.Kind] {
    override def write(t: AlertKind.Kind): BSONString = BSONString(t.toString)
    override def read(bson: BSONString): AlertKind.Kind = AlertKind.withName(bson.value)
  }

  implicit val IconsHandler = new BSONHandler[BSONString,Icons.Kind] {
    override def write(t: Icons.Kind): BSONString = BSONString(t.toString)
    override def read(bson: BSONString): Icons.Kind = Icons.withName(bson.value)
  }

  implicit val ConfigHandler: BSONHandler[BSONString,Config] = new BSONHandler[BSONString,Config] {
    override def write(t: Config): BSONString = BSONString(t.root.render (ConfigRenderOptions.concise()))
    override def read(bson: BSONString): Config = ConfigFactory.parseString(bson.value, ConfigParseOptions.defaults())
  }

  implicit val ConfigurationHandler: BSONHandler[BSONString,Configuration] = new BSONHandler[BSONString,Configuration] {
    override def write(c: Configuration): BSONString =
      BSONString(c.underlying.root.render (ConfigRenderOptions.concise()))
    override def read(bson: BSONString): Configuration =
      Configuration(ConfigFactory.parseString(bson.value, ConfigParseOptions.defaults()))
  }

  implicit val RegexHandler: BSONHandler[BSONString,Regex] = new BSONHandler[BSONString,Regex] {
    override def write(t: Regex): BSONString = BSONString(t.pattern.pattern())
    override def read(bson: BSONString): Regex = new Regex(bson.value)
  }

  implicit val TypeHandler : BSONHandler[BSONString,Type] = new BSONHandlerForType[Type]
  implicit val BundleTypeHandler: BSONHandler[BSONString,BundleType] = new BSONHandlerForType[BundleType]

  implicit val ModuleHandler : BSONHandler[BSONString,Module] = new BSONHandler[BSONString,Module] {
    override def write(m: Module): BSONString = BSONString(m.id.name)
    override def read(bson: BSONString): Module = Module(Symbol(bson.value)).get
  }

  implicit val AppSeqHandler : BSONHandler[BSONArray,Seq[Application]] = new BSONHandler[BSONArray, Seq[Application]] {
    override def write(sa: Seq[Application]): BSONArray = BSONArray(sa map { app ⇒ app._id.name })
    override def read(array: BSONArray): Seq[Application] =
      Application.find(array.values.toSeq.map{ v ⇒ Symbol(v.asInstanceOf[BSONString].value) })
  }

  implicit val MediaTypeHandler : BSONHandler[BSONArray,MediaType] = new BSONHandler[BSONArray,MediaType] {
    override def write(mt: MediaType): BSONArray = BSONArray(mt.mainType, mt.subType)
    override def read(bson: BSONArray): MediaType = {
      val key = bson.values(0).asInstanceOf[BSONString].value → bson.values(1).asInstanceOf[BSONString].value
      MediaTypes.getForKey(key) match {
        case Some(mt) ⇒ mt
        case None ⇒ toss(s"MediaType `$key` is unknown.")
      }
    }
  }

  implicit val ArrayByteHandler : BSONHandler[BSONBinary,Array[Byte]] = new BSONHandler[BSONBinary,Array[Byte]] {
    override def write(bytes: Array[Byte]) : BSONBinary = BSONBinary(bytes, Subtype.GenericBinarySubtype)
    override def read(bson: BSONBinary) : Array[Byte] = bson.value.readArray(bson.value.size)
  }

  implicit val HtmlHandler : BSONHandler[BSONBinary,Html] = new BSONHandler[BSONBinary,Html] {
    override def write(html: Html) : BSONBinary = BSONBinary(html.body.getBytes("UTF-8"), Subtype.GenericBinarySubtype)
    override def read(bson: BSONBinary) : Html = Html(new String(bson.value.readArray(bson.value.size)))
  }

  implicit val FileHandler : BSONHandler[BSONString,File] = new BSONHandler[BSONString,File] {
    override def write(f: File): BSONString = BSONString(f.getPath)
    override def read(bson: BSONString): File = new File(bson.value)
  }
}
