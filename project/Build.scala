/**********************************************************************************************************************
 * This file is part of Scrupal a Web Application Framework.                                                          *
 *                                                                                                                    *
 * Copyright (c) 2013, Reid Spencer and viritude llc. All Rights Reserved.                                            *
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

import play.Play.autoImport._
import PlayKeys._
import play.PlayScala
import sbt._
import sbt.Keys._

// import com.typesafe.jse.sbt.JsEnginePlugin.JsEngineKeys
// import com.typesafe.jshint.sbt.JSHintPlugin
import com.typesafe.sbt.web.SbtWeb
import com.typesafe.sbt.less.Import.LessKeys
import com.typesafe.sbt.web.Import.Assets
import com.typesafe.sbt.web.Import.pipelineStages
import com.typesafe.sbt.rjs.Import.rjs
import com.typesafe.sbt.gzip.Import.gzip
import com.typesafe.sbt.digest.Import.digest

/**
 * Capture basic information about the build.
 */
object BuildInfo {
  val appName = "scrupal"

  import com.typesafe.config._
  val conf = ConfigFactory.parseFile(new File("conf/application.conf")).resolve()
  val buildVersion = conf.getString("app.version")
}

/**
 * Augment the Play shell prompt with the Shell prompt which show the current project,
 * git branch and build version
 */
object ShellPrompt
{
  object devnull extends ProcessLogger {
    def info (s: => String) {}
    def error (s: => String) { }
    def buffer[T] (f: => T): T = f
  }

  val current = """\*\s+([^\s]+)""".r

  def gitBranches = ("git branch --no-color" lines_! devnull mkString)

  val buildShellPrompt = {
    (state: State) => {
      val currBranch = current findFirstMatchIn gitBranches map (_ group(1)) getOrElse "-"
      val currProject = Project.extract (state).currentProject.id
      "%s:%s:%s> ".format (currBranch, currProject, BuildInfo.buildVersion)
    }
  }
}




/**
 * Settings for building Scrupal. These are common settings for each sub-project.
 * Only put things in here that must be identical for each sub-project. Otherwise,
 * Specialize below in the definition of each Project object.
 */
trait BuildSettings
{
  val buildSettings : Seq[Def.Setting[_]] = Seq (
    // credentials += Credentials(Path.userHome / ".ivy2" / ".credentials"),
    javacOptions ++= Seq(
      "-encoding", "utf8",
      "-g",
      "-J-Xmx1024m",
	    "-Xlint"
    ),
    organization    := "scrupal.org",
    // publishTo := Some(Resolvers.MyArtifactHost),
    scalacOptions   ++= Seq(
      "-J-Xss32m",
      "-feature",
      "-Xlint",
      "-unchecked",
      "-deprecation",
      // "-Xlog-implicits", //<-- turn on to debug 'diverging implicit expansion error'
      "-language:implicitConversions",
      "-language:postfixOps",
      "-language:reflectiveCalls",
      "-encoding", "utf8",
      "-Ywarn-adapted-args"
    ),
    //closureCompilerOptions ++= Seq("ecmascript5", "checkControlStructures", "checkTypes", "checkSymbols"),
    scalaVersion    := "2.11.2",
    shellPrompt     := ShellPrompt.buildShellPrompt,
    version         := BuildInfo.buildVersion
  )
}

