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
import sbt._
import sbt.Keys._

object ScrupalBuild extends Build with BuildSettings with AssetsSettings with Dependencies {

  import sbtunidoc.{ Plugin => UnidocPlugin }
  import spray.revolver.RevolverPlugin._

  val base_name = BuildInfo.projectName

  lazy val utils_proj = Project(base_name + "-utils", file("./scrupal-utils"))
    .settings(buildSettings:_*)
    .settings(resolver_settings:_*)
    .settings(libraryDependencies ++= utils_dependencies)
  lazy val utils_deps = utils_proj % "compile->compile;test->test"

  lazy val db_proj = Project(base_name + "-db", file("./scrupal-db"))
    .settings(buildSettings:_*)
    .settings(resolver_settings:_*)
    .settings(libraryDependencies ++= db_dependencies)
    .dependsOn(utils_deps)
  lazy val db_deps = db_proj % "compile->compile;test->test"

  lazy val core_proj = Project(base_name + "-core", file("./scrupal-core"))
    .enablePlugins(SbtWeb)
    .settings(buildSettings:_*)
    .settings(resolver_settings:_*)
    .settings(sbt_web_settings:_*)
    .settings(less_settings:_*)
    .settings(core_pipeline_settings:_*)
    .settings(
      libraryDependencies ++= core_dependencies
    )
    .dependsOn(utils_deps, db_deps)
  lazy val core_deps = core_proj % "compile->compile;test->test"

  lazy val config_proj = Project(base_name + "-config", file("./scrupal-config"))
    .settings(buildSettings:_*)
    .settings(resolver_settings:_*)
    .settings(libraryDependencies ++= core_dependencies)
    .dependsOn(utils_deps, db_deps, core_deps)
  lazy val config_deps = config_proj % "compile->compile;test->test"

  lazy val root = Project(base_name, file("."))
    .settings(buildSettings:_*)
    .settings(resolver_settings:_*)
    .settings(Revolver.settings:_*)
    .settings(docSettings:_*)
    .settings(
      mainClass in (Compile, run) := Some("scrupal.core.Boot"),
      libraryDependencies ++= root_dependencies
    )
    .settings(UnidocPlugin.unidocSettings: _*)
    .dependsOn(config_deps, core_deps, db_deps, utils_deps)
    .aggregate(config_proj, core_proj, db_proj, utils_proj)

  override def rootProject = Some(root)
}
