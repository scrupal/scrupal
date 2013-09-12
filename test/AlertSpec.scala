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
package scrupal.core.models.test

import org.specs2.mutable.Specification
import org.joda.time.DateTime

import scrupal.models.{Alert, AlertKind}
import scrupal.utils.Icons
import play.api.templates.Html
import scala.concurrent.duration.span
import play.api.Logger

/**
 * One line sentence description here.
 * Further description here.
 */
class AlertSpec extends Specification
{
  import scrupal.models.Alert._
	"Alert" should {

	  val t = DateTime.parse("2014-01-01T00:00:00")

		"construct with one argument and give sane results" in {
			val alert = Alert.from( <span>Note Message</span> )
			alert.message.toString must beEqualTo("<span>Note Message</span>")
			alert.alertKind must beEqualTo(AlertKind.Note)
			alert.iconKind must beEqualTo(Icons.info)
			alert.cssClass must beEqualTo("alert-info")
			alert.prefix must beEqualTo("Note:")
		}
		"construct with two arguments and give sane results" in {
			val alert = Alert.from(AlertKind.Danger, <span>Danger Message</span> )
			alert.message.toString must beEqualTo("<span>Danger Message</span>")
			alert.alertKind must beEqualTo(AlertKind.Danger)
			alert.iconKind must beEqualTo(Icons.warning_sign)
			alert.cssClass must beEqualTo("alert-danger")
			alert.prefix must beEqualTo("Danger!")
		}
		"construct with three arguments and give sane results" in {
			val alert = Alert.from(AlertKind.Warning, <span>Warning Message</span>, t )
			alert.message.toString must beEqualTo("<span>Warning Message</span>")
			alert.alertKind must beEqualTo(AlertKind.Warning)
			alert.iconKind must beEqualTo(Icons.exclamation)
			alert.prefix must beEqualTo("Warning!")
			alert.cssClass must beEqualTo("")
			alert.expires must beEqualTo( t)
		}
		"construct with four arguments and give sane results" in {
			val alert = Alert.from(AlertKind.Caution, <span>Caution Message</span>, Icons.align_center, "Alignment!")
			alert.message.toString must beEqualTo("<span>Caution Message</span>")
			alert.alertKind must beEqualTo(AlertKind.Caution)
			alert.iconKind must beEqualTo(Icons.align_center)
			alert.cssClass must beEqualTo("")
			alert.prefix must beEqualTo("Alignment!")
		}
		"construct with five arguments and give sane results" in {
			val alert = Alert.from(AlertKind.Success, <span>Success Message</span>, Icons.globe, "Goodness!", "alert-success")
			alert.alertKind must beEqualTo(AlertKind.Success)
			alert.cssClass must beEqualTo("alert-success")
			alert.prefix must beEqualTo("Goodness!")
			alert.message.toString must beEqualTo("<span>Success Message</span>")
		}
		"construct with six arguments and give sane results" in {
			val alert = Alert.from(AlertKind.Critical, <span>Critical Message</span>, Icons.heart, "Heart!", "alert-success", t)
			alert.alertKind must beEqualTo(AlertKind.Critical)
			alert.cssClass must beEqualTo("alert-success")
			alert.prefix must beEqualTo("Heart!")
			alert.message.toString must beEqualTo("<span>Critical Message</span>")
			alert.expires must beEqualTo( t )
		}
		"produce correct icon html" in {
			val alert = Alert.from(<span>Html Message</span>)
			alert.iconHtml.toString must beEqualTo("<i class=\"icon-info\"></i>")
		}
	}
}