trait AssetsSettings {
  val lessSettings : Seq[Def.Setting[_]] = Seq(
    includeFilter in (Assets, LessKeys.less) := "*.less",
    excludeFilter in (Assets, LessKeys.less) := "_*.less",
    LessKeys.cleancss	in Assets := true, // Compress output using clean-css.
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

trait Dependencies
{
  // val scrupal_org_releases    = "Scrupal.org Releases" at "http://scrupal.github.org/mvn/releases"
  val google_sedis            = "Google Sedis" at "http://pk11-scratch.googlecode.com/svn/trunk/"
  val typesafe_releases       = "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases/"
  val sonatype_releases       = "Sonatype Releases"  at "http://oss.sonatype.org/content/repositories/releases/"
  val sonatype_snapshots      = "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"

//val scala_lang              = "Scala Language" at "http://mvnrepository.com/artifact/org.scala-lang/"
//val sbt_plugin_releases     = Resolver.url("SBT Plugin Releases",url("http://repo.scala-sbt.org/scalasbt/sbt-plugin-releases"))(Resolver.ivyStylePatterns)
//val geolocation             = "geolocation repository" at "http://blabluble.github.com/modules/releases/"

  val all_resolvers : Seq[MavenRepository] = Seq ( typesafe_releases, sonatype_releases, sonatype_snapshots, google_sedis  )

  // Fundamentals
  val scala_arm               = "com.jsuereth"        %% "scala-arm"              % "1.3"
  val play_cache              = "com.typesafe.play"   %% "play-cache"             % "2.3.4"
  val play_filters            = "com.typesafe.play"   %% "filters-helpers"        % "2.3.4"
  val play_test               = "com.typesafe.play"   %% "play-test"              % "2.3.4"
  val play_docs               = "com.typesafe.play"   %% "play-docs"              % "2.3.4"
  val play_ws                 = "com.typesafe.play"   %% "play-ws"                % "2.3.4"

  // Databass, Caches, Data Storage stuff
  val play_plugins_redis      = "com.typesafe"        %% "play-plugins-redis"     % "2.1.1"
  val slick                   = "com.typesafe.slick"  %% "slick"                  % "2.1.0"
  val reactivemongo            ="org.reactivemongo"   %% "reactivemongo"          % "0.10.5.0.akka23"
  val play2_reactivemongo     = "org.reactivemongo"   %% "play2-reactivemongo"    % "0.10.5.0.akka23"
  val reactivemongo_ext       = "org.reactivemongo"   %% "reactivemongo-extensions-json"  % "0.10.5.akka23-SNAPSHOT"

  // WebJars based UI components
  val webjars_play            = "org.webjars"         %% "webjars-play"           % "2.3.0"
  val requirejs               = "org.webjars"         % "requirejs"               % "2.1.14-3"
  val requirejs_domready      = "org.webjars"         % "requirejs-domready"      % "2.0.1"
  val angularjs               = "org.webjars"         % "angularjs"               % "1.3.0"
  val angular_drag_drop       = "org.webjars"         % "angular-dragdrop"        % "1.0.3"
  val angular_multi_select    = "org.webjars"         % "angular-multi-select"    % "2.0.1"
  val angular_ui              = "org.webjars"         % "angular-ui"              % "0.4.0-3"
  val angular_ui_bootstrap    = "org.webjars"         % "angular-ui-bootstrap"    % "0.11.2"
  val angular_ui_router       = "org.webjars"         % "angular-ui-router"       % "0.2.11-1"
  val angular_ui_utils        = "org.webjars"         % "angular-ui-utils"        % "47ff7ef35c"
  val angular_ui_calendar     = "org.webjars"         % "angular-ui-calendar"     % "0.9.0-beta.1"
  val angualr_ckeditor        = "org.webjars"         % "angular-ckeditor"        % "0.2.0"
  val marked                  = "org.webjars"         % "marked"                  % "0.3.2-1"
  val fontawesome             = "org.webjars"         % "font-awesome"            % "4.2.0"

  // Hashing Algorithms
  val pbkdf2                  = "io.github.nremond"   %% "pbkdf2-scala"           % "0.4"
  val bcrypt                  = "org.mindrot"         % "jbcrypt"                 % "0.3m"
  val scrypt                  = "com.lambdaworks"     % "scrypt"                  % "1.4.0"

  // Miscellaneous
  val mailer_plugin = "com.typesafe.play.plugins"      %% "play-plugins-mailer"    % "2.3.0"

  // Test Libraries
  val specs2                  = "org.specs2"          %% "specs2"                 % "2.1.1"       % "test"


//val icu4j                   = "com.ibm.icu"          % "icu4j"                  % "51.1"
//val geolocation             =  "com.edulify"        %% "geolocation"            % "1.1.0"

  val all_dependencies : Seq[ModuleID] = Seq(
    play_cache, play_filters, play_test, play_docs, play_ws,
    mailer_plugin,
    play2_reactivemongo, reactivemongo_ext,
    pbkdf2, bcrypt, scrypt,
    webjars_play,
    requirejs, requirejs_domready,
    angularjs, angular_drag_drop, angular_multi_select,
    angular_ui, angular_ui_bootstrap, angular_ui_router, angular_ui_utils, angular_ui_calendar,
    marked, fontawesome
  )

}

object ScrupalBuild extends Build with BuildSettings with AssetsSettings with Dependencies {

  addCommandAlias("tq", "test-quick")
  addCommandAlias("tm", "test-only scrupal.models")
  addCommandAlias("tu", "test-only scrupal.utils")
  addCommandAlias("tc", "test-only scrupal.controllers")

  val printClasspath = TaskKey[File]("print-class-path")

  def print_class_path = (target, fullClasspath in Compile, compile in Compile) map { (out, cp, analysis) =>
    println(cp.files.map(_.getCanonicalPath).mkString("\n"))
    println("----")
    println(analysis.relations.allBinaryDeps.toSeq.mkString("\n"))
    println("----")
    println(out)
    out
  }


  lazy val scrupal = Project(BuildInfo.appName, file("."))
    .enablePlugins(PlayScala)
    .enablePlugins(SbtWeb)
    .enablePlugins()
    .settings(buildSettings ++ Seq(
      fork in (Test) := false,
      pipelineStages := Seq(rjs, digest, gzip),

      //requireJs += "scrupal.js",
      //requireJsShim += "scrupal.js",
      // playAssetsDirectories <+= baseDirectory / "foo",
      resolvers ++= all_resolvers,
      libraryDependencies ++= all_dependencies,
      printClasspath <<= print_class_path
    ):_*
    //    angularAssets map { f : File => playAssetsDirectories <+= f }
    //    ++
  )

   override def rootProject = Some(scrupal)
}
