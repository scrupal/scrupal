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
 */
case class Alert (
    description : String,
    message: String,
    alertKind: AlertKind.Value = AlertKind.Note,
    iconKind : Icons.Value,
    prefix : String,
    cssClass : String,
    expires: DateTime,
    override val id : Option[Long],
    override val module_id : Long,
    override val label : String,
    override val created : DateTime
) extends Entity[Alert]
{
  /**
   * Shorthand constructor for Alerts
   *
   * @param id - The ID of the alert, or (typically) None
   * @param module_id - The ID of the module from which the alert was generated
   * @param label - The name of the alert
   * @param created - The timestamp of the creation of the
   * @param description
   * @param message
   * @param alertKind
   */
  def this(id: Option[Long], module_id: Long, label: String, created: DateTime,
           description : String, message: String, alertKind: AlertKind.Value = AlertKind.Note) =
  {
    this(description, message, alertKind, kind2icon(alertKind), kind2prefix(alertKind), kind2css(alertKind),
          kind2expiry(alertKind), id, module_id, label, created)
  }

  def forId(id: Long) = Alert(description, message, alertKind, iconKind,  prefix, cssClass, expires,
                              Some(id), module_id, label, created)
  def iconHtml : Html = Icons.html(iconKind);
  def expiresInTheFuture : Boolean = expires.isAfterNow

  implicit def Elem2Html(e : Elem) : Html = Html(e.buildString(stripComments = true))
  implicit def Node2Html(n : Node) : Html = Html(n.buildString(stripComments = true))
  implicit def NodeSeq2Html(ns : NodeSeq) : Html = {
    Html(ns.foldLeft[StringBuilder](new StringBuilder) { (s,n) => s.append( n.buildString(true))}.toString)
  }

}

