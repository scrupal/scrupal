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

import scrupal.storage.api.Schema

abstract class DataCache {

  def update(scrupal : Scrupal, schema : Schema)
}

object DataCache extends DataCache {

  private var _themes = Seq.empty[String]

  def themes : Seq[String] = _themes

  def update(scrupal : Scrupal, schema : Schema) = {
    _themes = Seq("amelia", "cyborg", "default", "readable") // FIXME: allow new themes from theme providers in modules
    _sites = scrupal.Sites.values.map { site ⇒ site.name }
  }

  private var _sites = Seq.empty[String]
  def sites : Seq[String] = _sites
}
