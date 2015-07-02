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

import scrupal.storage.api.{Collection, Schema}
import scrupal.utils.ScrupalUtilsInfo

import scala.concurrent.{Future, ExecutionContext}

abstract class DataCache {

  def update(scrupal : Scrupal, schema : Schema)
}

object DataCache extends DataCache {

  private var _themes = Seq.empty[String]

  def themes : Seq[String] = _themes

  private var _sites = Seq.empty[String]
  def sites : Seq[String] = _sites

  private var _alerts = Seq.empty[Alert]
  def alerts : Seq[Alert] = _alerts

  def update(scrupal : Scrupal, schema : Schema) : Unit = {
    _themes = ScrupalUtilsInfo.themes
    scrupal.withExecutionContext { implicit ec: ExecutionContext ⇒
      val f1 = {
        schema.withCollection("alerts") { alertsColl : Collection[Alert] ⇒
          alertsColl.fetchAll().map { alerts ⇒
            val unexpired = for (a ← alerts if a.unexpired) yield { a }
            _alerts = unexpired.toSeq
          }
        }
      }
      val f2 = Future {
        _sites = scrupal.Sites.values.map { site ⇒ site.name }
      }
      Future sequence Seq(f1, f2)
    }
  }
}
