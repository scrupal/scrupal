/**********************************************************************************************************************
 * This file is part of Scrupal, a Scalable Reactive Content Management System.                                       *
 *                                                                                                                    *
 * Copyright © 2015 Reactific Software LLC                                                                            *
 *                                                                                                                    *
 * Licensed under the Apache License, Version 2.0 (the "License");  you may not use this file                         *
 * except in compliance with the License. You may obtain a copy of the License at                                     *
 *                                                                                                                    *
 *        http://www.apache.org/licenses/LICENSE-2.0                                                                  *
 *                                                                                                                    *
 * Unless required by applicable law or agreed to in writing, software distributed under the                          *
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,                          *
 * either express or implied. See the License for the specific language governing permissions                         *
 * and limitations under the License.                                                                                 *
 **********************************************************************************************************************/

import play.sbt.PlayLayoutPlugin
import sbt._
import sbt.Keys._

import scrupal.sbt.ScrupalPlugin

object ScrupalBuild extends Build with AssetsSettings with Dependencies {

  val base_name = "scrupal"

  import scrupal.sbt.ScrupalPlugin.autoImport._

  val buildSettings : Seq[Def.Setting[_]] =  Seq (
    // credentials += Credentials(Path.userHome / ".ivy2" / ".credentials"),
    // publishTo := Some(Resolvers.MyArtifactHost),
    organization    := "org.scrupal",
    version         := "0.2.0-SNAPSHOT",
    //scalacOptions   += "-Xlog-implicits",
    scrupalCopyrightHolder := "Reactific Software LLC",
    scrupalCopyrightYears := Seq(2013,2014,2015),
    scrupalDeveloperUrl := url("http://reactific.com/")
  )

  lazy val utils_proj = Project(base_name + "-utils", file("./scrupal-utils"))
    .settings(buildSettings:_*)
    .settings(
      scrupalTitle := "Scrupal Utils",
      resolvers ++= all_resolvers,
      libraryDependencies ++= utils_dependencies
    )
    .enablePlugins(ScrupalPlugin).disablePlugins(PlayLayoutPlugin)
  lazy val utils_deps = utils_proj % "compile->compile;test->test"

  lazy val storage_proj = Project(base_name + "-storage", file("./scrupal-storage"))
    .settings(buildSettings:_*)
    .settings(
      scrupalTitle := "Scrupal Storage",
      resolvers ++= all_resolvers,
      libraryDependencies ++= storage_dependencies
    )
    .enablePlugins(ScrupalPlugin).disablePlugins(PlayLayoutPlugin)
    .dependsOn(utils_deps)
  lazy val storage_deps = storage_proj % "compile->compile;test->test"

  lazy val api_proj = Project(base_name + "-api", file("./scrupal-api"))
    .settings(buildSettings:_*)
    .settings(
      scrupalTitle := "Scrupal API",
      resolvers ++= all_resolvers,
      libraryDependencies ++= api_dependencies
    )
    .enablePlugins(ScrupalPlugin).disablePlugins(PlayLayoutPlugin)
    .dependsOn(storage_deps, utils_deps)
  lazy val api_deps = api_proj % "compile->compile;test->test"

  lazy val filesys_proj = Project(base_name + "-store-filesys", file("./scrupal-store-filesys"))
    .settings(buildSettings:_*)
    .settings(
      scrupalTitle := "Scrupal FileSystem Store",
      resolvers ++= all_resolvers,
      libraryDependencies ++= filesys_dependencies
    )
    .enablePlugins(ScrupalPlugin).disablePlugins(PlayLayoutPlugin)
    .dependsOn(storage_deps, utils_deps)
  lazy val filesys_deps = filesys_proj % "compile->compile;test->test"

  lazy val core_proj = Project(base_name + "-core", file("./scrupal-core"))
    .settings(buildSettings:_*)
    // .settings(sbt_web_settings:_*)
    // .settings(less_settings:_*)
    // .settings(core_pipeline_settings:_*)
    .settings(
      scrupalTitle := "Scrupal Core",
      mainClass in (Compile, run) := Some("scrupal.core.Boot"),
      resolvers ++= all_resolvers,
      libraryDependencies ++= core_dependencies
    )
    .enablePlugins(ScrupalPlugin).disablePlugins(PlayLayoutPlugin)
    .dependsOn(api_deps, storage_deps, utils_deps)
  lazy val core_deps = core_proj % "compile->compile;test->test"

  lazy val config_proj = Project(base_name + "-config", file("./scrupal-config"))
    .settings(buildSettings:_*)
    .settings(
      scrupalTitle := "Scrupal Configuration",
      resolvers ++= all_resolvers,
      libraryDependencies ++= core_dependencies)
    .enablePlugins(ScrupalPlugin).disablePlugins(PlayLayoutPlugin)
    .dependsOn(utils_deps, core_deps)
  lazy val config_deps = config_proj % "compile->compile;test->test"

  lazy val root = Project(base_name, file("."))
    .settings(buildSettings:_*)
    .settings(
      scrupalTitle := "Scrupal Root",
      resolvers ++= all_resolvers,
      libraryDependencies ++= root_dependencies
    )
    .enablePlugins(ScrupalPlugin)
    .dependsOn(config_deps, core_deps, api_deps, filesys_deps, storage_deps, utils_deps)
    .aggregate(config_proj, core_proj, api_proj, filesys_proj, storage_proj, utils_proj)

  override def rootProject = Some(root)
}
