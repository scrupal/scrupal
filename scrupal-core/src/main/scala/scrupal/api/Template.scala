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

package scrupal.api

import akka.http.scaladsl.model.MediaType
import scrupal.utils.{ Registry, Registrable }

trait Template[T] extends Registrable[Template[_]] with Describable with ((Map[String, T], Context) ⇒ T) {
  def contentType : MediaType
  def registry = Template
  def asT : this.type = this
}

object Template extends Registry[Template[_]] {
  val registryName = "Templates"
  val registrantsName = "template"

  /*
  /** Handle reading/writing Template instances to and from BSON.
    * Note that templates are a little special. We write them as strings and restore them via lookup. Templates are
    * intended to only ever live in memory but they can be references in the database. So when a Template is a field
    * of some class that is stored in the database, what actually gets stored is just the name of the template.
    */
  class BSONHandlerForTemplate[T <: Registrable[_]] extends BSONHandler[BSONString, T] {
    override def write(t : T) : BSONString = BSONString(t.id.name)
    override def read(bson : BSONString) : T = Template.as(Symbol(bson.value))
  }
    */
}

