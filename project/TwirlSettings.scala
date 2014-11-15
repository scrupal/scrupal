
import sbt._
import sbt.Keys._
import play.twirl.sbt.Import.TwirlKeys

/**
 * Settings for building Scrupal. These are common settings for each sub-project.
 * Only put things in here that must be identical for each sub-project. Otherwise,
 * Specialize below in the definition of each Project object.
 */
trait TwirlSettings {
  lazy val twirlSettings = Seq[Setting[_]](
    sourceDirectories in (Compile, TwirlKeys.compileTemplates) := (unmanagedSourceDirectories in Compile).value
  )

  lazy val twirlSettings_core = twirlSettings ++ Seq[Setting[_]](
    TwirlKeys.templateImports += "scrupal.core.views.%format%._"
  )

  lazy val twirlSettings_http = twirlSettings ++ Seq[Setting[_]](
    TwirlKeys.templateImports ++= Seq(
      "scrupal.core.views.%format%._",
      "scrupal.http.views.%format%._"
    )
  )

  lazy val twirlSettings_top = twirlSettings ++ Seq[Setting[_]](
    TwirlKeys.templateImports ++= Seq(
      "scrupal.core.views.%format%._",
      "scrupal.http.views.%format%._",
      "scrupal.web.views.%format%._"
    )
  )
}
