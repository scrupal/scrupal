/**********************************************************************************************************************
 * This file is part of Scrupal a Web Application Framework.                                                          *
 *                                                                                                                    *
 * Copyright (c) 2014, Reid Spencer and viritude llc. All Rights Reserved.                                            *
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

package scrupal.db


import org.joda.time.DateTime
import play.api.libs.json.Json
import play.twirl.api.Html
import scrupal.api.{Thing, Identifier}
import scrupal.utils.Icons

import scala.xml.{NodeSeq, Node, Elem}

/**
 * Representation of an alert message that is shown at the top of every page. Alerts are queued and each user
 * has a "latest" alert they've seen. Alerts expire, however, so it is possible for a user to miss an alert.
 * The intent is to provide a way to provide cross-site alerting of system notices such as down time or new
 * feature enhancements.
 * @param name Name of the Alert
 * @param description Brief description of the alert
 * @param message Text of the message to deliver to users
 * @param alertKind The kind of alert
 * @param iconKind The icon to use in the alert
 * @param prefix The prefix label to use in the alert
 * @param cssClass The cssClass name to use in the alert
 * @param expires The time at which the alert expires
 */
case class Alert (
  _id : Identifier,
  override val name : Symbol,
  override val description : String,
  message: String,
  alertKind: AlertKind.Value,
  iconKind : Icons.Value,
  prefix : String,
  cssClass : String,
  expires: DateTime,
  override val modified : Option[DateTime] = None,
  override val created : Option[DateTime] = None
) extends Thing
{
  /**
   * A shorthand constructor for Alerts.
   * This makes it possible to construct alerts with fewer parameters. The remaining parameters are chosen sanely
   * based on the alertKind parameter, which defaults to a Note
   * @param name Label of the Alert
   * @param description Brief description of the alert
   * @param message Text of the message to deliver to users
   * @param alertKind The kind of alert
   */
  def this(name: Symbol, description : String, message: String, alertKind: AlertKind.Value) =
  {
    this(name, name, description, message, alertKind, AlertKind.toIcon(alertKind),  AlertKind.toPrefix(alertKind),
          AlertKind.toCss(alertKind), AlertKind.toExpiry(alertKind), None, None)
  }

  def iconHtml : Html = Icons.html(iconKind)
  def expired : Boolean = expires.isBeforeNow
  def unexpired : Boolean = !expired

  implicit def Elem2Html(e : Elem) : Html = Html(e.buildString(stripComments = true))
  implicit def Node2Html(n : Node) : Html = Html(n.buildString(stripComments = true))
  implicit def NodeSeq2Html(ns : NodeSeq) : Html = {
    Html(ns.foldLeft[StringBuilder](new StringBuilder) { (s,n) => s.append( n.buildString(stripComments=true))}.toString())
  }

}

object Alert {
  import scrupal.api.Symbol_Format
  implicit val Alert_Format = Json.format[Alert]
}
