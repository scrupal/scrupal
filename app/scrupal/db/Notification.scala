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

package scrupal.models.db

import play.api.templates.Html
import scala.xml.{Elem, NodeSeq, Node}
import scala.Enumeration

import org.joda.time.{Duration, DateTime}
import scala.slick.lifted.{ DDL}

import scrupal.utils.Icons
import scrupal.api.{Identifier, Thing, Component}

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

  def toPrefix(kind: AlertKind) : String =
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

  def toIcon(kind: AlertKind) : Icons.Icons =
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

  def toCss(kind: AlertKind) : String = {
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

  def toExpiry(kind: AlertKind) : DateTime = {
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
  override val name : Symbol,
  override val description : String,
  message: String,
  alertKind: AlertKind.Value,
  iconKind : Icons.Value,
  prefix : String,
  cssClass : String,
  expires: DateTime,
  override val modified : Option[DateTime] = None,
  override val created : Option[DateTime] = None,
  override val id : Option[Identifier] = None
) extends Thing(name, description, modified, created, id)
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
  def this(name: Symbol, description : String, message: String, alertKind: AlertKind.Value = AlertKind.Note) =
  {
    this(name, description, message, alertKind, toIcon(alertKind),  toPrefix(alertKind), toCss(alertKind),
         toExpiry(alertKind))
  }

  def forId(id: Long) = Alert(name, description, message, alertKind, iconKind,  prefix, cssClass, expires)
  def iconHtml : Html = Icons.html(iconKind);
  def expired : Boolean = expires.isBeforeNow
  def unexpired : Boolean = !expired

  implicit def Elem2Html(e : Elem) : Html = Html(e.buildString(stripComments = true))
  implicit def Node2Html(n : Node) : Html = Html(n.buildString(stripComments = true))
  implicit def NodeSeq2Html(ns : NodeSeq) : Html = {
    Html(ns.foldLeft[StringBuilder](new StringBuilder) { (s,n) => s.append( n.buildString(true))}.toString)
  }

}

trait NotificationComponent extends Component {

  import sketch.profile.simple._

  // Get the TypeMapper for DateTime
  import CommonTypeMappers._

  // This allows you to use AlertKind.Value as a type argument to the column method in a table definition
  implicit val iconTM = MappedTypeMapper.base[Icons.Value,Int]( { icon => icon.id }, { id => Icons(id)})

  // This allows you to use AlertKind.Value as a type argument to the column method in a table definition
  implicit val alertTM = MappedTypeMapper.base[AlertKind.Value,Int]( { alert => alert.id }, { id => AlertKind(id) } )


  object Alerts extends ScrupalTable[Alert]("alerts") with ThingTable[Alert] {
    def message =     column[String](tableName + "_message")
    def alertKind =   column[AlertKind.Value](tableName + "_alertKind")
    def iconKind =    column[Icons.Value](tableName + "_iconKind")
    def prefix =      column[String](tableName + "_prefix")
    def cssClass =    column[String](tableName + "_css")
    def expires =     column[DateTime](tableName + "_expires")
    def expires_index = index(tableName + "_expires_index", expires, unique=false)
    def * = name ~ description  ~ message ~ alertKind  ~ iconKind ~ prefix ~ cssClass ~ expires ~ modified.? ~
      created.? ~ id.?  <> (Alert.tupled, Alert.unapply _ )

    lazy val unexpiredQuery = {
      for {
        expires <- Parameters[DateTime] ;
        alrt <- this if alrt.expires > expires
      } yield alrt
    }

    def findUnexpired(implicit session: Session) : List[Alert] =  {  unexpiredQuery(DateTime.now()).list }

    def renew(theID: Long, howLong: Duration)(implicit session: Session) = {
      val query = for {  alrt <- this if alrt.id === theID } yield alrt.expires
      query.update(DateTime.now().plus(howLong))
    }
  }

  def notificationDDL : DDL = Alerts.ddl

}

