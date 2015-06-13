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

import scrupal.utils.{Registrable, Registry}

import scalatags.Text.all._
import scalatags.Text.{TypedTag, tags2}
import scalatags.generic.AttrPair
import scalatags.text.Builder

object Html {
  type AttrContent = AttrPair[Builder, String]
  type TagContent = TypedTag[String]
  type Contents = Seq[Modifier]
  val emptyContents = Seq.empty[Modifier]

  def js(javascript : String) = script(`type` := "application/javascript", javascript)
  def jslib(lib : String, path : String) = script(`type` := "application/javascript", src := s"/assets/lib/$lib/$path")

  val nbsp = raw("&nbsp;")

  object ng {
    val app = "ng-app".attr
    val controller = "ng-controller".attr
    val show = "ng-show".attr
    val hide = "ng-hide".attr
  }

  def ng(name : String) = ("ng-" + name).attr

  def renderContents(contents : Contents) : String = {
    val sb = new StringBuilder(4096)
    for (tag ← contents) { sb.append(tag.toString) }
    sb.toString()
  }

  type ContentsArgs = Map[String, Generator]
  val EmptyContentsArgs = Map.empty[String, Generator]

  trait Generator {
    def generate(context : Context, args : ContentsArgs) : Contents
    def render(context : Context, args : ContentsArgs) : String
    def tag(tagName : String, context : Context, args : ContentsArgs) : Contents = {
      args.get(tagName) match {
        case Some(v) ⇒ v.generate(context, args)
        case None    ⇒ Seq("")
      }
    }
  }

  trait SimpleGenerator extends Generator with (() ⇒ Contents) {
    def generate(context : Context, args : ContentsArgs) : Contents = {
      apply()
    }
    def render(context : Context, args : ContentsArgs) : String = {
      renderContents(apply())
    }
  }

  trait FragmentGenerator extends Generator with ((Context) ⇒ Contents) {
    def generate(context : Context, args : ContentsArgs) : Contents = {
      this.apply(context)
    }
    def render(context : Context, args : ContentsArgs) : String = {
      renderContents(apply(context))
    }
  }

  trait TemplateGenerator extends Generator with ((Context, ContentsArgs) ⇒ Contents) {
    def generate(context : Context, args : ContentsArgs) : Contents = {
      this.apply(context, args)
    }
    def render(context : Context, args : ContentsArgs) : String = {
      renderContents(apply(context, args))
    }
  }

  /** ********************************************************************************************************************
    * This file is part of Scrupal, a Scalable Reactive Web Application Framework for Content Management                 *
    *                                                                                                                   *
    * Copyright (c) 2015, Reactific Software LLC. All Rights Reserved.                                                   *
    *                                                                                                                   *
    * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance     *
    * with the License. You may obtain a copy of the License at                                                          *
    *                                                                                                                   *
    *    http://www.apache.org/licenses/LICENSE-2.0                                                                     *
    *                                                                                                                   *
    * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed   *
    * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for  *
    * the specific language governing permissions and limitations under the License.                                     *
    * ********************************************************************************************************************
    */

  abstract class Template(_i : Symbol) extends { val id : Symbol = _i } with Registrable[Template] with Describable with TemplateGenerator {
    def registry = Template
  }

  object Template extends Registry[Template] {
    def registryName = "Html Templates"
    def registrantsName = "html template"

    /*
    class BSONHandlerForHtmlTemplate[T <: Template] extends BSONHandler[BSONString, T] {
      override def write(t : T) : BSONString = BSONString(t.id.name)
      override def read(bson : BSONString) : T = Template.as(Symbol(bson.value))
    }
    */
  }

  trait PageGenerator extends Describable with TemplateGenerator {
    def title : String
    def headTitle(context : Context, args : ContentsArgs = EmptyContentsArgs) : TagContent = {
      tags2.title(title)
    }
    def headDescription(context : Context, args : ContentsArgs = EmptyContentsArgs) : TagContent = {
      meta(name := "description", content := description)
    }
    def favIcon(context : Context, args : ContentsArgs = EmptyContentsArgs) : TagContent = {
      link(rel := "shortcut icon", `type` := "image/x-icon", href := PathOf.favicon()(context))
    }
    def headSuffix(context : Context, args : ContentsArgs = EmptyContentsArgs) : Contents
    def headTag(context : Context, args : ContentsArgs = EmptyContentsArgs) : TagContent = {
      head(
        headTitle(context, args),
        headDescription(context, args),
        meta(charset := "UTF-8"),
        meta(name := "viewport", content := "width=device-width, initial-scale=1.0"),
        favIcon(context, args),
        headSuffix(context, args)
      )
    }
    def bodyPrefix(context : Context, args : ContentsArgs = EmptyContentsArgs) : Contents
    def bodyMain(context : Context, args : ContentsArgs = EmptyContentsArgs) : Contents
    def bodySuffix(context : Context, args : ContentsArgs = EmptyContentsArgs) : Contents
    def bodyTag(context : Context, args : ContentsArgs = EmptyContentsArgs) : TagContent = {
      body(
        bodyPrefix(context, args), bodyMain(context, args), bodySuffix(context, args)
      )
    }
    def apply(context : Context, args : ContentsArgs = EmptyContentsArgs) : Contents = {
      Seq[TagContent](scalatags.Text.all.html(headTag(context, args), bodyTag(context, args)))
    }
    override def render(context : Context, args : ContentsArgs = EmptyContentsArgs) : String = {
      val sb = new StringBuilder(4096)
      sb.append("<!DOCTYPE html>")
      for (tag ← generate(context, args)) {
        sb.append(tag.toString)
      }
      sb.toString()
    }
  }

  abstract class Page(val title : String, val description : String) extends PageGenerator

  abstract class TemplatePage(_id : Symbol, val title : String, val description : String)
    extends Template(_id) with PageGenerator
}
