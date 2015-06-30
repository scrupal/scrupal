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

import java.time.Instant

/** The kinds of alerts that can be generated are subclasses of this sealed trait. Each alert defines the prefix
  * text, css class, icon and expiration time of the alert via the various kind2xxx methods on this enumeration.
  * Alert instances make use of these values in their constructors.
  */

sealed trait AlertKind {
  def label : String
  def prefix : String
  def icon : Icons.Kind
  def css : String
  def expiry: Instant
}

case object SuccessAlert extends AlertKind{
  val label = "Success"
  val prefix = "Success!"
  val icon = Icons.ok
  val css = "alert alert-success"
  def expiry: Instant = Instant.now().plusMillis(100)
}

case object NoteAlert extends AlertKind {
  val label = "Note"
  val prefix = "Note:"
  val icon = Icons.info
  val css = "alert alert-info"
  def expiry: Instant = Instant.now().plusSeconds(30)
}

case object HelpAlert extends AlertKind {
  val label = "Help"
  val prefix = "Help:"
  val icon = Icons.info
  val css = "alert alert-info"
  def expiry: Instant = Instant.now().plusSeconds(30)

}
case object WarningAlert extends AlertKind {
  val label = "Warning"
  val prefix = "Warning!"
  val icon = Icons.exclamation
  val css = "alert alert-warning"
  def expiry: Instant = Instant.now().plusSeconds(30*60)
}

case object CautionAlert extends AlertKind {
  val label = "Caution"
  val prefix = "Caution!"
  val icon = Icons.exclamation_sign
  val css = "alert alert-warning"
  def expiry: Instant = Instant.now().plusSeconds(60*60)
}

case object ErrorAlert extends AlertKind {
  val label = "Error"
  val prefix = "Error:"
  val icon = Icons.remove
  val css = "alert alert-danger"
  def expiry: Instant = Instant.now().plusSeconds(4*3600)
}

case object DangerAlert extends AlertKind {
  val label = "Danger"
  val prefix = "Danger!"
  val icon = Icons.warning_sign
  val css = "alert alert-danger"
  def expiry: Instant = Instant.now().plusSeconds(12*3600)
}

case object CriticalAlert extends AlertKind {
  val label = "Critical"
  val prefix = "Critial!"
  val icon = Icons.warning_sign
  val css = "alert alert-danger"
  def expiry: Instant = Instant.now().plusSeconds(24*3600)
}
