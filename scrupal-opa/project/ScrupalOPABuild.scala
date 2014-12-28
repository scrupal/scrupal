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
import org.scalajs.sbtplugin.ScalaJSPlugin
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._
import sbt.Keys._
import sbt._

object ScrupalOPABuild extends Build with BuildSettings with AssetsSettings with Dependencies {

  import sbtunidoc.{Plugin ⇒ UnidocPlugin}
  import spray.revolver.RevolverPlugin._

  val base_name = BuildInfo.projectName

  lazy val opa_proj = Project(base_name , file("."))
    .enablePlugins(SbtWeb)
    .enablePlugins(ScalaJSPlugin)
    .settings(UnidocPlugin.unidocSettings: _*)
    .settings(Revolver.settings:_*)
    .settings(buildSettings:_*)
    .settings(resolver_settings:_*)
    .settings(docSettings:_*)
    .settings(opa_sbt_web_settings:_*)
    .settings(opa_pipeline_settings:_*)
    .settings(less_settings:_*)
    .settings(libraryDependencies ++= opa_dependencies)
    .settings(libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "0.7.0")
    .settings(libraryDependencies += "com.greencatsoft" %%% "scalajs-angular" % "0.3-SNAPSHOT")
    .settings(
      mainClass in (Compile, run) := Some("scrupal.core.Boot"),
      emitSourceMaps := true,
      persistLauncher := true,
      persistLauncher in (Test) := false,
      jsDependencies += RuntimeDOM,
      artifactPath in (Compile, packageScalaJSLauncher) := (
        target.value / "web" / "classes" / "main" / "lib" / "scrupal" / "scrupal-opa-launcher.js"),
      artifactPath in (Compile, fullOptJS) := (
        target.value / "web" / "classes" / "main" / "lib" / "scrupal" / "scrupal-opa-fullOpt.js"),
      artifactPath in (Compile, fastOptJS) := (
        target.value / "web" / "classes" / "main" / "lib" / "scrupal" / "scrupal-opa-fastOpt.js")
    )
  val opa_deps = opa_proj % "compile->compile;test->test"

  override def rootProject = Some(opa_proj)
}
