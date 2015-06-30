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

import com.typesafe.sbt.digest.Import._
import com.typesafe.sbt.gzip.Import._
import com.typesafe.sbt.rjs.Import._
import com.typesafe.sbt.less.Import.LessKeys
import com.typesafe.sbt.web.Import._

import sbt._
import sbt.Keys._

import scala.language.postfixOps

/** Settings for the Asset pipeline tools */
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
