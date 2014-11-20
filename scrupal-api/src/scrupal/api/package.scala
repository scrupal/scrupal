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
package scrupal


import java.nio.charset.Charset
import scrupal.utils._

/** Scrupal API Library.
  * This package provides all the abstract type definitions that Scrupal provides. These are the main abstractions
  * needed to write an application with Scrupal. We use the Acronym *MANIFEST*(O) to remember the key types of
  * objects Scrupal defines:
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
  * - O - ???
  *
  * If you can grok these few concepts then you have understood the core concepts of Scrupal.
  *
  */
package object api extends ScrupalComponent {

  lazy val utf8 = Charset.forName("UTF-8")

  /** The typical type of identifer.
    * We use Symbol because they are memoized by the compiler which means we only pay for the memory of a given
    * identifier once. They aren't as easily mistaken for a string either.
    */
  type Identifier = Symbol

  type ValidationResult = Option[Seq[String]]

 }
