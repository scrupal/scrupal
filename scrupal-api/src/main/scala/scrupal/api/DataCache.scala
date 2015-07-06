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

import play.api.libs.json.{JsError, JsSuccess, Json}
import scrupal.storage.api.{Collection, Schema}
import scrupal.utils.ScrupalComponent

import scala.concurrent.{Future, ExecutionContext}
import scala.util.{Failure, Success, Try}

abstract class DataCache extends ScrupalComponent {

  def update(scrupal : Scrupal, schema : Schema)
}

object DataCache extends DataCache {

  private var _themes : Map[String,Theme] = emptyThemeInfo

  def themes : Map[String,Theme] = _themes

  private var _sites = Seq.empty[String]
  def sites : Seq[String] = _sites

  private var _alerts = Seq.empty[Alert]
  def alerts : Seq[Alert] = _alerts

  def update(scrupal : Scrupal, schema : Schema) : Unit = {
    this.synchronized {
      updateThemeInfo
      scrupal.withExecutionContext { implicit ec: ExecutionContext ⇒
        val f1 = {
          schema.withCollection("alerts") { alertsColl: Collection[Alert] ⇒
            alertsColl.fetchAll().map { alerts ⇒
              val unexpired = for (a ← alerts if a.unexpired) yield {a}
              _alerts = unexpired.toSeq
            }
          }
        }
        val f2 = Future {
          _sites = scrupal.Sites.values.map { site ⇒ site.name }
        }
        Future sequence Seq(f1, f2)
      }
    }
  }

  def updateThemeInfo() : Unit = {
    _themes = getThemeInfo
  }

  case class Theme(name: String, description: String, thumbnail: String, preview: String, css: String,
    `css-min`: String)
  implicit val ThemeReads = Json.reads[Theme]
  case class ThemeInfo(version: String, themes: List[Theme])
  implicit val ThemeInfoReads = Json.reads[ThemeInfo]

  lazy val emptyThemeInfo = Map.empty[String,Theme]

  def getThemeInfo : Map[String,Theme] = Try[Map[String,Theme]] {
    val theme_path = "META-INF/resources/webjars/bootswatch/3.3.1+2/2/api/themes.json"
    val loader = this.getClass.getClassLoader
    Option(loader.getResourceAsStream(theme_path)) match {
      case Some(stream) ⇒ {
        val jsval = Json.parse(stream)
        ThemeInfoReads.reads(jsval) match {
          case JsSuccess(value, path) ⇒ value.themes.map { t ⇒ t.name → t }.toMap
          case JsError(errors) ⇒
            log.warn(s"Failed to parse Bootswatch themes Json: ${JsError.toJson(errors)}")
            emptyThemeInfo
          case _ ⇒
            log.warn(s"Failed to parse Bootswatch themes Json: mismatched result")
            emptyThemeInfo
        }
      }
      case None ⇒
        log.warn(s"Failed to find Bootswatch themes resource at $theme_path")
        emptyThemeInfo
    }
  } match {
    case Success(x) ⇒ x
    case Failure(x) ⇒
      log.warn("Failed to acquire Bootswatch themes:", x)
      emptyThemeInfo
  }
}
