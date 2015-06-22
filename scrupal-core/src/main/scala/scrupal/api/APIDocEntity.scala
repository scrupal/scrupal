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

import play.api.libs.json.JsObject
import scrupal.api.types.BundleType

/** API Documentation Helper For An Entity
  *
  * Entities provide an API by which their functionality is invoked. Being able to utilize the API depends on
  * documentation that corresponds to the Entity API and is accurate and up to date. To this end, this class provides
  * the means to auto-generated API Documentation for entities and by extending it enhance that documentation.
  *
  * @tparam E - The Entity type this EntityAPIDoc documents
  */
class APIDocEntity[E <: Entity](id : Symbol)(implicit val scrupal : Scrupal) extends Entity(id) {
  def instanceType : BundleType = ???

  val author = scrupal.author
  val copyright = scrupal.copyright
  val license = scrupal.license

  def kind : Symbol = ???

  def description : String = ???

}
