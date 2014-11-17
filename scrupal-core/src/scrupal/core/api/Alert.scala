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

package scrupal.core.api

import org.joda.time.DateTime
import play.twirl.api.Html
import reactivemongo.api.DefaultDB
import reactivemongo.api.indexes.{IndexType, Index}
import reactivemongo.bson._
import reactivemongo.bson.Macros._
import scrupal.db.{IdentifierDAO, Storable}
import scrupal.utils.{AlertKind, Icons}

import scala.xml.{Elem, NodeSeq}

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
 * @param expiry The time at which the alert expires
 */
case class Alert (
  _id : Identifier,
  name : String,
  description : String,
  message: String,
  alertKind: AlertKind.Value,
  iconKind : Icons.Value,
  prefix : String,
  cssClass : String,
  expiry: Option[DateTime],
  override val modified : Option[DateTime] = None,
  override val created : Option[DateTime] = None
) extends Storable[Identifier] with Nameable with Describable with Modifiable with Expirable
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
  def this(id: Identifier, name: String, description : String, message: String, alertKind: AlertKind.Value) =
  {
    this(id, name, description, message, alertKind, AlertKind.toIcon(alertKind),  AlertKind.toPrefix(alertKind),
          AlertKind.toCss(alertKind), AlertKind.toExpiry(alertKind), None, None)
  }

  def iconHtml : Html = Icons.html(iconKind)

  implicit def Elem2Html(e : Elem) : Html = Html(e.buildString(stripComments = true))
  implicit def Node2Html(n : scala.xml.Node) : Html = Html(n.buildString(stripComments = true))
  implicit def NodeSeq2Html(ns : NodeSeq) : Html = {
    Html(ns.foldLeft[StringBuilder](new StringBuilder) { (s,n) => s.append( n.buildString(stripComments=true))}.toString())
  }

}

object Alert {

  import BSONHandlers._

  implicit val AlertHandler = handler[Alert]

  case class AlertDAO(db: DefaultDB) extends IdentifierDAO[Alert] {
    implicit val reader : IdentifierDAO[Alert]#Reader = Macros.reader[Alert]
    implicit val writer : IdentifierDAO[Alert]#Writer = Macros.writer[Alert]
    final def collectionName : String = "alerts"
    override def indices : Traversable[Index] = super.indices ++ Seq(
      Index(key = Seq("_id" -> IndexType.Ascending), name = Some("UniqueId"))
    )
  }


}
