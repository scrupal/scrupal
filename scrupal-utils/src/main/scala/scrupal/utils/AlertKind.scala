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

package scrupal.utils

import org.joda.time.DateTime

/** The kinds of alerts that can be generated. Selecting the alert kind can also pre-select the prefix text, css class,
  * icon and expiration time of the alert via the various kind2xxx methods on this enumeration. The Alert class makes
  * use of those methods in its constructors
  */
object AlertKind extends Enumeration {
  type Kind = Value
  val Success = Value("Success") ///< Denotes a successful operation that was completed
  val Note = Value("Note") ///< An FYI for the user to take note of
  val Help = Value("Help") ///< A helpful tip the user may need to proceed successfully
  val Warning = Value("Warning") ///< Warning about something the user has just done and how it will affect things later
  val Caution = Value("Caution") ///< Alternate to Warning
  val Error = Value("Error") ///< An error in the user's input or use of the site
  val Danger = Value("Danger") ///< A caution about the potential loss of information or other significant action
  val Critical = Value("Critical") ///< Alternate to Danger

  def toPrefix(kind : Kind) : String =
    {
      kind match {
        case Success  ⇒ "Success!"
        case Note     ⇒ "Note:"
        case Warning  ⇒ "Warning!"
        case Caution  ⇒ "Caution!"
        case Error    ⇒ "Error:"
        case Danger   ⇒ "Danger!"
        case Critical ⇒ "Critical!"
      }
    }

  def toIcon(kind : Kind) : Icons.Kind =
    {
      kind match {
        case Success  ⇒ Icons.ok
        case Note     ⇒ Icons.info
        case Warning  ⇒ Icons.exclamation
        case Caution  ⇒ Icons.exclamation
        case Error    ⇒ Icons.remove
        case Danger   ⇒ Icons.warning_sign
        case Critical ⇒ Icons.warning_sign
      }
    }

  def toCss(kind : Kind) : String = {
    kind match {
      case Success  ⇒ "alert-success"
      case Note     ⇒ "alert-info"
      case Warning  ⇒ ""
      case Caution  ⇒ ""
      case Error    ⇒ "alert-danger"
      case Danger   ⇒ "alert-danger"
      case Critical ⇒ "alert-danger"
    }
  }

  def toExpiry(kind : Kind) : Option[DateTime] = {
    Some(kind match {
      case Success  ⇒ DateTime.now().plusMillis(100)
      case Note     ⇒ DateTime.now().plusSeconds(30)
      case Warning  ⇒ DateTime.now().plusMinutes(30)
      case Caution  ⇒ DateTime.now().plusHours(1)
      case Error    ⇒ DateTime.now().plusHours(4)
      case Danger   ⇒ DateTime.now().plusHours(12)
      case Critical ⇒ DateTime.now().plusHours(24)
    })
  }

}
