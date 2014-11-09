package scrupal.core

/**
 * Created by reid on 11/9/14.
 */
object PathFinder {
  def favicon() = "/assets/favicon.ico"

  def theme(provider: String, name: String) = "/assets/themes/default.css"

  def css(name: String) = s"/assets/css/$name"

  def css_s(name: String) = s"/assets/css/$name"
}
