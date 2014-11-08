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

import com.typesafe.config.{ConfigParseOptions, ConfigFactory, ConfigRenderOptions, Config}
import scala.util.matching.Regex

import org.joda.time.DateTime
import reactivemongo.bson._
import scrupal.utils.{Configuration, Icons, AlertKind}

/** Scrupal Core API Library.
  * This package provides all the abstract type definitions needed to write a module for Scrupal. Since Scrupal itself
  * is simply the "Core" module, this API provides essentially everything needed to write Scrupal itself and any
  * extension to Scrupal through the introduction of a new module.
  *
  * ITEMS = Instance, Type, Entity, Module, Site
  * ITEMS are the main object types in Scrupal.
  */
package object api {

  /** The typical type of identifer.
    * We use Symbol because they are memoized by the compiler which means we only pay for the memory of a given
    * identifier once. They aren't as easily mistaken for a string either.
    */
  type Identifier = Symbol

  type ValidationResult = Option[Seq[String]]

  implicit val IdentifierBSONHandler = new BSONHandler[BSONString,Identifier] {
    override def write(t: Identifier): BSONString = BSONString(t.name)
    override def read(bson: BSONString): Identifier = Symbol(bson.value)
  }

  implicit val ShortBSONHandler = new BSONHandler[BSONInteger,Short] {
    override def write(t: Short): BSONInteger = BSONInteger(t.toInt)
    override def read(bson: BSONInteger): Short = bson.value.toShort
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

  implicit val TypeHandler : BSONHandler[BSONString,Type] = new TypeHandlerForBSON[Type]
  implicit val BundleTypeHandler: BSONHandler[BSONString,BundleType] = new TypeHandlerForBSON[BundleType]

  implicit val AppSeqHandler : BSONHandler[BSONArray,Seq[Application]] = new BSONHandler[BSONArray, Seq[Application]] {
    override def write(sa: Seq[Application]): BSONArray = BSONArray(sa map { app ⇒ app._id.name })
    override def read(array: BSONArray): Seq[Application] =
      Application.find(array.values.toSeq.map{ v ⇒ Symbol(v.asInstanceOf[BSONString].value) })
  }
}
