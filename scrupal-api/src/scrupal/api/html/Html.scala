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

package scrupal.api.html

import scrupal.api.{Context, PathOf}

import scalatags.Text.{tags2, TypedTag}
import scalatags.Text.all._

trait Fragment {
  type TagContent = TypedTag[String]
  type TagsContent = Seq[TypedTag[String]]
  type Modifiers = Seq[Modifier]
  def js(javascript: String) = script(`type`:="text/javascript", javascript)
  object ng {
    val app = "ng-app".attr
    val controller = "ng-controller".attr
    val show = "ng-show".attr
    val hide = "ng-hide".attr
  }
  def ng(name: String) = ("ng-" + name).attr
  def apply(context: Context) : String
}

abstract class TagFragment extends Fragment {
  def contents(context: Context) : TagContent
  def apply(context: Context) : String = contents(context).toString()
}

abstract class TagsFragment extends Fragment {
  def contents(context: Context) : TagsContent
  def apply(context: Context) : String = contents(context).toString()
}

abstract class ModifiersFragment extends Fragment {
  def contents(context: Context) : Modifiers
  def apply(context: Context) : String = contents(context).toString()
}

abstract class Page(theTitle: String, theDescription: String) extends TagFragment {
  def headTitle(context: Context) : TagContent = tags2.title(theTitle)
  def headDescription(context: Context) : TagContent = {
    meta(name := "description", content := theDescription)
  }
  def favIcon(context: Context) : TagContent = {
    link(rel := "shortcut icon", `type` := "image/x-icon", href := PathOf.favicon()(context))
  }
  def headSuffix(context: Context) : Seq[Modifier]
  def headTag(context: Context) : TagContent = {
    head(
      headTitle(context),
      headDescription(context),
      meta(charset := "UTF-8"),
      meta(name := "viewport", content := "width=device-width, initial-scale=1.0"),
      favIcon(context),
      headSuffix(context)
    )
  }
  def bodyPrefix(context: Context) : Seq[Modifier]
  def bodyMain(context: Context) : Seq[Modifier]
  def bodySuffix(context: Context) : Seq[Modifier]
  def bodyTag(context: Context) : TagContent = {
    body(
      bodyPrefix(context), bodyMain(context), bodySuffix(context)
    )
  }
  def contents(context: Context) : TagContent = html(headTag(context), bodyTag(context))

  override def apply(context: Context) : String = "<!DOCTYPE html>" + contents(context)
}
