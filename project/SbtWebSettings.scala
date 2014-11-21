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

import com.typesafe.sbt.digest.Import._
import com.typesafe.sbt.gzip.Import._
import com.typesafe.sbt.rjs.Import._
import com.typesafe.sbt.web.Import._
import sbt._
import sbt.Keys._

/** Provides Sbt Settings for the sbt-web plugin  */
trait SbtWebSettings {

  lazy val sbt_web_settings = Seq[Setting[_]](
    unmanagedSourceDirectories in Assets := Seq(baseDirectory.value / "assets"),
    unmanagedSourceDirectories in TestAssets := Seq(baseDirectory.value / "test" / "assets"),
    moduleName in Assets := "scrupal"
  )

  lazy val general_pipeline_settings = Seq[Setting[_]](
    RjsKeys.appDir := (resourceManaged in rjs).value,
    RjsKeys.mainModule := "scrupal",
    DigestKeys.algorithms := Seq("md5")
  )

  lazy val api_pipeline_settings = general_pipeline_settings ++ Seq[Setting[_]](
    pipelineStages := Seq(digest, gzip)
  )


  lazy val core_pipeline_settings = general_pipeline_settings ++ Seq[Setting[_]](
    pipelineStages := Seq(digest, gzip)
  )

  lazy val web_pipeline_settings = general_pipeline_settings ++ Seq[Setting[_]](
    pipelineStages := Seq(rjs, digest, gzip)
  )
}
