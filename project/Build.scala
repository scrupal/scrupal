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

import sbt._
import sbt.Keys._

/**
 * Settings for building Scrupal. These are common settings for each sub-project.
 * Only put things in here that must be identical for each sub-project. Otherwise,
 * Specialize below in the definition of each Project object.
 */
object BuildSettings
{
  val appName = "scrupal"

  import com.typesafe.config._
  val conf = ConfigFactory.parseFile(new File("conf/application.conf")).resolve()
  val buildVersion = conf.getString("app.version")

  val buildSettings = Seq (
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
    scalaVersion    := "2.10.2",
    shellPrompt     := ShellPrompt.buildShellPrompt,
    version         := buildVersion
  )
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
      "%s:%s:%s> ".format (currBranch, currProject, BuildSettings.buildVersion)
    }
  }
}


trait Resolvers
{
  // val scrupal_org_releases    = "Scrupal.org Releases" at "http://scrupal.github.org/mvn/releases"
  val google_sedis            = "Google Sedis" at "http://pk11-scratch.googlecode.com/svn/trunk"
  val typesafe_releases       = "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases/"
//val sonatype_releases       = "Sonatype Releases"  at "http://oss.sonatype.org/content/repositories/releases"
//val scala_lang              = "Scala Language" at "http://mvnrepository.com/artifact/org.scala-lang/"
//val sbt_plugin_releases     = Resolver.url("SBT Plugin Releases",url("http://repo.scala-sbt.org/scalasbt/sbt-plugin-releases"))(Resolver.ivyStylePatterns)
//val geolocation             = "geolocation repository" at "http://blabluble.github.com/modules/releases/"

  val all_resolvers           = Seq ( typesafe_releases, google_sedis  )
}

trait Dependencies
{
  // Databass, Caches, Data Storage stuff
  val play_plugins_redis      = "com.typesafe"        %% "play-plugins-redis"     % "2.1.1"
  val slick                   = "com.typesafe.slick"  %% "slick"                  % "1.0.1"
  val h2                      = "com.h2database"      % "h2"                      % "1.3.173"
  val postgresql              = "postgresql"          % "postgresql"              % "9.1-901.jdbc4"

  // WebJars based UI components
  val webjars_play            = "org.webjars"         %% "webjars-play"           % "2.2.0"
  val requirejs               = "org.webjars"         % "requirejs"               % "2.1.8"
  val requirejs_domready      = "org.webjars"         % "requirejs-domready"      % "2.0.1"
  // val bootstrap               = "org.webjars"         % "bootstrap"               % "3.0.0"
  val angularjs               = "org.webjars"         % "angularjs"               % "1.1.5-1"
  val angular_ui              = "org.webjars"         % "angular-ui"              % "0.4.0-1"
  val angular_ui_bootstrap    = "org.webjars"         % "angular-ui-bootstrap"    % "0.6.0-1"
  val angular_ui_router       = "org.webjars"         % "angular-ui-router"       % "0.2.0"
  val marked                  = "org.webjars"         % "marked"                  % "0.2.9"
  val fontawesome             = "org.webjars"         % "font-awesome"            % "3.2.1"

  // Hashing Algorithms
  val pbkdf2                  = "io.github.nremond"   %% "pbkdf2-scala"           % "0.2"
  val bcrypt                  = "org.mindrot"         % "jbcrypt"                 % "0.3m"
  val scrypt                  = "com.lambdaworks"     % "scrypt"                  % "1.4.0"

  // Miscellaneous
  val mailer_plugin           = "com.typesafe"        %% "play-plugins-mailer"    % "2.2.0"

  // Test Libraries
  val specs2                  = "org.specs2"          %% "specs2"                 % "2.1.1"       % "test"


//val play2_reactivemongo     = "org.reactivemongo"   %% "play2-reactivemongo"    % "0.9"
//val icu4j                   = "com.ibm.icu"          % "icu4j"                  % "51.1"
//val geolocation             =  "com.edulify"        %% "geolocation"            % "1.1.0"

}

object ScrupalBuild extends Build  with Resolvers with Dependencies {

  import BuildSettings._

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

  lazy val scrupal = play.Project(
    appName,
    path = file("."),
    settings = buildSettings ++ Seq (
      //requireJs += "scrupal.js",
      //requireJsShim += "scrupal.js",
      resolvers := all_resolvers,
      // playAssetsDirectories <+= baseDirectory / "foo",
      libraryDependencies := Seq (
        play.Keys.jdbc,
        play.Keys.cache,
        play.Keys.filters,
        play.Keys.component("play-test"),
        play.Keys.component("play-docs"),
        specs2,
        play_plugins_redis,
        mailer_plugin,
        slick,
        h2,
        pbkdf2, bcrypt, scrypt,
        webjars_play, requirejs, requirejs_domready,  angularjs, angular_ui, angular_ui_bootstrap,
        angular_ui_router, marked, fontawesome
      ),
      printClasspath <<= print_class_path
    ) ++  play.Project.playScalaSettings ++ Seq (
      // This bit of nonsense brought to you by https://github.com/playframework/playframework/issues/1813
      // and its related friend at
      fork in (Test) := false
    )

    //    angularAssets map { f : File => playAssetsDirectories <+= f }
    //    ++
  )

   override def rootProject = Some(scrupal)
}
