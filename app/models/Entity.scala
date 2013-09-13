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

package scrupal.models

import scala.collection.mutable

import play.api.Play.current

import play.modules.reactivemongo.ReactiveMongoPlugin._
import play.modules.reactivemongo.json.collection.JSONCollection

import reactivemongo.bson.BSONObjectID
import scrupal.utils.Pluralizer
import play.api.libs.json.{Json, JsString, JsObject}
import reactivemongo.core.commands.LastError
import scala.concurrent.{ExecutionContext, Future}

import ExecutionContext.Implicits.global

import MissingJSONReadsWrites._


/**
 * A Scrupal content entity. This is the abstract superclass of all content objects in Scrupal.
 * It provides the basic methods for finding
 */
abstract class Entity
{
  lazy val entityType = Entity.computeEntityTypeName(this)
  lazy val collectionName = Pluralizer.pluralize(entityType)
  lazy val collection = Entity.computeCollection(collectionName)

  lazy val id: BSONObjectID = BSONObjectID.generate

  val label : String = "Entity"
  val description : String = "Abstract Entity"
  def toJson : JsObject = {
    Json.obj(
      "_id" ->  id,
      "label" ->  label,
      "description" -> description
    )
  }

}

object Entity
{

  val entityType2collection = mutable.HashMap[Class[_], String]()

  def computeEntityTypeName(e: Entity) : String = {
    val klass: Class[_] = e.getClass
    val memo = entityType2collection.get(klass)
    if (memo.isDefined)
      return memo.get
    val name = klass.getSimpleName()
    val pos$ = name.lastIndexOf('$')
    if (pos$ < 0)
      return name
    name.substring(0, pos$).split('$').last
  }

  def computeCollection(name: String) : JSONCollection = {
    db.collection[JSONCollection](name)
  }

  /*
  def fetch[E <: Entity](id: BSONObjectID)(implicit entity: E) : Future[Option[E]] = {
    val selector = Json.obj ( "id" -> id.toString )
    implicit val formatter = Json.format[E]
    val query = entity.collection.find(selector)
    query.cursor[E].headOption()
  }
  */

  def save(entity: Entity) : Future[LastError] = {
    val jsValue = entity.toJson
    entity.collection.save(jsValue)
  }

  /*
  def upsert(entity: Entity) : Future[LastError] = {

  }
  */

  def remove(entity: Entity) : Future[LastError] = {
    assert( entity.id.toString != "", "Empty entity ID")
    val selector = Json.obj( "_id" -> entity.id )
    entity.collection.remove(selector, firstMatchOnly = true)
  }
}
