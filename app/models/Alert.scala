/**********************************************************************************************************************
 * This file is part of Scrupal a Web Application Framework.                                                          *
 *                                                                                                                    *
 * Copyright (c) 2013, Reid Spencer and viritude llc. All Rights Reserved.                                            *
 *                                                                                                                    *
 * Scrupal is free software: you can redistribute it and/or modify it under the terms                                 *
 * of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License,   *
 * or (at your option) any later version.                                                                             *
 *                                                                                                                    *
 * Scrupal is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied      *
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more      *
 * details.                                                                                                           *
 *                                                                                                                    *
 * You should have received a copy of the GNU General Public License along with Scrupal. If not, see either:          *
 * http://www.gnu.org/licenses or http://opensource.org/licenses/GPL-3.0.                                             *
 **********************************************************************************************************************/

package scrupal.models

import org.joda.time.DateTime
import scrupal.utils.Icons
import play.api.libs.json._
import play.api.templates.Html
import play.api.libs.json
import scala.xml.{Elem, NodeSeq, Node}

/**
 * The kinds of alerts that can be generated. Selecting the alert kind can also pre-select the prefix text, css class,
 * icon and expiration time of the alert.
 */
object AlertKind extends Enumeration
{
	type AlertKind = Value
	val Success = Value("Success")      ///< Denotes a successful operation that was completed
	val Note = Value("Note")            ///< An FYI for the user to take note of
	val Help = Value("Help")            ///< A helpful tip the user may need to proceed successfully
	val Warning = Value("Warning")      ///< Warning about something the user has just done and how it will affect things later
	val Caution = Value("Caution")      ///< Alternate to Warning
	val Error = Value("Error")          ///< An error in the user's input or use of the site
	val Danger = Value("Danger")        ///< A caution about the potential loss of information or other significant action
	val Critical = Value("Critical")    ///< Alternate to Danger
}

import AlertKind._

/**
 * Representation of an alert message that is shown at the top of every page. Alerts are queued and each user
 * has a "latest" alert they've seen. Alerts expire, however, so it is possible for a user to miss an alert.
 * The intent is to provide a way to provide cross-site alerting of system notices such as down time or new
 * feature enhancements.
 */
case class Alert(
    alertKind: AlertKind.AlertKind,
    message: Html,
    iconKind : Icons.Icons,
    prefix : String,
    cssClass : String,
    expires: DateTime
) extends Entity
{
	def this(message: Html)(implicit kind: AlertKind = AlertKind.Note ) = {
		this(kind, message,  Alert.iconForKind(kind), Alert.prefixForKind(kind), Alert.cssClassForKind(kind),
			Alert.expirationForKind(kind))
	}

	def this(kind: AlertKind, message: Html) = {
		this(kind, message, Alert.iconForKind(kind), Alert.prefixForKind(kind), Alert.cssClassForKind(kind),
			Alert.expirationForKind(kind))
	}

	def this(kind: AlertKind, message: Html, iconKind: Icons.Icons) = {
		this(kind, message, iconKind, Alert.prefixForKind(kind), Alert.cssClassForKind(kind),
			Alert.expirationForKind(kind))
	}

	def this(kind: AlertKind, message: Html, prefix: String) = {
		this(kind, message, Alert.iconForKind(kind), prefix, Alert.cssClassForKind(kind), Alert.expirationForKind(kind))
	}

	def this(kind: AlertKind, message: Html, iconKind: Icons.Icons, prefix: String) = {
		this(kind, message, iconKind, prefix, Alert.cssClassForKind(kind), Alert.expirationForKind(kind))
	}

	def this(kind: AlertKind, message:Html, iconKind: Icons.Icons, prefix: String, cssClass: String) = {
		this(kind, message, iconKind, prefix, cssClass, Alert.expirationForKind(kind))
	}

	def this(kind: AlertKind, message: Html, expiry: DateTime) = {
		this(kind, message, Alert.iconForKind(kind), Alert.prefixForKind(kind), Alert.cssClassForKind(kind), expiry)
	}

	def iconHtml : Html = Icons.html(iconKind);
}

