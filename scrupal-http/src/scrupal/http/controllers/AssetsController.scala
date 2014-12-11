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

package scrupal.http.controllers

import scrupal.api._
import scrupal.http.ScrupalMarshallers
import spray.http.{MediaTypes, StatusCodes, Uri}
import spray.routing._


/** Controller that provides assets
  *
  * This controller provides the assets that are "baked" in to a Scrupal applications.
 */
class AssetsController(scrupal: Scrupal) extends BasicController('Assets, priority=Int.MinValue /*Make Assets first*/)
  with AssetLocator with ScrupalMarshallers {

  val assets_path: Seq[String] = {
    scrupal.withConfiguration[Seq[String]] { config => super.asset_path_from_config(config)}
  }

  def routes(implicit scrupal: Scrupal): Route = {
    logRequestResponse(showAllResponses _) {
      get {
        pathPrefix("assets") {
          path("favicon") {
            favicon(/*theSite*/)
          } ~
            path("lib" / Segment / RestPath) { case (library, rest_of_path) ⇒
              lib(library, rest_of_path)
            } ~
            path("themes" / Segment / RestPath) { case (provider, rest_of_path) ⇒
              theme(provider, rest_of_path)
            } ~
            path("stylesheets" / RestPath) { rest_of_path: Uri.Path ⇒
              stylesheets(rest_of_path)
            } ~
            path("javascripts" / RestPath) { rest_of_path: Uri.Path ⇒
              javascripts(rest_of_path)
            } ~
            path("images" / RestPath) { rest_of_path: Uri.Path ⇒
              images(rest_of_path)
            } ~
            path(RestPath) { rest_of_path: Uri.Path ⇒
              val r = (StatusCodes.NotFound, s"Asset '$rest_of_path' was not found.")
              complete(r)
            }
        }
      }
    }
  }

  def resultAsRoute(result: ⇒ Result[_])(implicit scrupal: Scrupal): Route = {
    if (result.disposition.isSuccessful) {
      complete {makeMarshallable(result)}
    } else {
      result match {
        case er: ErrorResult =>
          if (result.disposition == NotFound) {
            reject
          } else {
            reject
          }
      }
    }
  }

  def favicon(/*aSite: Site*/)(implicit scrupal: Scrupal): StandardRoute = {
    complete {
      val path = "images/scrupal.ico"
      val result = fetch(path, MediaTypes.`image/x-icon`, minified = false)
      makeMarshallable(result)
    }
  }

  def lib(library: String, rest_of_path: Uri.Path)(implicit scrupal: Scrupal): Route = {
    complete {
      val thePath = s"lib/$library/${rest_of_path}"
      val result = fetch(thePath)
      makeMarshallable(result)
    }
  }

  def theme(provider: String, rest_of_path: Uri.Path)(implicit scrupal: Scrupal): Route = {
    complete {
      val thePath = s"themes/$provider/$rest_of_path"
      val result = fetch(thePath)
      makeMarshallable(result)
    }
  }

  def images(rest_of_path: Uri.Path)(implicit scrupal: Scrupal): Route = {
    complete {
      val thePath = s"images/$rest_of_path"
      val result = fetch(thePath)
      makeMarshallable(result)
    }
  }

  def javascripts(rest_of_path: Uri.Path)(implicit scrupal: Scrupal): Route = {
    complete {
      val thePath = s"javascripts/$rest_of_path"
      val result = fetch(thePath)
      makeMarshallable(result)
    }
  }

  def stylesheets(rest_of_path: Uri.Path)(implicit scrupal: Scrupal): Route = {
    complete {
      val thePath = s"stylesheets/$rest_of_path"
      val result = fetch(thePath)
      makeMarshallable(result)
    }
  }

}

case class WebJarsController() extends BasicController('webjars) {
  def routes(implicit scrupal: Scrupal): Route = {
    path("javascripts" / RestPath) { file =>
      get {
        complete("foo")
      }
    }
  }
}


/*
class AssetsBuilder(errorHandler: HttpErrorHandler) extends Controller {

import Assets._
import AssetInfo._

private def currentTimeFormatted: String = df.print((new Date).getTime)

private def maybeNotModified(request: Request[_], assetInfo: AssetInfo, aggressiveCaching: Boolean): Option[Result] = {
  // First check etag. Important, if there is an If-None-Match header, we MUST not check the
  // If-Modified-Since header, regardless of whether If-None-Match matches or not. This is in
  // accordance with section 14.26 of RFC2616.
  request.headers.get(IF_NONE_MATCH) match {
    case Some(etags) =>
      assetInfo.etag.filter(someEtag => etags.split(',').exists(_.trim == someEtag)).flatMap(_ => Some(cacheableResult(assetInfo, aggressiveCaching, NotModified)))
    case None =>
      for {
        ifModifiedSinceStr <- request.headers.get(IF_MODIFIED_SINCE)
        ifModifiedSince <- parseModifiedDate(ifModifiedSinceStr)
        lastModified <- assetInfo.parsedLastModified
        if !lastModified.after(ifModifiedSince)
      } yield {
        NotModified.withHeaders(DATE -> currentTimeFormatted)
      }
  }
}

private def cacheableResult[A <: Result](assetInfo: AssetInfo, aggressiveCaching: Boolean, r: A): Result = {

  def addHeaderIfValue(name: String, maybeValue: Option[String], response: Result): Result = {
    maybeValue.fold(response)(v => response.withHeaders(name -> v))
  }

  val r1 = addHeaderIfValue(ETAG, assetInfo.etag, r)
  val r2 = addHeaderIfValue(LAST_MODIFIED, assetInfo.lastModified, r1)

  r2.withHeaders(CACHE_CONTROL -> assetInfo.cacheControl(aggressiveCaching))
}

private def result(file: String,
  length: Int,
  mimeType: String,
  resourceData: Enumerator[Array[Byte]],
  gzipRequested: Boolean,
  gzipAvailable: Boolean): Result = {

  val response = Result(
    ResponseHeader(
      OK,
      Map(
        CONTENT_LENGTH -> length.toString,
        CONTENT_TYPE -> mimeType,
        DATE -> currentTimeFormatted
      )
    ),
    resourceData)
  if (gzipRequested && gzipAvailable) {
    response.withHeaders(VARY -> ACCEPT_ENCODING, CONTENT_ENCODING -> "gzip")
  } else if (gzipAvailable) {
    response.withHeaders(VARY -> ACCEPT_ENCODING)
  } else {
    response
  }
}

/**
 * Generates an `Action` that serves a versioned static resource.
 */
def versioned(path: String, file: Asset): Action[AnyContent] = {
  val f = new File(file.name)
  // We want to detect if it's a fingerprinted asset, because if it's fingerprinted, we can aggressively cache it,
  // otherwise we can't.
  val requestedDigest = f.getName.takeWhile(_ != '-')
  if (!requestedDigest.isEmpty) {
    val bareFile = new File(f.getParent, f.getName.drop(requestedDigest.size + 1)).getPath
    val bareFullPath = new File(path + File.separator + bareFile).getPath
    blocking(digest(bareFullPath)) match {
      case Some(`requestedDigest`) => at(path, bareFile, aggressiveCaching = true)
      case _ => at(path, file.name)
    }
  } else {
    at(path, file.name)
  }
}

/**
 * Generates an `Action` that serves a static resource.
 *
 * @param path the root folder for searching the static resource files, such as `"/public"`. Not URL encoded.
 * @param file the file part extracted from the URL. May be URL encoded (note that %2F decodes to literal /).
 * @param aggressiveCaching if true then an aggressive set of caching directives will be used. Defaults to false.
 */
def at(path: String, file: String, aggressiveCaching: Boolean = false): Action[AnyContent] = Action.async {
  implicit request =>

    import Implicits.trampoline
    val assetName: Option[String] = resourceNameAt(path, file)
    val assetInfoFuture: Future[Option[(AssetInfo, Boolean)]] = assetName.map { name =>
      assetInfoForRequest(request, name)
    } getOrElse Future.successful(None)

    val pendingResult: Future[Result] = assetInfoFuture.flatMap {
      case Some((assetInfo, gzipRequested)) =>
        val stream = assetInfo.url(gzipRequested).openStream()
        val length = stream.available
        val resourceData = Enumerator.fromStream(stream)(Implicits.defaultExecutionContext)

        Future.successful(maybeNotModified(request, assetInfo, aggressiveCaching).getOrElse {
          cacheableResult(
            assetInfo,
            aggressiveCaching,
            result(file, length, assetInfo.mimeType, resourceData, gzipRequested, assetInfo.gzipUrl.isDefined)
          )
        })
      case None => errorHandler.onClientError(request, NOT_FOUND, "Resource not found by Assets controller")
    }

    pendingResult.recoverWith {
      case e: InvalidUriEncodingException =>
        errorHandler.onClientError(request, BAD_REQUEST, s"Invalid URI encoding for $file at $path: " + e.getMessage)
      case NonFatal(e) =>
        // Add a bit more information to the exception for better error reporting later
        errorHandler.onServerError(request, new RuntimeException(s"Unexpected error while serving $file at $path: " + e.getMessage, e))
    }
}

/**
 * Get the name of the resource for a static resource. Used by `at`.
 *
 * @param path the root folder for searching the static resource files, such as `"/public"`. Not URL encoded.
 * @param file the file part extracted from the URL. May be URL encoded (note that %2F decodes to literal /).
 */
private[controllers] def resourceNameAt(path: String, file: String): Option[String] = {
  val decodedFile = UriEncoding.decodePath(file, "utf-8")
  def dblSlashRemover(input: String): String = dblSlashPattern.replaceAllIn(input, "/")
  val resourceName = dblSlashRemover(s"/$path/$decodedFile")
  val resourceFile = new File(resourceName)
  val pathFile = new File(path)
  if (!resourceFile.getCanonicalPath.startsWith(pathFile.getCanonicalPath)) {
    None
  } else {
    Some(resourceName)
  }
}

private val dblSlashPattern = """//+""".r

// Save the Play AssetBuilder object under a new name so we can refer to it without referring to ourself!
val assetBuilder = controllers.Assets
def fallback(path : String, file : String) : Action[AnyContent] = {
 assetBuilder.at(path, file)
}

val root = "/public"
val javascripts = root + "/javascripts"
val javascripts_min = root + "/javascripts_min"
val stylesheets = root + "/stylesheets"
val images = root + "/images"
val themes = root + "/themes"
val docs = root + "/docs"
val chunks = root + "/chunks"
val templates = root + "/templates"

/** Resolve a path/file combination from either WebJars or the static/compiled resources Scrupal provides
 * Attempts to resolve the path/file combination using WebJars but if the file could not be located there then it
 * falls back to using Play's AssetBuilder to locate the resource inherent to Scrupal.
 * @param path The static path at which the resource might be located (if not found b WebJars)
 * @param file The basic file name with path and basename but without suffixes or versions
 * @return The Enumeratee of the resource
 */
def resolve(path: String, file: String) : Action[AnyContent] = {
 try {
   resolve(file)
 }
 catch {
   case x: IllegalArgumentException => fallback(path, file)
 }
}

def resolve(file:String) : Action[AnyContent] = {
 val expanded_file_path = super.locate(file)
 super.at(expanded_file_path)
}

def misc(file: String) = resolve(root, file)

/** Get a Javascript from a Jar file
 * Uses WebJarAssets to locate and return the Jar file corresponding to the argument which must end with .js. T
 * @param file
 * @return
 */
def js(file: String, min : Boolean = true) = resolve("", minify(file, ".js", min))

def requirejs() = resolve(javascripts, minify("require.js", ".js", true))

/** Get a Javascript from assets/javascripts (static or compiled)
 * Just uses the Play AssetBuilder to extract the javascript file.
 * @param file The name of the script with partial path after "javascripts" and no version or suffix.
 * @param min Whether or not to minify the resulting file name (always off for Dev mode)
 * @return The Content of the file as an Action
 */
def js_s(file: String, min : Boolean = true) = {
 if (file.endsWith(".js"))
   resolve(javascripts, minify(file, ".js", min))
 else
   fallback(javascripts,file)
}
def js_s_min(file: String) = js_s(file, true)

/** Get a Stylesheet from a Jar file
 * Uses WebJarAssets to locate and return the `file` from within a ClassLoaded Jar file.
 *
 * @param file The name of the file without path prefix, version nor suffix, just the basename
 * @param min Whether or not to minify the resulting file name (always off for Dev mode)
 * @return The Content of the file as an Action
 */
def css(file: String, min : Boolean = true) = resolve(stylesheets, minify(file, ".css", min))

/** Get a Stylesheet from public/stylesheets (static or compiled)
 *
 * @param file The partial path with no suffix
 * @return The Content of the file as an Action
 */
def css_s(file: String) = fallback(stylesheets, minify(file, ".css", min=false))

/** Get a PNG (Portable Network Graphic) file with extension .png from the static assets
 *
 * @param file name of the file to fetch with any partial path (after /public/images) and without the suffix
 * @return
 */
def img(file: String) = fallback(images, file)

/** Get the correct favicon for the context
 * todo : Defaulted for now to a static result :(
 * @return The /public/images/favicon.png file
 */
def favicon = fallback(images, "viritude.ico")

/**
* A way to obtain a theme css file just by the name of the theme
* @param provider The web source for the theme
* @param name Name of the theme
* @return path to the theme's .css file
*/
def theme(provider: String, name: String, min: Boolean = true) : Action[AnyContent] =  {
 (Global.ScrupalIsConfigured && !CoreFeatures.DevMode) match {
   case true => {
     provider.toLowerCase() match {
       case "scrupal"    => {
         // TODO : Look it up in the database first and if that does not work forward on to static resolution
         fallback(themes, minify(name,".css", true))
       }
       case "bootswatch" =>  Action { request:RequestHeader =>
         // TODO : Deal with "NotModified" better here?
         MovedPermanently("http://bootswatch.com/" + name + "/" + minify("bootstrap", ".css", min))
       }
       case _ =>  fallback(stylesheets, "boostrap.min.css")
     }
   }
   case false => fallback(themes, "bootswatch/cyborg.min.css")
 }
}

/** Serve markdown fragments that provide the documentation
 *
 */
def doc(path: String) = fallback(docs, path)

def isValidDocAsset(path: String) : Boolean = {
 resourceNameAt(docs, path).exists { resourceName: String => Play.resource(resourceName).exists { u: URL => true } }
}

private def resourceNameAt(path: String, file: String): Option[String] = {
 val decodedFile = UriEncoding.decodePath(file, "utf-8")
 val resourceName = Option(path + "/" + decodedFile).map(name => if (name.startsWith("/")) name else ("/" + name)).get
 if (new File(resourceName).isDirectory || !new File(resourceName).getCanonicalPath.startsWith(new File(path).getCanonicalPath)) {
   None
 } else {
   Some(resourceName)
 }
}


/** Serve AngularJS Partial Chunks of HTML
* An accessor for getting Angular's partial/fragment/chunks of HTML for composed views. We store the HTML files in
* a directory named "chunks" underneath the javascript module's directory. But, we ask for it with a path like
* /chunks/module/file.html and the router routes such requests HERE.
* @param module The name of the Angular module
* @param file The name of the file being requested
* @return The content of the file
*/
def chunk(module: String, file: String) = fallback(chunks + "/" + module, file)

/** ng-ui-bootstrap requires template files to satisfy some of its directives.
 *
 * @param path Path to the template
 * @return
 */
def template(path: String) = resolve(root, "/template/" + path)

/** The pattern for extracting the suffix from a file name */
private lazy val suffix_r = "(\\.[^.]*)$".r

private def minify(file: String, suffix: String, min: Boolean ) = {
 (min && Play.mode != Mode.Dev, file.endsWith(suffix)) match {
   case (false, false) => file + suffix
   case (false, true) => file
   case (true, false) => file + ".min" + suffix
   case (true, true) => suffix_r.replaceFirstIn(file, ".min$1")
 }
}
*/
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

*/
