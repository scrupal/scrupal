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

package scrupal.api.html

import java.time.Instant
import java.time.format.DateTimeFormatter

import scrupal.api.Html.TagContent

import scalatags.Text.all._
import scalatags.Text.attrs

object Forms {

  def form(path: String, name: String, items: Modifier*) = {
    scalatags.Text.tags.form(
      scalatags.Text.attrs.action := path, method := "POST", attrs.name := name,
      "enctype".attr := "application/x-www-form-urlencoded", items)
  }

  def with_checked(is_checked : Boolean) = {
    if (is_checked)
      Seq(checked := "checked")
    else
      Seq.empty[AttrPair]
  }

  def with_value[T](fld_value : Option[T]) : Seq[AttrPair] = {
    fld_value match {
      case Some(v) ⇒ Seq(scalatags.Text.attrs.value := v.toString)
      case None    ⇒ Seq.empty[AttrPair]
    }
  }

  def checkbox(fld_name : String, is_checked : Boolean, attributes : AttrPair*) = {
    input(`type` := "checkbox", name := fld_name, with_checked(is_checked), with_value(Some(fld_name)), attributes)
  }

  def color(fld_name : String, fld_value : Option[String], attributes : AttrPair*) = {
    input(`type` := "color", name := fld_name, with_value(fld_value), attributes)
  }

  def date(fld_name : String, fld_value : Option[Instant], attributes : AttrPair*) = {
    input(`type` := "date", name := fld_name, with_value(fld_value), attributes)
  }

  def datetime(fld_name : String, fld_value : Option[Instant], attributes : AttrPair*) = {
    val value = fld_value match {
      case Some(dt) ⇒
        Seq(scalatags.Text.attrs.value := DateTimeFormatter.ISO_INSTANT.format(dt))
      case None ⇒
        Seq.empty[AttrPair]
    }
    input(`type` := "datetime", name := fld_name, value, attributes)
  }

  def datetime_local(fld_name : String, fld_value : Option[Instant], attributes : AttrPair*) = {
    val value = fld_value match {
      case Some(dt) ⇒
        Seq(scalatags.Text.attrs.value := DateTimeFormatter.ISO_INSTANT.format(dt))
      case None ⇒
        Seq.empty[AttrPair]
    }
    input(`type` := "datetime-local", name := fld_name, value, attributes)
  }

  def email(fld_name : String, fld_value : Option[String], attributes: AttrPair*) = {

    input(`type` := "email", name := fld_name, with_value(fld_value), attributes)
  }

  def file(fld_name : String, fld_value : Option[String], attributes : AttrPair*) = {
    input(`type` := "file", name := fld_name, with_value(fld_value), attributes)
  }

  def hidden(fld_name : String, fld_value : Option[String], attributes : AttrPair*) = {
    input(`type` := "hidden", name := fld_name, with_value(fld_value), attributes)
  }

  def image(fld_name : String, attributes : AttrPair*) = {
    input(`type` := "image", name := fld_name, attributes)
  }

  def month(fld_name : String, fld_value : Option[Instant], attributes : AttrPair*) = {
    input(`type` := "month", name := fld_name, with_value(fld_value), attributes)
  }

  def number(fld_name : String, fld_value : Option[Double], minVal : Double, maxVal : Double, attrs : AttrPair*) = {
    input(`type` := "number", name := fld_name, min := minVal, max := maxVal, with_value(fld_value), attrs)
  }

  def password(fld_name : String, fld_value : Option[String], attributes : AttrPair*) = {
    input(`type` := "password", name := fld_name, with_value(fld_value), attributes)
  }

  def radio(fld_name : String, vlu : Option[String], is_checked : Boolean, attributes : AttrPair*) = {
    input(`type` := "radio", name := fld_name, with_checked(is_checked), with_value(vlu), attributes)
  }

  def range(fld_name : String, fld_value : Option[Double], minVal : Double, maxVal : Double, attrs : AttrPair*) = {
    input(`type` := "range", name := fld_name, min := minVal, max := maxVal, with_value(fld_value), attrs)
  }

