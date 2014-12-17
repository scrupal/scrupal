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

import org.joda.time.DateTime
import reactivemongo.bson._
import scrupal.api.Context

import scalatags.Text.all._


object display_context_table extends TagFragment
{
  override def contents(context: Context) = {
    div(cls := "span10 row", style := "font-size: 0.75em",
      table(cls := "span10 table table-striped table-bordered table-condensed",
        caption(style := "font-size: 1.2em; font-weight: bold;", "Context Details"),
        thead(tr(th("Parameter"), th("Value"))),
        tbody(
          tr(th("Site"), td(context.siteName)),
          tr(th("Application"), td(context.appName)),
          tr(th("User"), td(context.user)),
          tr(th("Theme"), td(context.themeName))
        )
      ),
      table(cls := "span10 table table-striped table-bordered table-condensed",
        caption(style := "font-size: 1.2em; font-weight: bold;", "Request Header Details"),
        thead(tr(th("Parameter"), th("Value"))),
        tbody(
          tr(th("Request"), td(context.method.toString(), ": ", context.uri.toString())),
          tr(th("Protocol"), td(context.protocol.toString())),
          tr(th("Headers"), td(context.headers.toString()))
        )
      )
    )
  }
}

object display_alerts extends TagsFragment {
  override def contents(context: Context) = {
    for (alert ← context.alerts if alert.unexpired) yield {
      div(cls := "alert alert-dismissible @alert.cssClass",
        button(`type` := "button", cls := "close", data("dismiss") := "alert", aria.hidden := "true",
          i(cls := "icon-remove-sign")),
        strong(alert.iconHtml, "&nbsp;", alert.prefix), "&nbsp;", alert.message)
    }
  }
}

case class display_exception_result(xcptn: scrupal.api.ExceptionResult) extends TagFragment {
  import java.io.StringWriter
  import java.io.PrintWriter
  import org.apache.commons.lang3.exception.ExceptionUtils
  def contents(context: Context) = {
    div(cls:="bg-danger",
      dl(cls:="dl-horizontal",
        dt("Exception:"),dd(xcptn.payload.getClass.getCanonicalName),
        dt("Message:"),dd(xcptn.payload.getLocalizedMessage),
        dt("Root Cause:"),dd(
          pre(style:="width:95%", code(style:="font-size:8pt",{
            var sw: StringWriter = null
            var pw: PrintWriter = null
            try {
              sw = new StringWriter()
              pw = new PrintWriter(sw)
              ExceptionUtils.printRootCauseStackTrace(xcptn.payload, pw)
              sw.toString
            } finally {
              if(pw != null)  pw.close()
              if(sw != null)  sw.close()
            }
          })),
          br()
        )
      )
    )
  }
}

case class bson_value(bv: BSONValue) extends TagFragment {
  def contents(context: Context) : TagContent = {
    span(
      bv match {
        case s: BSONString ⇒ span("\"" + s.value + "\"")
        case i: BSONInteger ⇒ span(i.value.toString)
        case l: BSONLong ⇒ span(l.value + "L")
        case d: BSONDouble ⇒ span(d.value + "D")
        case b: BSONBoolean ⇒ span(b.value.toString)
        case x: BSONDateTime ⇒ span(new DateTime(x.value).toString)
        case a: BSONArray ⇒ bson_array(a).contents(context)
        case d: BSONDocument ⇒ bson_document(d).contents(context)
        case b: BSONBinary ⇒ span(s"Binary(${b.subtype})")
        case x: BSONValue ⇒ span(s"Code(${x.code.toInt})")
      }
    )
  }
}

case class bson_array(array: BSONArray) extends TagFragment {
  def contents(context: Context) = {
    div(s"Array(${array.length}) [", {
      for (e ← array.values) {
        bson_value(e)
      }
    }, "]"
    )
  }
}

case class bson_document(doc: BSONDocument) extends TagFragment {
  def contents(context: Context) = {
    dl(cls:="dl-horizontal", {
      for ((k, v) ← doc.elements) yield {
        Seq(dt(k), dd(bson_value(v).contents(context)))
      }
    }
    )
  }
}

case class bson_document_panel(title: String, doc: BSONDocument) extends TagFragment {
  def contents(context: Context) = {
    div(cls:="panel panel-primary",
      div(cls:="panel-heading",
        h3(cls:="panel-title", title)
      ),
      div(cls:="panel-body",bson_document(doc).contents(context))
    )
  }
}

object reactific_copyright extends TagFragment {
  def contents(context: Context) = {
    sub(sup("Copyright &copy; 2012-2014, Reactific Software LLC. All Rights Reserved."))
  }
}
