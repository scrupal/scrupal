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

package scrupal.core

import scrupal.api.APISchemaDesign
import scrupal.storage.api.Index


/** The basic schema for Scrupal. This is composed by merging together the various Components.
  */
case class CoreSchemaDesign() extends APISchemaDesign {

  /*
  // case class AliasDao(db: DB) extends JsonDao[String,BSONObjectID](db,"aliases") with DataAccessObject[String]
  // case class TokenDao(db: DB) extends JsonDao[String,BSONObjectID](db,"tokens") with DataAccessObject[String]
  // val aliases = dbc.withDatabase { db ⇒ new AliasDao(db) }
  // val tokens = dbc.withDatabase { db ⇒ new TokenDao(db) }

  override def daos : Seq[DataAccessInterface[_, _]] = super.daos

  override def validateDao(dao : DataAccessInterface[_, _]) : Boolean = { super.validateDao(dao) }

  override def create(implicit context : DBContext) : Future[Seq[(String, Boolean)]] = {
    // FIXME: This needs to be written again.

    // First, call our super class to install our schema
    val futures = super.create

    // First, install the CoreModule itself
    // val f1 = modules.insert(CoreModule)

    // Now, install all the CoreModule's types and entities
    // val f2 = for (ty <- CoreModule.types) yield { types.insert( ty ) }
    /*val f3 = for (en <- CoreModule.entities) yield { entities.insert( en ).map { wr ⇒ en.label → !wr.hasErrors } }

    val combined = Future sequence (/*Seq(f1)  ++*/ f3)

    for (f <- futures; c <- combined) yield { f ++ c }
    */
    Future[Seq[(String, Boolean)]](Seq())

  }
*/
  override def name: String = "Core"

  override def requiredNames: Seq[String] = super.requiredNames ++ Seq("alias", "token")

  override def indicesFor(name: String): Seq[Index] = super.indicesFor(name) ++ Seq.empty[Index]
}
