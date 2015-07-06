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

/** Simple Reverse Routing For Scrupal.
  *
  * Reverse Routing is the process of generating a URI path for a certain resource. In Scrupal, paths are not arbitrary
  * but highly structured and regular. Because of this regularity, the corresponding path can be constructed quite
  * easily especially from a given context. Every operation in Scrupal has a context in which that operation occurs.
  * The context provides the site, application, request context, and other details of the operation that is
  * requesting a path. Because the context is present, the path can be generated easily by simply substituting context
  * and requested elements. Some level of validation can occur too.
  *
  */
object PathOf {
  // TODO: Add validation and context awareness, customization by applications, entities, etc.

  def favicon()(implicit context: Context) = {
    s"/assets/images/${context.favicon}"
  }

  def lib(library: String, path: String)(implicit context: Context) = {
    s"/assets/lib/$library/$path"
  }

  def webjar(library: String, path: String)(implicit context: Context) = {
    s"/webjar/$library/$path"
  }

  def theme(themeName: String)(implicit context: Context) = {
    DataCache.themes.get(themeName) match {
      case Some(thm) ⇒
        thm.`css-min`
      case None ⇒
        s"/assets/theme/$themeName"
    }
  }

  def bsjs(file: String) = {
    s"/assets/bsjs/$file"
  }

  def css(name: String)(implicit context: Context) = {
    s"/assets/stylesheets/$name.min.css"
  }

  def js(name: String)(implicit context: Context) = {
    s"/assets/javascripts/$name.min.js"
  }
}
