import sbt._
import sbt.Keys._
import play.Project._
import sbt.Opts.resolver

/**
 * Settings for building Scrupal. These are common settings for each sub-project.
 * Only put things in here that must be identical for each sub-project. Otherwise,
 * Specialize below in the definition of each Project object.
 */
object BuildSettings
{
  val appName = "scrupal"
  val buildVersion = "0.1"
  val buildSettings = playScalaSettings ++ Seq (
    // credentials += Credentials(Path.userHome / ".ivy2" / ".credentials"),
    javacOptions ++= Seq(
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


object Resolvers
{
  val sbt_plugin_releases     = Resolver.url("SBT Plugin Releases", url("http://repo.scala-sbt.org/scalasbt/sbt-plugin-releases"))(Resolver.ivyStylePatterns)
  val pk11_scratch            = "Google PK11-Scratch" at "http://pk11-scratch.googlecode.com/svn/trunk"
  val typesafe_releases       = "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases/"
  val sonatype_releases       = "Sonatype Releases"  at "http://oss.sonatype.org/content/repositories/releases"
  val scala_lang              = "Scala Language" at "http://mvnrepository.com/artifact/org.scala-lang/"

//val geolocation             = "geolocation repository" at "http://blabluble.github.com/modules/releases/",

  val all_resolvers           = Seq ( sbt_plugin_releases, pk11_scratch, typesafe_releases, sonatype_releases,
	                                    scala_lang )
}

object Dependencies
{
  val play_plugins_redis      = "com.typesafe"        %% "play-plugins-redis"     % "2.1.1"
  val mailer_plugin           = "com.typesafe"        %% "play-plugins-mailer"    % "2.1.0"
  val slick                   = "com.typesafe.slick"  %% "slick"                  % "1.0.1"
  val h2                      = "com.h2database"      % "h2"                      % "1.3.173"

  val pbkdf2                  = "io.github.nremond"   %% "pbkdf2-scala"           % "0.2"
  val bcrypt                  = "org.mindrot"         % "jbcrypt"                 % "0.3m"
  val scrypt                  = "com.lambdaworks"     % "scrypt"                  % "1.4.0"

//val play2_reactivemongo     = "org.reactivemongo"   %% "play2-reactivemongo"    % "0.9"
//val icu4j                   = "com.ibm.icu"          % "icu4j"                  % "51.1"
//val geolocation             =  "com.edulify"        %% "geolocation"            % "1.1.0"

  // Test Libraries
  val specs2                  = "org.specs2"          %% "specs2"                 % "2.1.1"       % "test"

}

object ScrupalBuild extends Build {

  import BuildSettings._
  import Resolvers._
  import Dependencies._

  val printClasspath = TaskKey[File]("print-class-path")

  addCommandAlias("tq", "test-quick")
  addCommandAlias("tm", "test-only scrupal.models")
  addCommandAlias("tu", "test-only scrupal.utils")
  addCommandAlias("tc", "test-only scrupal.controllers")

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
      resolvers := all_resolvers,
      libraryDependencies := Seq (
        jdbc,
        cache,
        filters,
        component("play-test"),
        specs2,
        play_plugins_redis,
        mailer_plugin,
        slick,
        h2,
        pbkdf2, bcrypt, scrypt
      ),
      printClasspath <<= print_class_path
    )
  )
  override def rootProject = Some(scrupal)
}
