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
import play.api.libs.json._
import scrupal.utils.Icons

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

  implicit object AlertKind_Format extends Format[AlertKind.Value] {
    def reads(json: JsValue) : JsResult[AlertKind.Value] = { JsSuccess(AlertKind.Value(json.asInstanceOf[JsString].value)) }
    def writes(a: AlertKind.Value) : JsValue = JsString(a.toString)
  }
}
