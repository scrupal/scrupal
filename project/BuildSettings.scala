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
import play.twirl.sbt.Import.TwirlKeys._
import sbt._
import sbt.Keys._

/**
 * Settings for building Scrupal. These are common settings for each sub-project.
 * Only put things in here that must be identical for each sub-project. Otherwise,
 * Specialize below in the definition of each Project object.
 */
trait BuildSettings extends CompilerSettings
{
  val buildSettings : Seq[Def.Setting[_]] = compilerSettings ++ Seq (
    // credentials += Credentials(Path.userHome / ".ivy2" / ".credentials"),
    // publishTo := Some(Resolvers.MyArtifactHost),
    organization    := "scrupal.org",
    version         := BuildInfo.projectVersion,

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
    ivyScala := ivyScala.value map { _.copy(overrideScalaVersion = true) },
    BuildCommands.printClasspath <<= BuildCommands.print_class_path
  )

  val docSettings : Seq[Def.Setting[_]] = Seq(
    apiURL := Some(url("http://scrupal.org/api/")),
    scalacOptions in (Compile, doc) ++= Seq("-unchecked", "-deprecation", "-implicits"),
    scalacOptions in (Compile, doc) ++= Opts.doc.title("Scrupal API"),
    scalacOptions in (Compile, doc) ++= Opts.doc.version(BuildInfo.projectVersion),
    autoAPIMappings := true,
    apiMappings ++= {
      val cp: Seq[Attributed[File]] = (fullClasspath in Compile).value
      def findManagedDependency(organization: String, name: String): File = {
        ( for {
          entry <- cp
          module <- entry.get(moduleID.key)
          if module.organization == organization
          if module.name.startsWith(name)
          jarFile = entry.data
        } yield jarFile
          ).head
      }
      Map(
        findManagedDependency("org.reactivemongo",  "reactivemongo") → url("http://reactivemongo.org/releases/0.10.5/api/"),
        findManagedDependency("org.scala-lang", "scala-library") → url(s"http://www.scala-lang.org/api/$scalaVersion/"),
        findManagedDependency("com.typesafe.akka", "akka-actor") → url(s"http://doc.akka.io/api/akka/"),
        findManagedDependency("com.typesafe", "config") → url("http://typesafehub.github.io/config/latest/api/")
      )
    }
  )

}
