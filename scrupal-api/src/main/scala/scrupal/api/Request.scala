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

import akka.http.scaladsl.model.{MediaTypes, MediaType}
import akka.util.ByteString
import play.api.libs.iteratee.Enumerator

trait Request {
  def context : Context
  def entity : String
  def instance : String
  def message : Iterable[String] = Iterable.empty[String]
}

trait RequestDetails {
  def request : Request
  def mediaType : MediaType = MediaTypes.`application/octet-stream`
  def payload : Enumerator[ByteString] = Enumerator.empty[ByteString]
  def parameters : Map[String,String] = Map.empty[String,String]
}

case class SimpleRequest(context : Context, entity: String, instance: String, msg: String) extends Request {
  override val message = Seq(msg)
}

object Request {
  val empty = SimpleRequest(null, "", "", "")
}
