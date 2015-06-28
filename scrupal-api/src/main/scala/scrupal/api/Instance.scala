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

import org.joda.time.DateTime
import scrupal.storage.api.Storable

/* TODO: Generalize Instance to carry any kind of payload, using VariantDataAccessObject like Node[_]
trait Instance[P] extends Storable with Nameable with Describable with Modifiable with Facetable {
  def payload : P
}

case class BasicInstance(
  _id : Identifier,
  name: String,
  description: String,
  entityId: Identifier,
  payload: BSONDocument,
  facets: Map[String,Facet] = Map.empty[String,Facet],
  modified : Option[DateTime] = None,
  created : Option[DateTime] = None
) {

}
*/

/** The basic unit of storage and operation in Scrupal
  * Further description needed here.
  */
case class Instance(
  name : String,
  description : String,
  tipe : BundleType,
  entityId : Identifier,
  payload : Map[String,Atom],
  facets : Map[String,Facet] = Map.empty[String,Facet],
  modified : Option[DateTime] = None,
  created : Option[DateTime] = None) extends
    Storable with Nameable with Describable with Modifiable with Facetable {
}
