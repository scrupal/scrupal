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

package scrupal.core.html

import java.io.{ PrintWriter, StringWriter }

import org.apache.commons.lang3.exception.ExceptionUtils
import org.joda.time.DateTime
import reactivemongo.bson._
import scrupal.core.api.Html._
import scrupal.core.api.{ Feature, Context }
import scalatags.Text.Modifier

import scalatags.Text.all._

case class danger(message : Contents) extends SimpleGenerator {
  def apply() : Contents = { Seq(div(cls := "bg-danger", message)) }
}

case class warning(message : Contents) extends SimpleGenerator {
  def apply() : Contents = { Seq(div(cls := "bg-warning", message)) }
}

case class success(message : Contents) extends SimpleGenerator {
  def apply() : Contents = { Seq(div(cls := "bg-success", message)) }
}

case class exception(activity : String, error : Throwable) extends SimpleGenerator {
  def apply() : Contents = {
    danger(Seq(
      p(s"While attempting to ${activity} an exception occurred:"),
      display_exception(error)()
    ))()
  }
}

object display_context_table extends FragmentGenerator {
  def apply(context : Context) = {
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

object debug_footer extends FragmentGenerator {
  def apply(context : Context) = {
    if (Feature.enabled('DebugFooter, context.scrupal)) {
      display_context_table(context)
    } else {
      emptyContents
    }
  }
}

object display_alerts extends FragmentGenerator {
  def apply(context : Context) : Contents = {
    for (alert ← context.alerts if alert.unexpired) yield {
      div(cls := "alert alert-dismissible @alert.cssClass",
        button(`type` := "button", cls := "close", data("dismiss") := "alert", aria.hidden := "true",
          i(cls := "icon-remove-sign")),
        strong(alert.iconHtml, "&nbsp;", alert.prefix), "&nbsp;", alert.message)
    }
  }
}

case class display_exception(xcptn : Throwable) extends SimpleGenerator {
  def apply() = {
    Seq(
      dl(cls := "dl-horizontal",
        dt("Exception:"), dd(xcptn.getClass.getName),
        dt("Message:"), dd(xcptn.getLocalizedMessage),
        dt("Root Cause:"), dd(
          pre(style := "width:95%", code(style := "font-size:8pt", {
            var sw : StringWriter = null
            var pw : PrintWriter = null
            try {
              sw = new StringWriter()
              pw = new PrintWriter(sw)
              ExceptionUtils.printRootCauseStackTrace(xcptn, pw)
              sw.toString
            } finally {
              if (pw != null) pw.close()
              if (sw != null) sw.close()
            }
          })),
          br()
        )
      )
    )
  }
}

case class display_exception_result(xcptn : scrupal.core.api.ExceptionResult) extends SimpleGenerator {
  def apply() = { Seq(div(cls := "bg-danger", display_exception(xcptn.payload)())) }
}

trait bson_fragment extends SimpleGenerator {
  def value(value : BSONValue) : Modifier = {
    value match {
      case s : BSONString   ⇒ "\"" + s.value + "\""
      case i : BSONInteger  ⇒ i.value.toString
      case l : BSONLong     ⇒ l.value + "L"
      case d : BSONDouble   ⇒ d.value + "D"
      case b : BSONBoolean  ⇒ b.value.toString
      case x : BSONDateTime ⇒ new DateTime(x.value).toString
      case a : BSONArray    ⇒ array(a)
      case d : BSONDocument ⇒ document(d)
      case b : BSONBinary   ⇒ s"Binary(${b.subtype})"
      case x : BSONValue    ⇒ s"Code(${x.code.toInt})"
    }

  }
  def array(array : BSONArray) : Modifier = {
    div(s"Array(${array.length}) [",
      {
        for (e ← array.values) { Seq(value(e), ", ") }
      },
      "]"
    )
  }

  def document(doc : BSONDocument) : Modifier = {
    div(s"Document(${doc.elements.length}) {",
      dl(cls := "dl-horizontal",
        for ((k, v) ← doc.elements) { Seq(dt(k), dd(value(v))) }
      ),
      "}"
    )
  }
}

case class bson_value(bv : BSONValue) extends bson_fragment {
  def apply() : Contents = { Seq(span(value(bv))) }
}

case class bson_document_panel(title : String, doc : BSONDocument) extends bson_fragment {
  def apply() = Seq (
    div(cls := "panel panel-primary",
      div(cls := "panel-heading",
        h3(cls := "panel-title", title)
      ),
      div(cls := "panel-body", document(doc))
    )
  )
}

object reactific_copyright extends SimpleGenerator {
  def apply() = {
    Seq(sub(sup("Copyright &copy; 2012-2014, Reactific Software LLC. All Rights Reserved.")))
  }
}
