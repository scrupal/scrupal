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

package scrupal.storage.impl

import java.nio.charset.StandardCharsets

import play.api.libs.json._
import scrupal.storage.api.{StorageFormatter, Storable, StorageFormat}

case class JsonFormat(jsv: JsValue) extends StorageFormat {
  def toBytes: Array[Byte] = Json.stringify(jsv).getBytes(StandardCharsets.UTF_8)
}

case class JsonFormatter[S <: Storable](format: Format[S])
  extends StorageFormatter[JsonFormat, S]
{
  def write(s : S) : JsonFormat = JsonFormat(format.writes(s))
  def read(data: JsonFormat) : S = {
    val jsv = Json.parse(data.toBytes)
    format.reads(jsv) match {
      case x: JsSuccess[S] ⇒ x.value;
      case x: JsError ⇒ throw new Exception(JsError.toFlatForm(x).toString())
    }
  }
}

