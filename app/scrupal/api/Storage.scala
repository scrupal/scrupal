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

package scrupal.api

import play.api.libs.json.{JsResult, JsObject}
import org.joda.time.DateTime

/** All storable things have, at least, a typ */
abstract class Storable(val typ : Symbol)

abstract class Reference[T <: Type]


abstract class StoreHandler[S <: Storable] {
  def create(s: S) : Storable
  def fetch(id : Long) : Option[Storable]
  def update(s: S) : Option[Storable]
  def delete(s: S) : Boolean
  def delete(id: Long) : Boolean
}

/** One line sentence description here.
  * Further description here.
  */
abstract class Storage[S <: Storable] {
  def create(obj: S ) : JsResult[Long]
  def fetch(id: Long) : JsResult[JsObject]
  def update(obj: S) : JsResult[JsObject]
  def delete(obj: S) : JsResult[Boolean]
}
   /*
case class MemoryStorage() extends Storage

case class CacheStorage() extends Storage

case class DatabaseStorage() extends Storage
     */
