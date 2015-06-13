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

package scrupal.core.types

import scrupal.core.api._
import spray.http.{MediaType, MediaTypes}

/** Abstract Node Type
  *
  * A NodeType defines a way to generate
  * NodeType inherits this trait so it defines an apply method with
  * a matching signature. The intent is that the BSONDocument supplied to the NodeType is validated against the
  * node types fields. Because a Type is also a validator
  * @param id
  * @param description
  * @param fields
  * @param mediaType
  */
case class NodeType (
  id : Identifier,
  description : String,
  fields : Map[String, Type],
  mediaType : MediaType = MediaTypes.`text/html`
) extends StructuredType
{
  override type ScalaValueType = Map[String,Any]
  override def kind = 'Node
}
