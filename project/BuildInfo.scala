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
import java.io.File

import scala.language.postfixOps

import com.typesafe.config.ConfigFactory
import sbt._

/** Build Information
 * Capture basic information about the build that is configured in the project/build_info.conf file
 */
object BuildInfo {
  val project_conf = new File("project/project.conf")
  val conf = ConfigFactory.parseFile(project_conf).resolve()
  val buildNumber = conf.getInt("build.number")
  val buildIdentifier = conf.getString("build.id")
  val buildUrl = conf.getString("build.url")
  val projectName = conf.getString("project.name")
  val projectBaseVersion = conf.getString("project.version")
  val projectVersion = if (buildNumber==0) projectBaseVersion + "-SNAPSHOT" else projectBaseVersion

  object devnull extends ProcessLogger {
    def info (s: => String) {}
    def error (s: => String) { }
    def buffer[T] (f: => T): T = f
  }

  val currentBranchPattern = """\*\s+([^\s]+)""".r

  def gitBranches = "git branch --no-color" lines_! devnull mkString

  def currBranch = ("git status -sb" lines_! devnull headOption) getOrElse "-" stripPrefix "## "

  def currentGitBranch = currentBranchPattern findFirstMatchIn gitBranches map (_ group 1) getOrElse "-"

  def currentProject(state: State) = Project.extract (state).currentProject.id

}
