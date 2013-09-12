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
import play.api.libs.json._
import play.api.libs.json.JsSuccess

import play.modules.reactivemongo.ReactiveMongoPlugin._
import play.modules.reactivemongo.json.collection.JSONCollection

import reactivemongo.bson.BSONObjectID
import scrupal.utils.Pluralizer


/**
 * A Scrupal content entity. This is the abstract superclass of all content objects in Scrupal.
 * It provides the basic methods for finding
 */
abstract class Entity(val id: BSONObjectID = BSONObjectID.generate)
{
  lazy val collectionName = Entity.computeCollectionName(this)
  lazy val collection = Entity.computeCollection(collectionName)
}

object Entity
{
  val entity2collection = mutable.HashMap[Class[_], String]()

  def computeCollectionName(e: Entity) : String = {
    val klass: Class[_] = e.getClass
    val memo = entity2collection.get(klass)
    if (memo.isDefined)
      return Pluralizer.pluralize(memo.get)
    val name = klass.getSimpleName()
    val pos$ = name.lastIndexOf('$')
    if (pos$ < 0)
      return Pluralizer.pluralize(name)
    Pluralizer.pluralize(name.substring(0, pos$).split('$').last)
  }

  def computeCollection(name: String) : JSONCollection = {
    db.collection[JSONCollection](name)
  }

}
