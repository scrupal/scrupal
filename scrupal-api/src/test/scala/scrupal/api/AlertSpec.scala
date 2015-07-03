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

import java.time.Instant

import scrupal.storage.api.{Collection, Schema}
import scrupal.test.ScrupalSpecification
import scrupal.api.html.Icons._

import scala.concurrent.{ExecutionContext, Await}
import scala.concurrent.duration._

/**
 * One line sentence description here.
 * Further description here.
 */
class AlertSpec extends ScrupalSpecification("AlertSpec") {


	"Alert" should {

		lazy val t: Instant = Instant.now().plusSeconds(24 * 3600)

		"construct with one argument and give sane results" in {
			val alert = new Alert('Alert, "Alert", "Description", "<span>Note Message</span>", NoteAlert)
			alert.message.toString must beEqualTo("<span>Note Message</span>")
			alert.alertKind must beEqualTo(NoteAlert)
			alert.icon must beEqualTo(info)
			alert.cssClass must beEqualTo("alert alert-info")
			alert.prefix must beEqualTo("Note:")
		}

		"construct with two arguments and give sane results" in {
			val alert = new Alert('Alert, "Alert", "Description", "<span>Danger Message</span>", DangerAlert)
			alert.message.toString must beEqualTo("<span>Danger Message</span>")
			alert.alertKind must beEqualTo(DangerAlert)
			alert.icon must beEqualTo(warning_sign)
			alert.cssClass must beEqualTo("alert alert-danger")
			alert.prefix must beEqualTo("Danger!")
		}

		"construct with three arguments and give sane results" in {
			val alert = Alert('Alert, "Alert", "Description", "<span>Warning Message</span>", WarningAlert,
				WarningAlert.icon, WarningAlert.prefix, WarningAlert.css, Some(t))
			alert.message.toString must beEqualTo("<span>Warning Message</span>")
			alert.alertKind must beEqualTo(WarningAlert)
			alert.icon must beEqualTo(exclamation)
			alert.prefix must beEqualTo("Warning!")
			alert.cssClass must beEqualTo("alert alert-warning")
			alert.expiry.get must beEqualTo(t)
		}

		"construct with four arguments and give sane results" in {
			val alert = Alert('Alert, "Alert", "Description", "<span>Caution Message</span>", CautionAlert,
				align_center, "Alignment!", "", Some(Instant.ofEpochMilli(0L)))
			alert.message.toString must beEqualTo("<span>Caution Message</span>")
			alert.alertKind must beEqualTo(CautionAlert)
			alert.icon must beEqualTo(align_center)
			alert.cssClass must beEqualTo("")
			alert.prefix must beEqualTo("Alignment!")
		}

		"produce correct icon html" in {
			val alert = new Alert('A, "A", "Description", "<span>Html Message</span>", NoteAlert)
			alert.icon.toString must beEqualTo("<i class=\"icon-info\"></i>")
		}

		"save to and fetch from the DB" in {
      testScrupal.withExecutionContext { implicit ec: ExecutionContext ⇒
        val future = {
          super.ensureSchema(ApiSchemaDesign()) { case schema: Schema =>
            schema.withCollection("alerts") { alerts: Collection[Alert] ⇒
              alerts.deleteAll().flatMap { result ⇒
                val a1 = new Alert('foo, "Alert", "Description", "Message", WarningAlert)
                alerts.insert(a1) flatMap { wr =>
                  alerts.fetch(a1.getPrimaryId()) map { optAlert =>
                    val a2 = optAlert.get
                    val saved_time = a2.expires
                    a2.getPrimaryId() must beEqualTo(a1.getPrimaryId())
                  }
                }
              }
            }
          } flatMap { x ⇒ x }
        }
        Await.result(future, 5.seconds)
      }
		}
	}
}
