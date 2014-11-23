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

package scrupal.api

import java.io.File
import java.net.{URLClassLoader, URL}

import com.typesafe.config.{Config, ConfigFactory}
import scrupal.utils.{OSSLicense, Configuration}
import spray.http.{MediaType, MediaTypes}

import scala.reflect.internal.util.ScalaClassLoader
import scala.collection.JavaConverters._

trait AssetLocator {

  final val extension_delimiter : Char = '.'
  final val path_delimiter : String = "/"
  final val minified_extension : String = "min"

  def asset_path_from_config(config: Configuration) : Seq[String] = {
    config.getStringList("scrupal.assets_path") match {
      case Some(x) => x.asScala.toList
      case None => Seq.empty[String]
    }
  }

  def assets_path : Seq[String]

  lazy val cl = {
    val cwd = new File(".")
    val uris = for (
      path ← assets_path ;
      dir = new File(cwd,path) if dir.isDirectory
    ) yield { dir.toURI.toURL }
    ScalaClassLoader(new URLClassLoader(uris.toArray[URL], this.getClass.getClassLoader))
  }

  /** Find A Resource In The Classpath
    *
    * Static assets are searched in the classpath because sbt-web puts them into a XXX-assets.jar file which is
    * part of the classpath. This just invokes the ClassLoader to find a resource in the class path and makes sure we
    * strip any leading / from the name as ClassLoader.getResource requires.
    * @param name - The name of the resource to find.
    * @return None if it wasn't found, Some(URL) if it was
    */
  def resourceOf(name: String) : Option[URL] = {
    val path = if (name.startsWith(path_delimiter)) name.drop(1) else name
    Option(cl.getResource(path)) /*match {
      case Some(url) => Some(url)
      case None =>
        for (p <- dev_mode_asset_dirs) {
          val f = new File(p, name)
          if (f.isFile && f.canRead)
            return Some(new URL("file","localhost",f.getCanonicalPath))
        }
        None
    }*/
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

  val directoryAssetName = "__dir.conf"

  case class Directory(
    author : Option[String] = None,
    copyright : Option[String] = None,
    license : Option[OSSLicense] = None,
    title : Option[String] = None,
    description: Option[String] = None,
    index : Option[String] = None,
    files : Map[String, (String,Option[URL])],
    dirs : Map[String, Option[Directory]]
  )

  def fetchDirectory(path: String, recurse: Boolean = false) : Option[Directory] = {
    val cfg: Config = ConfigFactory.parseResourcesAnySyntax(cl, path + "/" + directoryAssetName)
    if (cfg.isEmpty)
      return None
    val config : Configuration = Configuration(cfg)
    val license = config.getString("license").flatMap { l ⇒ OSSLicense.lookup(Symbol(l)) }
    val files = config.getConfig("files").fold(Map.empty[String, (String,Option[URL])])(c ⇒
      (c.entrySet.filter { case (k, v) ⇒
        v.unwrapped.isInstanceOf[String]
      } map {
        case (k, v) ⇒
          val kSlashed = if (k.startsWith("/")) k else "/" + k
          val title = v.unwrapped.asInstanceOf[String]
          val resource = minifiedResourceOf(path + kSlashed)
          k → (title -> resource)
      }).toMap)

    val dirs : Map[String, Option[Directory]] = recurse match {
      case false ⇒ Map.empty[String, Option[Directory]]
      case true ⇒
        config.getStringList("dirs").fold(Map.empty[String, Option[Directory]]) { case list: java.util.List[String] ⇒
          val dirs = for (d ← list.asScala) yield {
            val dSlashed = if (d.startsWith("/")) d else "/" + d
            d → fetchDirectory(path + dSlashed, recurse=true)
          }
          dirs.toMap
        }
    }
    Some(Directory(config.getString("author"), config.getString("copyright"), license, config.getString("title"),
              config.getString("description"), config.getString("index"), files, dirs))
  }

  def isFile(url: URL) = { url != null && url.getProtocol == "file" }
  def isJar(url: URL) =  { url != null && url.getProtocol == "jar" }

  /**
   * List directory contents for a resource folder. Not recursive.
   * This is basically a brute-force implementation.
   * Works for regular files and also JARs.
   *
  String[] getResourceListing(Class clazz, String path)  {
    URL dirURL = clazz.getClassLoader().getResource(path);
    if (dirURL != null && dirURL.getProtocol().equals("file")) {
      /* A file path: easy enough */
      return new File(dirURL.toURI()).list();
    }

    if (dirURL == null) {
      /*
       * In case of a jar file, we can't actually find a directory.
       * Have to assume the same jar as clazz.
       */
      String me = clazz.getName().replace(".", "/")+".class";
      dirURL = clazz.getClassLoader().getResource(me);
    }

    if (dirURL.getProtocol().equals("jar")) {
      /* A JAR path */
      String jarPath = dirURL.getPath().substring(5, dirURL.getPath().indexOf("!")); //strip out only the JAR file
      JarFile jar = new JarFile(URLDecoder.decode(jarPath, "UTF-8"));
      Enumeration<JarEntry> entries = jar.entries(); //gives ALL entries in jar
      Set<String> result = new HashSet<String>(); //avoid duplicates in case it is a subdirectory
      while(entries.hasMoreElements()) {
        String name = entries.nextElement().getName();
        if (name.startsWith(path)) { //filter according to the path
          String entry = name.substring(path.length());
          int checkSubdir = entry.indexOf("/");
          if (checkSubdir >= 0) {
            // if it is a subdirectory, we just return the directory name
            entry = entry.substring(0, checkSubdir);
          }
          result.add(entry);
        }
      }
      return result.toArray(new String[result.size()]);
    }

    throw new UnsupportedOperationException("Cannot list files for URL "+dirURL);
  }
   */

}

class ConfiguredAssetsLocator(config: Configuration) extends AssetLocator {
  def assets_path = asset_path_from_config(config)
}

