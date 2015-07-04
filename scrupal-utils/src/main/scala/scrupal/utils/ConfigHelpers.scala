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

package scrupal.utils

import java.io.File

import com.typesafe.config._
import play.api.{ Environment, Configuration }

import scala.collection.immutable.TreeMap
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
  }

  def from(env : Environment) = {
    Configuration.load(env)
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
    Configuration.load(Environment.simple())
  }
}

