/**********************************************************************************************************************
 * Copyright © 2014 Reactific Software LLC                                                                            *
 *                                                                                                                    *
 * This file is part of Scrupal, an Opinionated Web Application Framework.                                            *
 *                                                                                                                    *
 * Scrupal is free software: you can redistribute it and/or modify it under the terms                                 *
 * of the GNU General Public License as published by the Free Software Foundation,                                    *
 * either version 3 of the License, or (at your option) any later version.                                            *
 *                                                                                                                    *
 * Scrupal is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;                               *
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                          *
 * See the GNU General Public License for more details.                                                               *
 *                                                                                                                    *
 * You should have received a copy of the GNU General Public License along with Scrupal.                              *
 * If not, see either: http://www.gnu.org/licenses or http://opensource.org/licenses/GPL-3.0.                         *
 **********************************************************************************************************************/

import com.typesafe.sbt.web.SbtWeb
import play.twirl.sbt.SbtTwirl
import sbt._
import sbt.Keys._

object ScrupalBuild extends Build
  with BuildSettings with AssetsSettings with TwirlSettings with SbtWebSettings with LessSettings with Dependencies {

  import sbtunidoc.{ Plugin => UnidocPlugin }
  import spray.revolver.RevolverPlugin._

  val base_name = BuildInfo.projectName

  lazy val utils = Project(base_name + "-utils", file("./scrupal-utils"))
    .settings(buildSettings:_*)
    .settings(resolver_settings:_*)
    .settings(Revolver.settings:_*)
    .settings(libraryDependencies ++= utils_dependencies)
  lazy val utils_deps = utils % "compile->compile;test->test"

  lazy val db = Project(base_name + "-db", file("./scrupal-db"))
    .settings(buildSettings:_*)
    .settings(resolver_settings:_*)
    .settings(libraryDependencies ++= db_dependencies)
    .dependsOn(utils_deps)
  lazy val db_deps = db % "compile->compile;test->test"

  lazy val core = Project(base_name + "-core", file("./scrupal-core"))
    .enablePlugins(SbtTwirl)
    .enablePlugins(SbtWeb)
    .settings(buildSettings:_*)
    .settings(resolver_settings:_*)
    .settings(twirlSettings_core:_*)
    .settings(Revolver.settings:_*)
    .settings(sbt_web_settings:_*)
    .settings(core_pipeline_settings:_*)
    .settings(less_settings:_*)
    .settings(libraryDependencies ++= core_dependencies)
    .dependsOn(utils_deps, db_deps)
  lazy val core_deps = core % "compile->compile;test->test"

  lazy val http = Project(base_name + "-http", file("./scrupal-http"))
    .enablePlugins(SbtTwirl)
    .settings(buildSettings:_*)
    .settings(resolver_settings:_*)
    .settings(twirlSettings_http:_*)
    .settings(Revolver.settings:_*)
    .settings(libraryDependencies ++= http_dependencies)
    .dependsOn(utils_deps, db_deps, core_deps)
  lazy val http_deps = http % "compile->compile;test->test"

  lazy val config_proj = Project(base_name + "-config", file("./scrupal-config"))
    .settings(buildSettings:_*)
    .settings(resolver_settings:_*)
    .settings(libraryDependencies ++= http_dependencies)
    .dependsOn(utils_deps, db_deps, core_deps, http_deps)
  lazy val config_deps = config_proj % "compile->compile;test->test"

  lazy val web = Project(base_name + "-web", file("./scrupal-web"))
    .enablePlugins(SbtTwirl)
    .enablePlugins(SbtWeb)
    .settings(buildSettings:_*)
    .settings(resolver_settings:_*)
    .settings(twirlSettings_web:_*)
    .settings(Revolver.settings:_*)
    .settings(sbt_web_settings:_*)
    .settings(web_pipeline_settings:_*)
    .settings(libraryDependencies ++= web_dependencies)
    .dependsOn(utils_deps, db_deps, core_deps, http_deps)
  val web_deps = web % "compile->compile;test->test"

  lazy val root = Project(base_name, file("."))
    .settings(buildSettings:_*)
    .settings(resolver_settings:_*)
    .settings(docSettings:_*)
    .settings(
      mainClass in (Compile, run) := Some("scrupal.http.Boot"),
      libraryDependencies ++= root_dependencies
    )
    .settings(UnidocPlugin.unidocSettings: _*)
    .dependsOn(config_deps, web_deps, http_deps, core_deps, db_deps, utils_deps)
    .aggregate(config_proj, web, http, core, db, utils)

  override def rootProject = Some(root)
}
