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

package scrupal.core

import java.io.File
import java.net.URL

import scrupal.api.{ErrorResult, StreamResult, NotFound, Result}
import spray.http.{MediaTypes, MediaType}

trait AssetLocator {

  final val extension_delimiter : Char = '.'
  final val path_delimiter : String = "/"
  final val minified_extension : String = "min"
  def asset_dirs : Seq[String]

  /** Find A Resource In The Classpath
    *
    * Static assets are searched in the classpath because sbt-web puts them into a XXX-assets.jar file which is
    * part of the classpath. This just invokes the ClassLoader to find a resource in the class path and makes sure we
    * strip any leading / from the name as ClassLoader.getResource requires.
    * @param name - The name of the resource to find.
    * @return None if it wasn't found, Some(URL) if it was
    */
  def resourceOf(name: String) : Option[URL] = {
    val cl = this.getClass.getClassLoader
    val path = if (name.startsWith(path_delimiter)) name.drop(1) else name
    Option(cl.getResource(path)) match {
      case Some(url) => Some(url)
      case None =>
        for (p <- asset_dirs) {
          val f = new File(p, name)
          if (f.isFile && f.canRead)
            return Some(new URL("file","localhost",f.getCanonicalPath))
        }
        None
    }
  }

  def extensionOf(path: String) : String = {
    path.reverse.takeWhile(_ != extension_delimiter).reverse
  }

  /** Get the minified version of the resource
    *
    * This just looks for the minified version of a resource by altering the file name and calling resourceOf on that.
    * @param path The path of the resource to find
    * @return None if the resource couldn't be found, otherwise Some(URL) to either the minified or plain version
    */
  def minifiedResourceOf(path: String): Option[URL] = {
    val extension = extensionOf(path)
    val extensionless = path.dropRight(extension.size + 1)
    val minifiedPath = extensionless + extension_delimiter + minified_extension + extension_delimiter + extension
    resourceOf(minifiedPath).orElse { resourceOf(path) }
  }

  /** Turn a path and a mediaType into a streamable result.
    *
    * The path is looked up as a resource and if it is found an EnumeratorResult is returned that will supply the
    * contents of the file. If the resource is not found, an ErrorResult of Disposition=NotFound is returned.
    * @param path The path to look up
    * @param mediaType The MediaType to associate with the Result
    * @return ErrorResult if the resource could not be located, EnumeratorResult if it could
    */
  def fetch(path: String, mediaType: MediaType, minified : Boolean = true) : Result[_] = {
    (if (minified) minifiedResourceOf(path) else resourceOf(path)) match {
      case Some(url) =>
        val stream = url.openStream()
        val length = stream.available
        StreamResult(stream, mediaType)
      case None =>
        ErrorResult(s"Asset '$path' could not be found", NotFound)
    }
  }

  def fetch(path: String) : Result[_] = {
    val mediaType = MediaTypes.forExtension(extensionOf(path)).getOrElse(MediaTypes.`application/octet-stream`)
    fetch(path, mediaType)
  }

}
