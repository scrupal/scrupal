/**********************************************************************************************************************
  * This file is part of Scrupal a Web Application Framework.                                                          *
  *                                                                                                                    *
  * Copyright (c) 2014, Reid Spencer and viritude llc. All Rights Reserved.                                            *
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


import play.twirl.sbt.Import.TwirlKeys
import sbt._
import sbt.Keys._

/**
 * Settings for building Scrupal. These are common settings for each sub-project.
 * Only put things in here that must be identical for each sub-project. Otherwise,
 * Specialize below in the definition of each Project object.
 */
trait BuildSettings
{
  val buildSettings : Seq[Def.Setting[_]] = Seq (
    // credentials += Credentials(Path.userHome / ".ivy2" / ".credentials"),
    // publishTo := Some(Resolvers.MyArtifactHost),
    organization    := "scrupal.org",
    scalaVersion    := "2.11.2",
    javacOptions ++= Seq(
      "-encoding", "utf8",
      "-g",
      // "-J-Xmx1024m",
	    "-Xlint"
    ),
    javacOptions in doc ++= Seq ("-source", "1.7"),
    scalacOptions   ++= Seq(
      "-J-Xss8m",
      "-feature",
      "-Xlint",
      "-unchecked",
      "-deprecation",
      "-language:implicitConversions",
      "-language:postfixOps",
      "-language:reflectiveCalls",
      "-encoding", "utf8",
      "-Ywarn-adapted-args"
    ),
    sourceDirectories in Compile := Seq(baseDirectory.value / "src"),
    sourceDirectories in Test := Seq(baseDirectory.value / "test"),
    unmanagedSourceDirectories in Compile := Seq(baseDirectory.value / "src"),
    unmanagedSourceDirectories in Test := Seq(baseDirectory.value / "test"),
    scalaSource in Compile := baseDirectory.value / "src",
    scalaSource in Test := baseDirectory.value / "test",
    javaSource in Compile := baseDirectory.value / "src",
    javaSource in Test := baseDirectory.value / "test",
    resourceDirectory in Compile := baseDirectory.value / "src/resources",
    resourceDirectory in Test := baseDirectory.value / "test/resources",
    fork in Test  := false,
    parallelExecution in Test := false,
    logBuffered in Test := false,
    shellPrompt     := BuildCommands.buildShellPrompt,
    version         := BuildInfo.buildVersion,
    BuildCommands.printClasspath <<= BuildCommands.print_class_path
  )

  val twirlSettings : Seq[Def.Setting[_]] = Seq (
    sourceDirectories in (Compile, TwirlKeys.compileTemplates) := (unmanagedSourceDirectories in Compile).value,
    TwirlKeys.templateFormats += ("html" -> "play.twirl.api.HtmlFormat")
  )
}
