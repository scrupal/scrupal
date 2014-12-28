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
import sbt.Keys._
import sbt._
import sbt.complete.Parsers._

import scala.language.postfixOps

/**
 * Augment the Play shell prompt with the Shell prompt which show the current project,
 * git branch and build version
 */
object BuildCommands
{
  addCommandAlias("tq", "test-quick")
  addCommandAlias("tm", "test-only scrupal.models")
  addCommandAlias("tu", "test-only scrupal.utils")
  addCommandAlias("tc", "test-only scrupal.controllers")

  val printClasspath = TaskKey[File]("print-class-path", "Print the project's class path line by line.")

  val printRuntimeClasspath = TaskKey[File]("print-runtime-class-path", "Print the project's runtime class path.")

  def print_class_path = (target, fullClasspath in Compile, compile in Compile) map { (out, cp, analysis) =>
    println("----- " + out.getCanonicalPath + ": FILES:")
    println(cp.files.map(_.getCanonicalPath).mkString("\n"))
    println("----- " + out.getCanonicalPath + ": All Binary Dependencies:")
    println(analysis.relations.allBinaryDeps.toSeq.mkString("\n"))
    println("----- END")
    out
  }

  def print_runtime_class_path = (target, fullClasspath in Runtime).map { (out, cp) =>
    println("----- " + out.getCanonicalPath + ": FILES:")
    println(cp.files.map(_.getCanonicalPath).mkString("\n"))
    println("----- END")
    out
  }


  val buildShellPrompt = {
    (state: State) => {
      "%s - %s - %s> ".format (BuildInfo.currentProject(state), BuildInfo.currentGitBranch, BuildInfo.projectVersion)
    }
  }

  // FIXME: Trying to do something like "testOnly"
  // SEE: https://github.com/sbt/sbt/blob/0.13/main/src/main/scala/sbt/Defaults.scala

  val compileOnly = inputKey[Unit]("Compile just the specified files")

  compileOnly := {
    val args: Seq[String] = spaceDelimited("<arg>").parsed
    args foreach println
  }
}
