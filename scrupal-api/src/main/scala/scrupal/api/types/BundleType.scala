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

import scrupal.api._

/** A group of named and typed fields that are related in some way.
  * An bundle type defines the structure of the fundamental unit of storage in Scrupal: An instance.  BundleTypes
  * are analogous to table definitions in relational databases, but with one important difference. An BundleType can
  * define itself recursively. That is, one of the fields of an Bundle can be another Bundle Type. Cycles in the
  * definitions of BundleTypes are not permitted. In this way it is possible to assemble entities from a large
  * collection of smaller bundle concepts (traits if you will). Like relational tables, bundle types define the names
  * and types of a group of fields (columns) that are in some way related. For example, a street name, city name,
  * country and postal code are fields of an address bundle because they are related by the common purpose of
  * specifying a location.
  *
  * Note that BundleTypes, along with ListType, SetType and MapType make the type system fully composable. You
  * can create an arbitrarily complex data structure (not that we recommend that you do so).
  *
  * @param id The name of the trait
  * @param description A description of the trait in terms of its purpose or utility
  * @param fields A map of the field name symbols to their Type
  */
case class BundleType(
  id : Identifier,
  description : String,
  fields : Map[String, Type[_]]
  )(implicit val   scrupal : Scrupal) extends StructuredType[Type[_]] {
  override def kind = 'Bundle
}

object BundleType {
  def Empty(implicit scrupal: Scrupal) =
    BundleType('EmptyBundleType, "A Bundle with no fields", Map.empty[String, Type[_]])
}

