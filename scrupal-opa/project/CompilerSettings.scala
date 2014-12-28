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

import sbt.Def
import sbt.Keys._

trait CompilerSettings {

  lazy val compilerSettings : Seq[Def.Setting[_]] = Seq (
    // credentials += Credentials(Path.userHome / ".ivy2" / ".credentials"),
    // publishTo := Some(Resolvers.MyArtifactHost),
    scalaVersion    := "2.11.4",
    javacOptions ++= Seq(
      "-encoding", "utf8",
      "-g",
      // "-J-Xmx1024m",
      "-Xlint"
    ),
    javacOptions in doc ++= Seq ("-source", "1.7"),
    scalacOptions   ++= Seq(
      "-J-Xss8m",
      "-J-Xmx1024m",
      "-feature",
      "-Xlint",
      "-unchecked",
      "-deprecation",
      "-language:implicitConversions",
      "-language:postfixOps",
      "-language:reflectiveCalls",
      "-encoding", "utf8",
      "-Ywarn-adapted-args",
      "-target:jvm-1.7"
    )
  )
}
