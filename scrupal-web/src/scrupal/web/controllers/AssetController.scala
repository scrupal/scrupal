package scrupal.web.controllers

import scrupal.http.controllers.BasicController
import spray.http.Uri
import spray.routing.Directives._
import spray.routing._

/**
 * Created by reidspencer on 10/29/14.
 */
case class AssetsController() extends BasicController("assets") {

  def id = 'assets

  def routes: Route = {
    get {
      path("favicon") {
        favicon
      } ~
      path("javascripts" / RestPath) { rest_path =>
        javascripts(rest_path)
      } ~
      path("stylesheets" / RestPath) { rest_path =>
        stylesheets(rest_path)
      }
      path("images" / RestPath) { rest_path =>
        images(rest_path)
      }
    }
  }

  def favicon = complete("favicon")
  def javascripts(rest_of_path: Uri.Path) = complete("javascripts: " + rest_of_path)
  def stylesheets(rest_of_path: Uri.Path) = complete("stylesheets: " + rest_of_path)
  def images(rest_of_path: Uri.Path) = complete("images: " + rest_of_path)
}

case class WebJarsController() extends BasicController("webjars") {
  def id = 'webjars
  def routes: Route = {
    path("javascripts" / RestPath) { file =>
      get {
        complete("foo")
      }
    }
  }
}

  /**
      # Special handling for AngularJS modules that have partial HTML files that need to be served.
      GET            /chunks/:module/:file                  scrupal.controllers.Assets.chunk(module,file)

      # Special handling for the favicon
        GET            /assets/favicon                        scrupal.controllers.Assets.favicon

      # Get dependency managed resources from WebJars
        GET            /webjars/require.js                    scrupal.controllers.Assets.requirejs
      GET            /webjars/javascripts/ *file             scrupal.controllers.Assets.js(file)
    GET            /webjars/stylesheets/ *file             scrupal.controllers.Assets.css(file)
    GET            /webjars/ *file                         scrupal.controllers.Assets.misc(file)

    # Map the specialized, manufactured jsroutes.js file which maps THESE routes into javascript !
    GET            /assets/javascripts/jsroutes.js        scrupal.controllers.Home.jsRoutes(varName ?= "jsRoutes")

    # Map static resources from the /assets folder to the /assets URL path
     GET            /assets/javascripts/ *file              scrupal.controllers.Assets.js_s(file)
    GET            /assets/javascripts-min/ *file          scrupal.controllers.Assets.js_s_min(file)
    GET            /assets/stylesheets/ *file              scrupal.controllers.Assets.css_s(file)
    #GET            /assets/stylesheets-min/ *file          scrupal.controllers.Assets.css_s(file,min=true)
    GET            /assets/images/ *file                   scrupal.controllers.Assets.img(file)
    GET            /assets/themes/:provider/ *file         scrupal.controllers.Assets.theme(provider, file)
    GET            /assets/doc/ *file                      scrupal.controllers.Assets.doc(file)
    GET            /webjars/ *file                         scrupal.controllers.Assets.resolve(file)
    GET            /template/ *file                        scrupal.controllers.Assets.template(file)

 **/
