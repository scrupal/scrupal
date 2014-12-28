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

package scrupal.core.html

import java.text.DateFormat
import java.util.{Locale, Date}

import org.joda.time._
import org.joda.time.format.{ISODateTimeFormat, DateTimeFormatter}

import scalatags.Text.all._

object Forms {

  type AttrList = Seq[scalatags.Text.AttrPair]
  val EmptyAttrList = Seq.empty[scalatags.Text.AttrPair]

  def with_checked(is_checked: Boolean) = {
    if (is_checked)
      Seq(checked:="checked")
    else
      Seq.empty[AttrPair]
  }

  def with_value[T](fld_value : Option[T]) : Seq[AttrPair] = {
    fld_value match {
      case Some(v) ⇒ Seq(scalatags.Text.attrs.value:=v.toString)
      case None ⇒ Seq.empty[AttrPair]
    }
  }

  def checkbox(fld_name: String, is_checked: Boolean, attributes: AttrList = EmptyAttrList) = {
    input(`type`:="checkbox", name:=fld_name, with_checked(is_checked), with_value(Some(fld_name)), attributes)
  }

  def color(fld_name: String, fld_value: Option[String], attributes: AttrList = EmptyAttrList) = {
    input(`type`:="color", name:=fld_name, with_value(fld_value), attributes)
  }

  def date(fld_name: String, fld_value: Option[LocalDate], attributes: AttrList = EmptyAttrList) = {
    input(`type`:="date", name:=fld_name, with_value(fld_value), attributes)
  }

  def datetime(fld_name: String, fld_value: Option[DateTime], attributes: AttrList = EmptyAttrList) = {
    val value = fld_value match {
      case Some(dt) ⇒
        Seq(scalatags.Text.attrs.value:=ISODateTimeFormat.dateTime().print(dt.toDateTime(DateTimeZone.UTC)))
      case None ⇒
        Seq.empty[AttrPair]
    }
    input(`type`:="datetime", name:=fld_name, value, attributes)
  }

  def datetime_local(fld_name: String, fld_value: Option[DateTime], attributes: AttrList = EmptyAttrList) = {
    val value = fld_value match {
      case Some(dt) ⇒
        Seq(scalatags.Text.attrs.value:=ISODateTimeFormat.dateTime().print(dt))
      case None ⇒
        Seq.empty[AttrPair]
    }
    input(`type`:="datetime-local", name:=fld_name, value, attributes)
  }

  def email(fld_name: String, fld_value: Option[String], attributes: AttrList = EmptyAttrList) = {

    input(`type`:="email", name:=fld_name, with_value(fld_value), attributes)
  }

  def file(fld_name: String, fld_value: Option[String], attributes: AttrList = EmptyAttrList) = {
    input(`type`:="file", name:=fld_name, with_value(fld_value), attributes)
  }

  def hidden(fld_name: String, fld_value: Option[String], attributes: AttrList = EmptyAttrList) = {
    input(`type`:="hidden", name:=fld_name, with_value(fld_value), attributes)
  }

  def image(fld_name: String, attributes: AttrList = EmptyAttrList) = {
    input(`type`:="image", name:=fld_name, attributes)
  }

  def month(fld_name: String, fld_value: Option[YearMonth], attributes: AttrList = EmptyAttrList) = {
    input(`type`:="month", name:=fld_name, with_value(fld_value), attributes)
  }

  def number(fld_name: String, fld_value: Option[Double], minVal: Double = 0, maxVal:Double = Double.MaxValue,
             attrs: AttrList = EmptyAttrList) = {
    input(`type`:="number", name:=fld_name, min:=minVal, max:=maxVal, with_value(fld_value), attrs)
  }

  def password(fld_name: String, fld_value: Option[String], attributes: AttrList = EmptyAttrList) = {
    input(`type`:="password", name:=fld_name, with_value(fld_value), attributes)
  }

  def radio(fld_name: String, vlu: Option[String], is_checked: Boolean, attributes: AttrList = EmptyAttrList) = {
    input(`type`:="radio", name:=fld_name, with_checked(is_checked), with_value(vlu), attributes)
  }

