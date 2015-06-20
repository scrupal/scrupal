/** ********************************************************************************************************************
  * This file is part of Scrupal, a Scalable Reactive Web Application Framework for Content Management                 *
  *                                                                                                                    *
  * Copyright (c) 2015, Reactific Software LLC. All Rights Reserved.                                                   *
  *                                                                                                                    *
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance     *
  * with the License. You may obtain a copy of the License at                                                          *
  *                                                                                                                    *
  * http://www.apache.org/licenses/LICENSE-2.0                                                                         *
  *                                                                                                                    *
  * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed   *
  * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for  *
  * the specific language governing permissions and limitations under the License.                                     *
  * ********************************************************************************************************************
  */

package scrupal.storage.api

import scrupal.utils.Validation._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/** A Schema Design
  *
  * This provides a pattern for a schema which is simply a group of related Collections, their indices,
  * and the necessary validation of the schema. A SchemaDesign is agnostic to the storage system utilized.
  */
abstract class SchemaDesign extends Validator[Schema] {

  def name : String

  def requiredNames : Seq[String]

  def indicesFor(name : String) : Seq[Index]

  def validateCollection(schema : Schema, name : String, coll : Collection[_]) : Option[String] = {
    if (coll.name.isEmpty)
      Some(s"Collection, $name in $schema, may not have an empty name.")
    else if (coll.name != name)
      Some(s"The schema name ($name), must match the collection name (${coll.name})")
    else if (coll.schema != null)
      Some(s"Collection $coll,  may not have a null schema. It should be $schema")
    else if (coll.schema != schema)
      Some(s"Collection $name belongs to wrong schema (${coll.schema}), should be $schema")
    else {

    }
    None
  }

  def construct(schema : Schema) : Future[Results[Schema]] = {
    val futures = for (name ← requiredNames) yield {
      schema.collectionFor(name) match {
        case Some(coll) ⇒ Future.successful(coll)
        case None ⇒ schema.addCollection(name)
      }
    } /* TODO: Support index creation from schema design
      map { collection ⇒
      for (index ← indicesFor(name)) {
        collection.indexOf(index.indexables) map {
          case Some(idx) ⇒ // nothing to do
          case None ⇒ collection.addIndex(index)
        }
      }
    }*/
    Future.sequence(futures).map {
      fs ⇒ validate(schema)
    }
  }

  final def validate(schema : Schema) : Results[Schema] = {
    validate(TypedLocation(schema), schema)
  }

  final def validate(location : Location, schema : Schema) : Results[Schema] = {
    val collections = schema.collections
    val errors = {
      for (name ← requiredNames) yield {
        collections.get(name) match {
          case None ⇒
            StringFailure(TypedLocation(schema).select(name), schema, s"Collection $name does not exist")
          case Some(c) ⇒ {
            validateCollection(schema, name, c) match {
              case None    ⇒ null
              case Some(s) ⇒ StringFailure(TypedLocation(schema).select(name), schema, s)
            }
          }
        }
      }
    }.filterNot { x ⇒ x == null }

    if (errors.nonEmpty)
      Failures(location, schema, errors : _*)
    else
      Success(location, schema)
  }
}
