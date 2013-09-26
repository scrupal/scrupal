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

import play.api.templates.Html
import scala.xml.{Elem, NodeSeq, Node}

import scrupal.utils.Icons
import scrupal.models.db._
import org.joda.time.DateTime
import scala.Enumeration

/**
 * The kinds of alerts that can be generated. Selecting the alert kind can also pre-select the prefix text, css class,
 * icon and expiration time of the alert via the various kind2xxx methods on this enumeration. The Alert class makes
 * use of those methods in its constructors
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

  def kind2prefix(kind: AlertKind) : String =
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

  def kind2icon(kind: AlertKind) : Icons.Icons =
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

  def kind2css(kind: AlertKind) : String = {
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

  def kind2expiry(kind: AlertKind) : DateTime = {
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

}

import AlertKind._

/**
 * Representation of an alert message that is shown at the top of every page. Alerts are queued and each user
 * has a "latest" alert they've seen. Alerts expire, however, so it is possible for a user to miss an alert.
 * The intent is to provide a way to provide cross-site alerting of system notices such as down time or new
 * feature enhancements.
 * @param id Optional unique identifier for the Alert
 * @param created Timestamp of creation
 * @param label Label of the Alert
 * @param description Brief description of the alert
 * @param message Text of the message to deliver to users
 * @param alertKind The kind of alert
 * @param iconKind The icon to use in the alert
 * @param prefix The prefix label to use in the alert
 * @param cssClass The cssClass name to use in the alert
 * @param expires The time at which the alert expires
 */
case class Alert (
  override val id : Option[Long],
  override val created : DateTime,
  override val label : String,
  override val description : String,
  message: String,
  alertKind: AlertKind.Value,
  iconKind : Icons.Value,
  prefix : String,
  cssClass : String,
  expires: DateTime
) extends Entity[Alert]
{
  /**
   * A shorthand constructor for Alerts.
   * This makes it possible to construct alerts with fewer parameters. The remaining parameters are chosen sanely
   * based on the alertKind parameter, which defaults to a Note
   * @param id Optional unique identifier for the Alert
   * @param created Timestamp of creation
   * @param label Label of the Alert
   * @param description Brief description of the alert
   * @param message Text of the message to deliver to users
   * @param alertKind The kind of alert
   */
  def this(id: Option[Long], created: DateTime, label: String,  description : String,
           message: String, alertKind: AlertKind.Value = AlertKind.Note) =
  {
    this(id, created, label, description, message, alertKind, kind2icon(alertKind),
         kind2prefix(alertKind), kind2css(alertKind), kind2expiry(alertKind))
  }

  def forId(id: Long) = Alert(Some(id), created, label, description,
                              message, alertKind, iconKind,  prefix, cssClass, expires)
  def iconHtml : Html = Icons.html(iconKind);
  def expired : Boolean = expires.isBeforeNow
  def unexpired : Boolean = !expired

  implicit def Elem2Html(e : Elem) : Html = Html(e.buildString(stripComments = true))
  implicit def Node2Html(n : Node) : Html = Html(n.buildString(stripComments = true))
  implicit def NodeSeq2Html(ns : NodeSeq) : Html = {
    Html(ns.foldLeft[StringBuilder](new StringBuilder) { (s,n) => s.append( n.buildString(true))}.toString)
  }

}

