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
package scrupal.db

import java.util.concurrent.TimeUnit

import org.specs2.mutable.Specification
import scrupal.utils.Icons
import org.joda.time.DateTime
import scrupal.fakes.{WithFakeScrupal}

import scala.concurrent.Await
import scala.concurrent.duration.Duration

/**
 * One line sentence description here.
 * Further description here.
 */
class AlertSpec extends Specification
{
	"Alert" should {

	  val t :DateTime =  DateTime.now().plusDays(1)

		"construct with one argument and give sane results" in {
			val alert =  new Alert( 'Alert, "Alert", "<span>Note Message</span>", AlertKind.Note )
			alert.message.toString must beEqualTo("<span>Note Message</span>")
			alert.alertKind must beEqualTo(AlertKind.Note)
			alert.iconKind must beEqualTo(Icons.info)
			alert.cssClass must beEqualTo("alert-info")
			alert.prefix must beEqualTo("Note:")
		}
		"construct with two arguments and give sane results" in {
			val alert = new Alert('Alert, "Alert", "<span>Danger Message</span>", AlertKind.Danger )
			alert.message.toString must beEqualTo("<span>Danger Message</span>")
			alert.alertKind must beEqualTo(AlertKind.Danger)
			alert.iconKind must beEqualTo(Icons.warning_sign)
			alert.cssClass must beEqualTo("alert-danger")
			alert.prefix must beEqualTo("Danger!")
		}
		"construct with three arguments and give sane results" in {
			val alert = Alert('Alert, 'Alert, "Alert", "<span>Warning Message</span>", AlertKind.Warning,
        AlertKind.toIcon(AlertKind.Warning), AlertKind.toPrefix(AlertKind.Warning),
        AlertKind.toCss(AlertKind.Warning), t )
			alert.message.toString must beEqualTo("<span>Warning Message</span>")
			alert.alertKind must beEqualTo(AlertKind.Warning)
			alert.iconKind must beEqualTo(Icons.exclamation)
			alert.prefix must beEqualTo("Warning!")
			alert.cssClass must beEqualTo("")
			alert.expires must beEqualTo( t)
		}
		"construct with four arguments and give sane results" in {
			val alert = Alert('Alert, 'Alert, "Alert", "<span>Caution Message</span>", AlertKind.Caution,
        Icons.align_center, "Alignment!", "", new DateTime(0))
			alert.message.toString must beEqualTo("<span>Caution Message</span>")
			alert.alertKind must beEqualTo(AlertKind.Caution)
			alert.iconKind must beEqualTo(Icons.align_center)
			alert.cssClass must beEqualTo("")
			alert.prefix must beEqualTo("Alignment!")
		}
		"produce correct icon html" in {
			val alert = new Alert('A, "A", "<span>Html Message</span>", AlertKind.Note)
			alert.iconHtml.toString must beEqualTo("<i class=\"icon-info\"></i>")
		}
    "save to and fetch from the DB" in  new WithFakeScrupal {
      withCoreSchema { schema: CoreSchema =>
        import schema._
        val a1 = new Alert('foo, "Alert", "Message", AlertKind.Warning )
        val fa1 = alerts.insert(a1)
        Await.result(fa1,Duration(1,TimeUnit.SECONDS)).ok must beTrue
        val a2 = Await.result(alerts.fetch('foo),Duration(1,TimeUnit.SECONDS)).get
        val saved_time = a2.expires
        a2.id must beEqualTo('foo)
        a1.id must beEqualTo('foo)
      }
    }
	}
}
