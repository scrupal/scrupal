/**********************************************************************************************************************
 * This file is part of Scrupal, a Scalable Reactive Web Application Framework for Content Management                 *
 *                                                                                                                    *
 * Copyright (c) 2015, Reactific Software LLC. All Rights Reserved.                                                   *
 *                                                                                                                    *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance     *
 * with the License. You may obtain a copy of the License at                                                          *
 *                                                                                                                    *
 *     http://www.apache.org/licenses/LICENSE-2.0                                                                     *
 *                                                                                                                    *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed   *
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for  *
 * the specific language governing permissions and limitations under the License.                                     *
 **********************************************************************************************************************/

package scrupal.api.types

import akka.http.scaladsl.model.{MediaTypes, MediaType}
import scrupal.api._

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
case class NodeType[R](
  id : Identifier,
  description : String,
  fields : Map[String, Type[R]],
  mediaType : MediaType = MediaTypes.`text/html`) extends StructuredType[R] {
  override def kind = 'Node
}
