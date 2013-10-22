/**********************************************************************************************************************
 * This file is part of Scrupal a Web Application Framework.                                                          *
 *                                                                                                                    *
 * Copyright (c) 2013, Reid Spencer and viritude llc. All Rights Reserved.                                            *
 *                                                                                                                    *
 * Scrupal is free software: you can redistribute it and/or modify it under the terms                                 *
 * of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License,   *
 * or (at your option) any later version.                                                                             *
 *                                                                                                                    *
 * Scrupal is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied      *
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more      *
 * details.                                                                                                           *
 *                                                                                                                    *
 * You should have received a copy of the GNU General Public License along with Scrupal. If not, see either:          *
 * http://www.gnu.org/licenses or http://opensource.org/licenses/GPL-3.0.                                             *
 **********************************************************************************************************************/

package scrupal.utils

import play.api.{Logger, Configuration}
import scala.util.Try

/**
 * Provide some extentions to the Play Configuration class via the pimp-my-library pattern
 * Further description here.
 */
class ConfigHelper(config : Configuration) {

  import ClassHelpers._

  /** Convert any class name into an instance of that class, assuming it has an empty args constructor
    *
    * @param name The class name
    * @param m A manifest for the class
    * @tparam C The kind of class expected, a base class
    * @return An instance of C that is of class `name` or None if it couldn't be instantiated.
    */
  def getInstance[C<:AnyRef](name: String)(implicit m: Manifest[C]) : Option[C] = {
    try
    {
      Option( string2instance[C](name))
    }
    catch {
      case x: IllegalAccessException =>  { Logger.error("Cannot access class " + name + " while instantiating: ", x); None }
      case x: InstantiationException =>  { Logger.error("Cannot instantiate uninstantiable class " + name + ": ", x); None }
      case x: ExceptionInInitializerError =>  { Logger.error("Instance initialization of " + name + " failed: ", x); None }
      case x: SecurityException => { Logger.error("Security exception while instantiating " + name + ": ", x);  None }
      case x: LinkageError => { Logger.error("Linkage error while instantiating " + name + ": ", x); None }
      case x: ClassNotFoundException =>  { Logger.error("Cannot find class " + name + " to instantiate: ", x); None }
      case x: Throwable  => { throw x }
    }
  }

  def validateDBs : Try[Set[String]] = {
    Try {
      forEachDB { (site: String, site_config: Configuration ) =>
        val keys: Set[String] = site_config.subKeys
        // Whatever keys are there they must all be strings so validate that (getString will throw if its not a string)
        // and make sure they didn't provide a key with an empty value, also
        for ( key <- keys ) yield if (site_config.getString(key).getOrElse {
          throw new Exception("Configuration for '" + site + "' is missing a value for '" + key + "'.")
        }.isEmpty) { throw new Exception("Configuration for '" + site + "' has an empty value for '" + key + "'.") }
        // The config needs to at least have a url key
        if (!keys.contains("url")) {
          throw new Exception("Configuration for '" + site + "' must specify a value for 'url' key, at least.")
        } else if (site_config.getString("url").get.equals("jdbc:h2:mem:")) {
          throw new Exception("Configuration for '" + site + "' must not use a private memory-only database")
        }
        // Okay, looks good, return the site name
        site
      }
    }
  }

  def forEachDB[FOO](f: (String, Configuration) => FOO ) : Set[FOO] = {

    // First, unpack the "db" configuration which is standardized by play
    val dbs_o: Option[Configuration] = config.getConfig("db")
    val dbs = dbs_o.getOrElse(Configuration.empty)
    val site_names: Set[String] = dbs.subKeys

    // Now map the site names to the config objects and then convert with the caller's function
    for ( site:String <- site_names )
    yield (f(site, dbs.getConfig(site).getOrElse(Configuration.empty)))
  }
}

object ConfigHelper
{
  implicit def helpYoConfig(config: Configuration) = new ConfigHelper(config)
  def apply(config: Configuration) = helpYoConfig(config)
}
