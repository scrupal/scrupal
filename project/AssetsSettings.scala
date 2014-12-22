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
import sbt._
import sbt.Keys._

import com.typesafe.sbt.less.Import.LessKeys
import com.typesafe.sbt.web.Import._

import scala.language.postfixOps


/** Settings for the Asset pipeline tools
 */
trait AssetsSettings {

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

  lazy val core_pipeline_settings = general_pipeline_settings ++ Seq[Setting[_]](
    pipelineStages := Seq(digest, gzip)
  )

  lazy val opa_pipeline_settings = general_pipeline_settings ++ Seq[Setting[_]](
    pipelineStages := Seq(rjs, digest, gzip)
  )

  lazy val opa_sbt_web_settings = sbt_web_settings ++ Seq(
    managedResources in Assets += crossTarget.value / "scrupal-opa-opt.js",
    fileMappings in Assets := (crossTarget.value / "scrupal-opa-opt.js" get).map {
      x => x -> target.value  / "web" / "classes" / "main" / "lib" / "scrupal" / "scrupal-opa.js"
    }
  )

  lazy val less_settings = Seq[Setting[_]](
    includeFilter in (Assets, LessKeys.less) := "*.less",
    excludeFilter in (Assets, LessKeys.less) := "_*.less",
    LessKeys.cleancss	in Assets := false, // Compress output using clean-css.
    // LessKeys.cleancssOptions	in Assets := "", // Pass an option to clean css, using CLI arguments from https://github.com/GoalSmashers/clean-css .
    LessKeys.color in Assets := true, // Whether LESS output should be colorised
    LessKeys.compress in Assets := true,
    // LessKeys.globalVariables	Variables that will be placed at the top of the less file.
    LessKeys.ieCompat in Assets := true, // Do IE compatibility checks.
    LessKeys.insecure in Assets := false, 	// Allow imports from insecure https hosts.
    LessKeys.maxLineLen	in Assets := 1024, // Maximum line length.
    // LessKeys.optimization	in Assets := // Set the parser's optimization level.
    // relativeImports	Re-write import paths relative to the base less file. Default is true.
    // relativeUrls	Re-write relative urls to the base less file.
    // rootpath	Set rootpath for url rewriting in relative imports and urls.
    // silent	Suppress output of error messages.
    LessKeys.sourceMap in Assets := true, //	Outputs a v3 sourcemap.
    LessKeys.sourceMapFileInline in Assets := false,  //	Whether the source map should be embedded in the output file
    // sourceMapLessInline	Whether to embed the less code in the source map
    // sourceMapRootpath	Adds this path onto the sourcemap filename and less file paths.
    // strictImports	Whether imports should be strict.
    // strictMath	Requires brackets. This option may default to true and be removed in future.
    // strictUnits	Whether all unit should be strict, or if mixed units are allowed.
    LessKeys.verbose in Assets := true // Be verbose.
  )
}