  def range(fld_name: String, fld_value: Option[Double], minVal: Double = 0, maxVal: Double = Double.MaxValue,
            attrs: AttrList = EmptyAttrList) = {
    input(`type`:="range", name:=fld_name, min:=minVal, max:=maxVal, with_value(fld_value), attrs)
  }

  def reset(fld_name: String, fld_value: Option[String], attributes: AttrList = EmptyAttrList) = {
    input(`type`:="reset", name:=fld_name, with_value(fld_value), attributes)
  }

  def search(fld_name: String, fld_value: Option[String], attributes: AttrList = EmptyAttrList) = {
    input(`type`:="search", name:=fld_name, with_value(fld_value), attributes)
  }

  def submit(fld_name: String, submitted_value: String, attributes: AttrList = EmptyAttrList) = {
    input(`type`:="submit", name:=fld_name, value:=submitted_value, attributes)
  }

  def telephone(fld_name: String,fld_value: Option[String], attributes: AttrList = EmptyAttrList) = {
    input(`type`:="tel", name:=fld_name, with_value(fld_value), attributes)
  }

  def text(fld_name: String, fld_value: Option[String], attributes: AttrList = EmptyAttrList) = {
    input(`type`:="text", name:=fld_name, with_value(fld_value), attributes)
  }

  def time(fld_name: String, fld_value: Option[LocalTime], attributes: AttrList = EmptyAttrList) = {
    input(`type`:="time", name:=fld_name, with_value(fld_value), attributes)
  }

  def url(fld_name: String, fld_value: Option[String], attributes: AttrList = EmptyAttrList) = {
    input(`type`:="url", name:=fld_name, with_value(fld_value), attributes)
  }

  def week(fld_name: String, fld_value: Option[String], attributes: AttrList = EmptyAttrList) = {
    input(`type`:="week", name:=fld_name, with_value(fld_value), attributes)
  }

  def list(fld_name: String, fld_value: Option[String], datalist: String, attributes: AttrList = EmptyAttrList) = {
    input(`type`:="list", name:=fld_name, id:=fld_name, scalatags.Text.attrs.list:=datalist, with_value(fld_value),
          attributes)
  }

  def datalist(fld_id: String, options: Seq[String]) = {
    scalatags.Text.tags.datalist(id:=fld_id, for (opt ← options) { option(value:=opt) } )
  }

  def textarea(fld_name: String, fld_value: Option[String], attributes: AttrList = EmptyAttrList) = {
    scalatags.Text.tags.textarea(name:=fld_name, with_value(fld_value), attributes)
  }

  def select(fld_name: String, fld_value: Option[String], options: Map[String,String],
              attributes: AttrList = EmptyAttrList) = {
    scalatags.Text.tags.select(name:=fld_name, id:=fld_name, with_value(fld_value), attributes,
      for ((the_value, label) ← options) { option(value:=the_value, label) }
    )
  }

  def multiselect(fld_name: String, fld_value: Seq[String], options: Map[String,String],
                  attributes: AttrList = EmptyAttrList) = {
    scalatags.Text.tags.select(name:=fld_name+"[]", id:=fld_name, multiple:="multiple", attributes,
      for ((the_value, label) ← options) { option(value:=the_value,
        if (fld_value.contains(the_value)) { Seq(scalatags.Text.attrs.selected:="") } else Seq.empty[AttrPair], label) }
    )
  }

  def button(label: String, fld_value: Option[String], attributes: AttrList = EmptyAttrList) = {
    scalatags.Text.tags.button(`type`:="button", with_value(fld_value), attributes, label)
  }
  def reset_button(label: String, fld_value: Option[String], attributes: AttrList = EmptyAttrList) = {
    scalatags.Text.tags.button(`type`:="reset", with_value(fld_value), attributes, label)
  }
  def submit_button(label: String, fld_value: Option[String], attributes: AttrList = EmptyAttrList) = {
    scalatags.Text.tags.button(`type`:="submit", with_value(fld_value), attributes, label)
  }

  def label(l: String, for_field: String, attributes: AttrList = EmptyAttrList) = {
    scalatags.Text.tags.label(`for`:=for_field, attributes, l)
  }

  def output(attr_name: String, for_attr: String, attributes: AttrList = EmptyAttrList) = {
    val output_tag = "output".tag[String]
    output_tag(name:=attr_name, `for`:=for_attr, attributes)
  }
}