/**
 * Companion object for Alert class. Just provides some constructor help.
 */
object Alert
{
	def from(message: Html) = new Alert(message)
	def from(kind: AlertKind, msg: Html) = new Alert(kind, msg)
	def from(kind: AlertKind, msg: Html, iconKind: Icons.Icons) = new Alert(kind, msg, iconKind)
	def from(kind: AlertKind, msg: Html, iconKind: Icons.Icons, prefix: String) = new Alert(kind, msg, iconKind, prefix)
	def from(kind: AlertKind, msg: Html, prefix: String) = new Alert(kind, msg, prefix)
	def from(kind: AlertKind, msg: Html, iconKind: Icons.Icons, prefix: String, cssClass: String) =
		new Alert(kind, msg, iconKind, prefix, cssClass)
  def from(kind: AlertKind, msg: Html, iconKind: Icons.Icons, prefix: String, cssClass: String, expires: DateTime) =
    new Alert(kind, msg, iconKind, prefix, cssClass, expires)
	def from(kind: AlertKind, msg: Html, expiry: DateTime) = new Alert(kind, msg, expiry)

	def prefixForKind(kind: AlertKind) : String =
	{
		kind match {
			case Success => "Success!"
			case Note => "Note:"
			case Warning => "Warning!"
			case Caution => "Caution!"
			case Error => "Error:"
			case Danger => "Danger!"
			case Critical => "Critical!"
		}
	}

	def iconForKind(kind: AlertKind) : Icons.Icons =
	{
		kind match {
			case Success => Icons.ok
			case Note => Icons.info
			case Warning => Icons.exclamation
			case Caution => Icons.exclamation
			case Error => Icons.remove
			case Danger => Icons.warning_sign
			case Critical => Icons.warning_sign
		}
	}

	def cssClassForKind(kind: AlertKind) : String = {
		kind match {
			case Success => "alert-success"
			case Note => "alert-info"
			case Warning => ""
			case Caution => ""
			case Error => "alert-danger"
			case Danger => "alert-danger"
			case Critical => "alert-danger"
		}
	}

	def expirationForKind(kind: AlertKind) : DateTime = {
		kind match {
			case Success => DateTime.now().plusMillis(100)
			case Note => DateTime.now().plusSeconds(30)
			case Warning => DateTime.now().plusMinutes(30)
			case Caution => DateTime.now().plusHours(1)
			case Error => DateTime.now().plusHours(4)
			case Danger => DateTime.now().plusHours(12)
			case Critical => DateTime.now().plusHours(24)
		}
	}

  implicit def Elem2Html(e : Elem) : Html = Html(e.buildString(stripComments = true))
  implicit def Node2Html(n : Node) : Html = Html(n.buildString(stripComments = true))
  implicit def NodeSeq2Html(ns : NodeSeq) : Html = {
    Html(ns.foldLeft[StringBuilder](new StringBuilder) { (s,n) => s.append( n.buildString(true))}.toString)
  }

  implicit val htmlReader : Reads[Html] = new Reads[Html] {
    def reads(jsValue : JsValue) : JsResult[Html] = {
      (jsValue \ "html" ).validate[String].map { h => Html(h) }
    }
  }

  implicit val htmlWriter : Writes[Html] = new Writes[Html] {
    def writes(h : Html) : JsValue = JsString(h.toString)
  }

  implicit val htmlFormat : Format[Html] = Format(htmlReader, htmlWriter)

  implicit val alertKindReader : Reads[AlertKind] = new Reads[AlertKind] {
    def reads(jsValue: JsValue) : JsResult[AlertKind] = {
      (jsValue \ "alert_kind").validate[String].map { s => AlertKind.withName(s) }
    }
  }

  implicit val alertKindWriter : Writes[AlertKind] = new Writes[AlertKind] {
    def writes(alert: AlertKind) : JsValue = {
      JsString(alert.toString)
    }
  }

  implicit val AlertKindFormatter : Format[AlertKind] = Format(alertKindReader, alertKindWriter)

  implicit val formatter : Format[Alert] = Json.format[Alert]


}

