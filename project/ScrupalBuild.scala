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

import com.typesafe.sbt.digest.Import.digest
import com.typesafe.sbt.gzip.Import.gzip
import com.typesafe.sbt.web.SbtWeb
import com.typesafe.sbt.web.SbtWeb.autoImport._
import play.routes.compiler.InjectedRoutesGenerator
import play.sbt.PlayLayoutPlugin
import play.sbt.routes.RoutesKeys._
import sbt.Keys._
import sbt._
import sbtbuildinfo.{BuildInfoKeys, BuildInfoPlugin}
import scrupal.sbt.ScrupalPlugin
import scrupal.sbt.ScrupalPlugin.autoImport._

object ScrupalBuild extends Build with AssetsSettings with Dependencies {

  val base_name = "scrupal"
  val buildSettings: Seq[Def.Setting[_]] = Seq(
    // credentials += Credentials(Path.userHome / ".ivy2" / ".credentials"),
    // publishTo := Some(Resolvers.MyArtifactHost),
    organization := "org.scrupal",
    version := "0.2.0-SNAPSHOT",
    maxErrors := 50,
    routesGenerator := InjectedRoutesGenerator,
    namespaceReverseRouter := true,
    // pipelineStages in Assets := Seq(concat),
    pipelineStages := Seq(digest, gzip),
    // RjsKeys.baseUrl := "javascripts",
    // RjsKeys.paths := Map.empty,
    //scalacOptions   += "-Xlog-implicits",
    resolvers ++= all_resolvers,
    scrupalCopyrightHolder := "Reactific Software LLC",
    scrupalCopyrightYears := Seq(2013, 2014, 2015),
    scrupalDeveloperUrl := url("http://reactific.com/")
  )

  lazy val utils_proj = Project(base_name + "-utils", file("./scrupal-utils"))
    .enablePlugins(ScrupalPlugin, BuildInfoPlugin)
    .disablePlugins(PlayLayoutPlugin)
    .settings(buildSettings: _*)
    .settings(
      scrupalTitle := "Scrupal Utils",
      scrupalPackage := "scrupal.utils",
      BuildInfoKeys.buildInfoKeys += ( "themes" → bootswatch_theme_names),
      libraryDependencies ++= utils_dependencies
    )

  lazy val utils_deps = utils_proj % "compile->compile;test->test"
  lazy val storage_proj = Project(base_name + "-storage", file("./scrupal-storage"))
    .enablePlugins(ScrupalPlugin)
    .disablePlugins(PlayLayoutPlugin)
    .settings(buildSettings:_*)
    .settings(
      scrupalTitle := "Scrupal Storage",
      scrupalPackage := "scrupal.storage",
      libraryDependencies ++= storage_dependencies
    )
    .dependsOn(utils_deps)
  lazy val storage_deps = storage_proj % "compile->compile;test->test"
  lazy val api_proj = Project(base_name + "-api", file("./scrupal-api"))
    .enablePlugins(ScrupalPlugin)
    .disablePlugins(PlayLayoutPlugin)
    .settings(buildSettings:_*)
    .settings(
      scrupalTitle := "Scrupal API",
      scrupalPackage := "scrupal.api",
      libraryDependencies ++= api_dependencies
    )
    .dependsOn(utils_deps, storage_deps)
  lazy val api_deps = api_proj % "compile->compile;test->test"
  lazy val store_reactivemongo_proj = Project(base_name + "-store-reactivemongo", file("./scrupal-store-reactivemongo"))
    .enablePlugins(ScrupalPlugin)
    .disablePlugins(PlayLayoutPlugin)
    .settings(buildSettings:_*)
    .settings(
      scrupalTitle := "Scrupal Store For ReactiveMongo",
      scrupalPackage := "scrupal.store.reactivemongo",
      libraryDependencies ++= store_reactivemongo_dependencies
    )
    .dependsOn(utils_deps, storage_deps, api_deps)
  lazy val store_reactivemongo_deps = store_reactivemongo_proj % "compile->compile;test->test"
  lazy val store_rxmongo_proj = Project(base_name + "-store-rxmongo", file("./scrupal-store-rxmongo"))
    .enablePlugins(ScrupalPlugin)
    .disablePlugins(PlayLayoutPlugin)
    .settings(buildSettings:_*)
    .settings(
      scrupalTitle := "Scrupal Store For RxMongo",
      scrupalPackage := "scrupal.store.rxmongo",
      libraryDependencies ++= store_rxmongo_dependencies
    )
    .dependsOn(utils_deps, storage_deps, api_deps)
  lazy val store_rxmongo_deps = store_rxmongo_proj % "compile->compile;test->test"
  lazy val core_proj = Project(base_name + "-core", file("./scrupal-core"))
    .enablePlugins(ScrupalPlugin, SbtWeb)
    .disablePlugins(PlayLayoutPlugin)
    .settings(buildSettings:_*)
    .settings(sbt_web_settings:_*)
    .settings(less_settings:_*)
    .settings(core_pipeline_settings:_*)
    .settings(
      scrupalTitle := "Scrupal Core",
      scrupalPackage := "scrupal.core",
      libraryDependencies ++= core_dependencies
    )
    .dependsOn(storage_deps, api_deps, utils_deps)
  lazy val core_deps = core_proj % "compile->compile;test->test"
  lazy val admin_proj = Project(base_name + "-admin", file("./scrupal-admin"))
    .enablePlugins(ScrupalPlugin)
    .disablePlugins(PlayLayoutPlugin)
    .settings(buildSettings:_*)
    .settings(
      scrupalTitle := "Scrupal Administration Module",
      scrupalPackage := "scrupal.admin",
      libraryDependencies ++= admin_dependencies)
    .dependsOn(utils_deps, api_deps, core_deps, storage_deps)
  lazy val admin_deps = admin_proj % "compile->compile;test->test"
  lazy val config_proj = Project(base_name + "-config", file("./scrupal-config"))
    .enablePlugins(ScrupalPlugin)
    .disablePlugins(PlayLayoutPlugin)
    .settings(buildSettings:_*)
    .settings(
      scrupalTitle := "Scrupal Configuration Module",
      scrupalPackage := "scrupal.config",
      libraryDependencies ++= config_dependencies)
    .dependsOn(utils_deps, core_deps)
  lazy val config_deps = config_proj % "compile->compile;test->test"
  lazy val doc_proj = Project(base_name + "-doc", file("./scrupal-doc"))
    .enablePlugins(ScrupalPlugin)
    .disablePlugins(PlayLayoutPlugin)
    .settings(buildSettings:_*)
    .settings(
      scrupalTitle := "Scrupal Documentation Module",
      scrupalPackage := "scrupal.doc",
      resolvers ++= all_resolvers,
      libraryDependencies ++= doc_dependencies)
    .dependsOn(utils_deps, api_deps, core_deps, storage_deps)
  lazy val doc_deps = doc_proj % "compile->compile;test->test"
  lazy val root = Project(base_name, file("."))
    .enablePlugins(ScrupalPlugin, SbtWeb)
    .settings(buildSettings:_*)
    .settings(
      scrupalTitle := "Scrupal",
      scrupalPackage := "scrupal",
      aggregateReverseRoutes := Seq(core_proj, config_proj, admin_proj, doc_proj),
      libraryDependencies ++= root_dependencies
    )
    .dependsOn(admin_proj, config_deps, doc_deps, core_deps, store_rxmongo_deps, api_deps, storage_deps, utils_deps)
    .aggregate(admin_proj, config_proj, doc_proj, core_proj, store_rxmongo_proj, api_proj, storage_proj, utils_proj)

  override def rootProject = Some(root)
}
