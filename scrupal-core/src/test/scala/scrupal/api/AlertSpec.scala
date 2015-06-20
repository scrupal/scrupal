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

package scrupal.api

import java.util.concurrent.TimeUnit

import org.joda.time.DateTime
import scrupal.test.ScrupalApiSpecification
import scrupal.utils.Icons
import scrupal.utils.AlertKind

import scala.concurrent.Await
import scala.concurrent.duration.Duration

/**
 * One line sentence description here.
 * Further description here.
 */
class AlertSpec extends ScrupalApiSpecification("AlertSpec") {


	"Alert" should {

		lazy val t :DateTime =  DateTime.now().plusDays(1)

		"construct with one argument and give sane results" in {
			val alert =  new Alert( 'Alert, "Alert", "Description", "<span>Note Message</span>", AlertKind.Note )
			alert.message.toString must beEqualTo("<span>Note Message</span>")
			alert.alertKind must beEqualTo(AlertKind.Note)
			alert.iconKind must beEqualTo(Icons.info)
			alert.cssClass must beEqualTo("alert-info")
			alert.prefix must beEqualTo("Note:")
		}

		"construct with two arguments and give sane results" in {
			val alert = new Alert('Alert, "Alert", "Description", "<span>Danger Message</span>", AlertKind.Danger )
			alert.message.toString must beEqualTo("<span>Danger Message</span>")
			alert.alertKind must beEqualTo(AlertKind.Danger)
			alert.iconKind must beEqualTo(Icons.warning_sign)
			alert.cssClass must beEqualTo("alert-danger")
			alert.prefix must beEqualTo("Danger!")
		}

		"construct with three arguments and give sane results" in {
			val alert = Alert('Alert, "Alert", "Description", "<span>Warning Message</span>", AlertKind.Warning,
        AlertKind.toIcon(AlertKind.Warning), AlertKind.toPrefix(AlertKind.Warning),
        AlertKind.toCss(AlertKind.Warning), Some(t) )
			alert.message.toString must beEqualTo("<span>Warning Message</span>")
			alert.alertKind must beEqualTo(AlertKind.Warning)
			alert.iconKind must beEqualTo(Icons.exclamation)
			alert.prefix must beEqualTo("Warning!")
			alert.cssClass must beEqualTo("")
			alert.expiry.get must beEqualTo( t)
		}

		"construct with four arguments and give sane results" in {
			val alert = Alert('Alert, "Alert", "Description", "<span>Caution Message</span>", AlertKind.Caution,
        Icons.align_center, "Alignment!", "", Some(new DateTime(0)))
			alert.message.toString must beEqualTo("<span>Caution Message</span>")
			alert.alertKind must beEqualTo(AlertKind.Caution)
			alert.iconKind must beEqualTo(Icons.align_center)
			alert.cssClass must beEqualTo("")
			alert.prefix must beEqualTo("Alignment!")
		}

		"produce correct icon html" in {
			val alert = new Alert('A, "A", "Description", "<span>Html Message</span>", AlertKind.Note)
			alert.iconHtml.toString must beEqualTo("<i class=\"icon-info\"></i>")
		}

    "save to and fetch from the DB" in  {
      withSchema { schema: Schema =>
				withEmptyDB(schema.nodes.db.name) { db =>
					val future = db.dropCollection("alerts") flatMap { result =>
						val a1 = new Alert('foo, "Alert", "Description", "Message", AlertKind.Warning )
						schema.alerts.insert(a1) flatMap { wr =>
							schema.alerts.fetch('foo) map { optAlert =>
								val a2 = optAlert.get
								val saved_time = a2.expires
								a2._id must beEqualTo('foo)
								a1._id must beEqualTo('foo)
							}
						}
					}
					Await.result(future,Duration(5,TimeUnit.SECONDS))
				}
      }
    }
	}
}
