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

package scrupal.core

import scrupal.api.{BundleType, DataCache, Markdown_t, SelectionType, Title_t}

object PageBundle_t
  extends BundleType('PageBundle, "Information bundle for a page entity.",
    fields = Map (
      "title" -> Title_t,
      "body" -> Markdown_t
      // TODO: Figure out how to structure a bundle to factor in constructing a network of nodes
      // 'master -> Node_t,
      // 'defaultLayout -> Node_t,
      // 'body -> Node_t,
      // 'blocks ->
    )
  )

object Theme_t extends SelectionType('Theme, "Choice of themes", DataCache.themes.keys.toSeq)

object Site_t extends SelectionType('Site, "Choice of sites", DataCache.sites)

