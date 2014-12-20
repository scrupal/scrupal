/**********************************************************************************************************************
 * Copyright © 2014 Reactific Software LLC                                                                            *
 *                                                                                                                    *
 * This file is part of Scrupal, an Opinionated Web Application Framework.                                            *
 *                                                                                                                    *
 * Scrupal is free software: you can redistribute it and/or modify it under the terms                                 *
 * of the GNU General Public License as published by the Free Software Foundation,                                    *
 * either version 3 of the License, or (at your option) any later version.                                            *
 *                                                                                                                    *
 * Scrupal is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;                               *
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                          *
 * See the GNU General Public License for more details.                                                               *
 *                                                                                                                    *
 * You should have received a copy of the GNU General Public License along with Scrupal.                              *
 * If not, see either: http://www.gnu.org/licenses or http://opensource.org/licenses/GPL-3.0.                         *
 **********************************************************************************************************************/

package scrupal.core.api

import reactivemongo.bson.{BSONHandler, BSONString}
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

  def renderContents(contents: Contents) : String = {
    val sb = new StringBuilder(4096)
    for (tag ← contents) {sb.append(tag.toString)}
    sb.toString()
  }

  trait ContentsGenerator extends ((Context) ⇒ Contents) {
    def render(context: Context) : String = { renderContents(apply(context)) }
  }

  trait TemplateGenerator extends ((Context,Map[String,Fragment]) ⇒ Contents) {
    def render(context: Context, args: Map[String,Fragment]) : String = { renderContents(apply(context, args)) }
  }

  trait SimpleContentsGenerator {
    def apply() : Contents
    def render() : String = { renderContents(apply()) }
  }

  def js(javascript: String) = script(`type`:="text/javascript", javascript)

  object ng {
    val app = "ng-app".attr
    val controller = "ng-controller".attr
    val show = "ng-show".attr
    val hide = "ng-hide".attr
  }

  def ng(name: String) = ("ng-" + name).attr

  case class Fragment(id: Identifier, description: String)(gen: ContentsGenerator)
    extends Registrable[Fragment] with Describable with ContentsGenerator
  {
    def registry = Fragment
    def apply(context:Context) : Contents = gen(context)
  }

  object Fragment extends Registry[Fragment] {
    def registryName = "Html Fragments"
    def registrantsName = "html fragment"

    class BSONHandlerForHtmlFragment[T <: Fragment]  extends BSONHandler[BSONString,T] {
      override def write(t: T): BSONString = BSONString(t.id.name)
      override def read(bson: BSONString): T = Fragment.as(Symbol(bson.value))
    }
  }

  case class Template(id: Symbol, description: String)(gen: TemplateGenerator)
    extends  Registrable[Template] with Describable with TemplateGenerator
  {
    def registry = Template
    def apply(context: Context, args: Map[String,Fragment]) : Contents = gen(context, args)
  }

  object Template extends Registry[Template] {
    def registryName = "Html Templates"
    def registrantsName = "html template"

    class BSONHandlerForHtmlTemplate[T <: Template]  extends BSONHandler[BSONString,T] {
      override def write(t: T): BSONString = BSONString(t.id.name)
      override def read(bson: BSONString): T = Template.as(Symbol(bson.value))
    }
  }

  case class Tag(tag: TypedTag[String]) extends ContentsGenerator  {
    def apply(context:Context) : Contents = Seq(tag)
  }

  abstract class Page(val theTitle: String, val theDescription: String) extends ContentsGenerator {
    def headTitle(context: Context)  : TagContent = tags2.title(theTitle)
    def headDescription(context: Context) : TagContent = {
      meta(name := "description", content := theDescription)
    }
    def favIcon(context: Context) : TagContent = {
      link(rel := "shortcut icon", `type` := "image/x-icon", href := PathOf.favicon()(context))
    }
    def headSuffix(context: Context)  : Contents
    def headTag(context: Context)  : TagContent = {
      head(
        headTitle(context),
        headDescription(context),
        meta(charset := "UTF-8"),
        meta(name := "viewport", content := "width=device-width, initial-scale=1.0"),
        favIcon(context),
        headSuffix(context)
      )
    }
    def bodyPrefix(context: Context) : Contents
    def bodyMain(context: Context) : Contents
    def bodySuffix(context: Context) : Contents
    def bodyTag(context: Context) : TagContent = {
      body(
        bodyPrefix(context), bodyMain(context), bodySuffix(context)
      )
    }
    def apply(context: Context): Contents = {
      Seq[TagContent](scalatags.Text.all.html(headTag(context), bodyTag(context)))
    }
    override def render(context: Context) : String = {
      val sb = new StringBuilder(4096)
      sb.append("<!DOCTYPE html>")
      for (tag ← apply(context)) {sb.append(tag.toString)}
      sb.toString()
    }
  }
}