  def reset(fld_name : String, fld_value : Option[String], attributes : AttrPair*) = {
    input(`type` := "reset", name := fld_name, with_value(fld_value), attributes)
  }

  def search(fld_name : String, fld_value : Option[String], attributes : AttrPair*) = {
    input(`type` := "search", name := fld_name, with_value(fld_value), attributes)
  }

  def submit(fld_name : String, submitted_value : String, attributes : AttrPair*) = {
    input(`type` := "submit", name := fld_name, value := submitted_value, attributes)
  }

  def telephone(fld_name : String, fld_value : Option[String], attributes : AttrPair*) = {
    input(`type` := "tel", name := fld_name, with_value(fld_value), attributes)
  }

  def text(fld_name : String, fld_value : Option[String], attributes : AttrPair*) = {
    input(`type` := "text", name := fld_name, with_value(fld_value), attributes)
  }

  def time(fld_name : String, fld_value : Option[Instant], attributes : AttrPair*) = {
    input(`type` := "time", name := fld_name, with_value(fld_value), attributes)
  }

  def url(fld_name : String, fld_value : Option[String], attributes : AttrPair*) = {
    input(`type` := "url", name := fld_name, with_value(fld_value), attributes)
  }

  def week(fld_name : String, fld_value : Option[String], attributes : AttrPair*) = {
    input(`type` := "week", name := fld_name, with_value(fld_value), attributes)
  }

  def list(fld_name : String, fld_value : Option[String], datalist : String, attributes : AttrPair*) = {
    input(`type` := "list", name := fld_name, id := fld_name, scalatags.Text.attrs.list := datalist, with_value(fld_value),
      attributes)
  }

  def datalist(fld_id : String, options : Seq[String]) = {
    scalatags.Text.tags.datalist(id := fld_id, for (opt ← options) { option(value := opt) })
  }

  def textarea(fld_name : String, fld_value : Option[String], attributes : AttrPair*) = {
    scalatags.Text.tags.textarea(name := fld_name, with_value(fld_value), attributes)
  }

  def select(fld_name : String, fld_value : Option[String], options : Map[String, String],
    attributes : AttrPair*) = {
    val opts : Seq[TagContent] = {
      for (key ← options.keys.toSeq.sorted) yield {
        val valu : String = options.getOrElse(key, key)
        option(value := valu, key)
      }
    }
    scalatags.Text.tags.select(name := fld_name, with_value(fld_value), attributes, opts)
  }

  def multiselect(fld_name : String, fld_value : Seq[String], options : Map[String, String],
    attributes : AttrPair*) = {
    scalatags.Text.tags.select(name := fld_name + "[]", id := fld_name, multiple := "multiple", attributes,
      for ((the_value, label) ← options) {
        option(value := the_value,
          if (fld_value.contains(the_value)) { Seq(scalatags.Text.attrs.selected := "") } else Seq.empty[AttrPair], label)
      }
    )
  }

  def button(label : String, fld_value : Option[String], attributes : AttrPair*) = {
    scalatags.Text.tags.button(`type` := "button", with_value(fld_value), attributes, label)
  }
  def reset_button(label : String, fld_value : Option[String], attributes : AttrPair*) = {
    scalatags.Text.tags.button(`type` := "reset", with_value(fld_value), attributes, label)
  }
  def submit_button(label : String, fld_value : Option[String], attributes : AttrPair*) = {
    scalatags.Text.tags.button(`type` := "submit", with_value(fld_value), attributes, label)
  }

  def label(l : String, for_field : String, attributes : AttrPair*) = {
    scalatags.Text.tags.label(`for` := for_field, attributes, l)
  }

  def output(attr_name : String, for_attr : String, attributes : AttrPair*) = {
    val output_tag = "output".tag[String]
    output_tag(name := attr_name, `for` := for_attr, attributes)
  }
}
