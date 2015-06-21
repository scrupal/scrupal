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

package scrupal.api

import java.io.File
import java.net.{ URLClassLoader, URL }

import akka.http.scaladsl.model.{ MediaTypes, MediaType }
import com.typesafe.config.{ Config, ConfigFactory }
import play.api.Configuration
import play.api.http.ContentTypes
import scrupal.utils.OSSLicense

import scala.reflect.internal.util.ScalaClassLoader
import scala.collection.JavaConverters._

trait AssetsLocator {

  final val extension_delimiter : Char = '.'
  final val path_delimiter : String = "/"
  final val minified_extension : String = "min"

  def asset_path_from_config(config : Configuration) : Seq[String] = {
    config.getStringList("scrupal.assets_path") match {
      case Some(x) ⇒ x.asScala.toList
      case None    ⇒ Seq.empty[String]
    }
  }

  def assets_path : Seq[String]

  lazy val cl = {
    val cwd = new File(".")
    val uris = for (
      path ← assets_path;
      dir = new File(cwd, path) if dir.isDirectory
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
  def resourceOf(name : String) : Option[URL] = {
    val path = if (name.startsWith(path_delimiter)) name.drop(1) else name
    Option(cl.getResource(path)) /*match {
      case Some(url) ⇒ Some(url)
      case None ⇒
        for (p <- dev_mode_asset_dirs) {
          val f = new File(p, name)
          if (f.isFile && f.canRead)
            return Some(new URL("file","localhost",f.getCanonicalPath))
        }
        None
    }*/
  }

  def extensionOf(path : String) : String = {
    path.reverse.takeWhile(_ != extension_delimiter).reverse
  }

  /** Get the minified version of the resource
    *
    * This just looks for the minified version of a resource by altering the file name and calling resourceOf on that.
    * @param path The path of the resource to find
    * @return None if the resource couldn't be found, otherwise Some(URL) to either the minified or plain version
    */
  def minifiedResourceOf(path : String) : Option[URL] = {
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
  def fetch(path : String, mediaType : MediaType, minified : Boolean = true) : Response = {
    (if (minified) minifiedResourceOf(path) else resourceOf(path)) match {
      case Some(url) ⇒
        val stream = url.openStream()
        val length = stream.available
        StreamResponse(stream, mediaType)
      case None ⇒
        ErrorResponse(s"Asset '$path' could not be found", NotFound)
    }
  }

  def fetch(path : String) : Response = {
    val mediaType = MediaTypes.forExtension(extensionOf(path)).getOrElse(MediaTypes.`application/octet-stream`)
    fetch(path, mediaType)
  }

  val directoryAssetName = "__dir.conf"

  import AssetsLocator.Directory

  def fetchDirectory(path : String, recurse : Boolean = false) : Option[Directory] = {
    val cfg : Config = ConfigFactory.parseResourcesAnySyntax(cl, path + "/" + directoryAssetName)
    if (cfg.isEmpty)
      return None
    val config : Configuration = Configuration(cfg)
    val files = config.getConfig("files").fold(Map.empty[String, (String, Option[URL])])(c ⇒
      (c.entrySet.filter {
        case (k, v) ⇒
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
        config.getStringList("dirs").fold(Map.empty[String, Option[Directory]]) {
          case list : java.util.List[String] ⇒
            val dirs = for (d ← list.asScala) yield {
              val dSlashed = if (d.startsWith("/")) d else "/" + d
              d → fetchDirectory(path + dSlashed, recurse = true)
            }
            dirs.toMap
        }
    }
    val author = config.getString("author")
    val copyright = config.getString("copyright")
    val license = config.getString("license").flatMap { l ⇒ OSSLicense.lookup(Symbol(l)) }
    val name = config.getString("name")
    val title = config.getString("title")
    val description = config.getString("description")
    val index = config.getString("index")
    Some(Directory(author, copyright, license, name, title, description, index, files, dirs))
  }

  def isFile(url : URL) = { url != null && url.getProtocol == "file" }
  def isJar(url : URL) = { url != null && url.getProtocol == "jar" }

  /*
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

class ConfiguredAssetsLocator(config : Configuration) extends AssetsLocator {
  def assets_path = asset_path_from_config(config)
}

object AssetsLocator {
  case class Directory(
    author : Option[String] = None,
    copyright : Option[String] = None,
    license : Option[OSSLicense] = None,
    name : Option[String] = None,
    title : Option[String] = None,
    description : Option[String] = None,
    index : Option[String] = None,
    files : Map[String, (String, Option[URL])],
    dirs : Map[String, Option[Directory]])
}
