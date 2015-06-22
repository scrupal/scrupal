/** ********************************************************************************************************************
  * This file is part of Scrupal, a Scalable Reactive Content Management System.                                       *
  *                                                                                                            *
  * Copyright © 2015 Reactific Software LLC                                                                            *
  *                                                                                                            *
  * Licensed under the Apache License, Version 2.0 (the "License");  you may not use this file                         *
  * except in compliance with the License. You may obtain a copy of the License at                                     *
  *                                                                                                            *
  * http://www.apache.org/licenses/LICENSE-2.0                                                                  *
  *                                                                                                            *
  * Unless required by applicable law or agreed to in writing, software distributed under the                          *
  * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,                          *
  * either express or implied. See the License for the specific language governing permissions                         *
  * and limitations under the License.                                                                                 *
  * ********************************************************************************************************************
  */

package scrupal.utils

import java.io.{ PrintWriter, File }

import com.typesafe.config._
import play.api.{ Environment, Configuration }

import scala.collection.JavaConverters._
import scala.collection.immutable.TreeMap
import scala.util.Try
import scala.util.matching.Regex

/** This object provides a set of operations to create `Configuration` values.
  *
  * For example, to load a `Configuration` in a running application:
  * {{{
  * val config = Configuration.load()
  * val foo = config.getString("foo").getOrElse("boo")
  * }}}
  *
  * The underlying implementation is provided by https://github.com/typesafehub/config.
  */
object ConfigHelpers extends ScrupalComponent {

  // The configuration key that says where to get the database configuration data.
  val scrupal_storage_config_file_key = "scrupal.storage.config.file"

  /** Pimp The Play Configuration class
    * @param under The underlying Configuration implementation
    */
  implicit class ConfigurationPimps(under : Configuration) extends ScrupalComponent {

    type FlatConfig = TreeMap[String, ConfigValue]

    def flatConfig : FlatConfig = { TreeMap[String, ConfigValue](under.entrySet.toSeq : _*) }

    def interestingFlatConfig : FlatConfig = {
      val elide : Regex = "^(akka|java|sun|user|awt|os|path|line).*".r
      val entries = under.entrySet.toSeq.filter { case (x, y) ⇒ elide.findPrefixOf(x).isEmpty }
      TreeMap[String, ConfigValue](entries.toSeq : _*)
    }

    import scrupal.utils.ClassHelpers._

    /** Convert any class name into an instance of that class, assuming it has an empty args constructor
      *
      * @param name The class name
      * @param m A manifest for the class
      * @tparam C The kind of class expected, a base class
      * @return An instance of C that is of class `name` or None if it couldn't be instantiated.
      */
    def getInstance[C <: AnyRef](name : String)(implicit m : Manifest[C]) : Option[C] = {
      try {
        Option(string2instance[C](name))
      } catch {
        case x : IllegalAccessException ⇒
          log.error("Cannot access class " + name + " while instantiating: ", x); None
        case x : InstantiationException ⇒
          log.error("Cannot instantiate uninstantiable class " + name + ": ", x); None
        case x : ExceptionInInitializerError ⇒
          log.error("Instance initialization of " + name + " failed: ", x); None
        case x : SecurityException ⇒
          log.error("Security exception while instantiating " + name + ": ", x); None
        case x : LinkageError ⇒
          log.error("Linkage error while instantiating " + name + ": ", x); None
        case x : ClassNotFoundException ⇒
          log.error("Cannot find class " + name + " to instantiate: ", x); None
        case x : Throwable ⇒ throw x
      }
    }
  }

  private[this] lazy val dontAllowMissingConfigOptions = ConfigParseOptions.defaults().setAllowMissing(false)

  private[this] lazy val dontAllowMissingConfig = ConfigFactory.load(dontAllowMissingConfigOptions)

  /** loads `Configuration` based on the environment it finds itself in. from config.resource or config.file.
    * If not found default to 'conf/application.conf' in Dev mode
    * @return  configuration to be used
    */
  private[scrupal] def loadDev(env : Environment, devSettings : Map[String, String]) : Config = {
    try {
      lazy val file : File = {
        devSettings.get("config.file").orElse(Option(System.getProperty("config.file")))
          .map(f ⇒ new File(f)).getOrElse(env.getExistingFile("conf/application.conf").get)
      }
      val config = Option(System.getProperty("config.resource"))
        .map(ConfigFactory.parseResources).getOrElse(ConfigFactory.parseFileAnySyntax(file))

      ConfigFactory.parseMap(devSettings.asJava).withFallback(ConfigFactory.load(config))
    } catch {
      case e : ConfigException ⇒ toss(s"Configuration error: ${e.getMessage}", e)
    }
  }

  def from(env : Environment) = {
    new Configuration(loadDev(env, Map.empty[String, String]))
  }

  def from(fileName : String) : Option[Configuration] = {
    val env = Environment.simple()
    env.getExistingFile(fileName) map { file : File ⇒
      Configuration(ConfigFactory.parseFileAnySyntax(file))
    }
  }

  def from(underlying : Config) = {
    new Configuration(underlying)
  }

  def default() = {
    val env = Environment.simple()
    val cwd = env.rootPath
    val conf = new File(cwd, "conf")
    if (conf.isDirectory)
      from(env)
    else {
      val parent = new File(cwd.getParentFile, "conf")
      if (parent.isDirectory)
        from (env.copy(rootPath = cwd.getParentFile))
      else
        from(ConfigFactory.load())
    }
  }
}

