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

import java.io.{PrintWriter, StringWriter}

import org.apache.commons.lang3.exception.ExceptionUtils
import org.joda.time.DateTime
import reactivemongo.bson._
import scrupal.core.api.Html._
import scrupal.core.api.{Feature, Context}
import scalatags.Text.Modifier

import scalatags.Text.all._

case class danger(message: Contents) extends SimpleContentsGenerator {
  def apply() : Contents = { Seq(div(cls:="bg-danger", message)) }
}

case class warning(message: Contents) extends SimpleContentsGenerator {
  def apply() : Contents = { Seq(div(cls:="bg-warning", message)) }
}

case class success(message: Contents) extends SimpleContentsGenerator {
  def apply() : Contents = { Seq(div(cls:="bg-success", message)) }
}

case class exception(activity: String, error: Throwable) extends SimpleContentsGenerator {
  def apply() : Contents = {
    danger(Seq(
      p(s"While attempting to ${activity} an exception occurred:"),
      display_exception(error)()
    ))()
  }
}


object display_context_table extends ContentsGenerator {
  def apply(context: Context) = {
    Seq(div(cls := "span10 row", style := "font-size: 0.75em",
      table(cls := "span10 table table-striped table-bordered table-condensed",
        caption(style := "font-size: 1.2em; font-weight: bold;", "Context Details"),
        thead(tr(th("Parameter"), th("Value"))),
        tbody(
          tr(th("Site"), td(context.siteName)),
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
    ))
  }
}

object debug_footer extends ContentsGenerator {
  def apply(context: Context) = {
    if (Feature.enabled('DebugFooter, context.scrupal)) {
      display_context_table(context)
    } else {
      emptyContents
    }
  }
}

object display_alerts extends ContentsGenerator {
  def apply(context: Context) : Contents = {
    for (alert ← context.alerts if alert.unexpired) yield {
      div(cls := "alert alert-dismissible @alert.cssClass",
        button(`type` := "button", cls := "close", data("dismiss") := "alert", aria.hidden := "true",
          i(cls := "icon-remove-sign")),
        strong(alert.iconHtml, "&nbsp;", alert.prefix), "&nbsp;", alert.message)
    }
  }
}

case class display_exception(xcptn: Throwable) extends SimpleContentsGenerator {
  def apply() = {
    Seq(
      dl(cls:="dl-horizontal",
        dt("Exception:"),dd(xcptn.getClass.getName),
        dt("Message:"),dd(xcptn.getLocalizedMessage),
        dt("Root Cause:"),dd(
          pre(style:="width:95%", code(style:="font-size:8pt",{
            var sw: StringWriter = null
            var pw: PrintWriter = null
            try {
              sw = new StringWriter()
              pw = new PrintWriter(sw)
              ExceptionUtils.printRootCauseStackTrace(xcptn, pw)
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

case class display_exception_result(xcptn: scrupal.core.api.ExceptionResult) extends SimpleContentsGenerator {
  def apply() = { Seq(div(cls:="bg-danger", display_exception(xcptn.payload)()))  }
}

trait bson_fragment extends SimpleContentsGenerator {
  def value(value: BSONValue) : Modifier = {
    value match {
      case s: BSONString ⇒ "\"" + s.value + "\""
      case i: BSONInteger ⇒ i.value.toString
      case l: BSONLong ⇒ l.value + "L"
      case d: BSONDouble ⇒ d.value + "D"
      case b: BSONBoolean ⇒ b.value.toString
      case x: BSONDateTime ⇒ new DateTime(x.value).toString
      case a: BSONArray ⇒ array(a)
      case d: BSONDocument ⇒ document(d)
      case b: BSONBinary ⇒ s"Binary(${b.subtype})"
      case x: BSONValue ⇒ s"Code(${x.code.toInt})"
    }

  }
  def array(array: BSONArray) : Modifier = {
    div(s"Array(${array.length}) [",
    {
      for (e ← array.values) { Seq(value(e), ", ") }
    },
    "]"
    )
  }

  def document(doc: BSONDocument) : Modifier = {
    div(s"Document(${doc.elements.length}) {",
      dl(cls:="dl-horizontal",
        for ((k, v) ← doc.elements) { Seq(dt(k), dd(value(v))) }
      ),
      "}"
    )
  }
}

case class bson_value(bv: BSONValue) extends bson_fragment {
  def apply() : Contents = { Seq(span(value(bv)) ) }
}

case class bson_document_panel(title: String, doc: BSONDocument) extends bson_fragment {
  def apply() = Seq (
    div(cls:="panel panel-primary",
      div(cls:="panel-heading",
        h3(cls:="panel-title", title)
      ),
      div(cls:="panel-body",document(doc))
    )
  )
}

object reactific_copyright extends SimpleContentsGenerator {
  def apply() = {
    Seq( sub(sup("Copyright &copy; 2012-2014, Reactific Software LLC. All Rights Reserved.")) )
  }
}
