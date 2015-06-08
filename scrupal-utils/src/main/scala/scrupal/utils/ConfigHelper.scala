/** ********************************************************************************************************************
  * This file is part of Scrupal, a Scalable Reactive Content Management System.                                       *
  *                                                                                                                 *
  * Copyright © 2015 Reactific Software LLC                                                                            *
  *                                                                                                                 *
  * Licensed under the Apache License, Version 2.0 (the "License");  you may not use this file                         *
  * except in compliance with the License. You may obtain a copy of the License at                                     *
  *                                                                                                                 *
  *     http://www.apache.org/licenses/LICENSE-2.0                                                                  *
  *                                                                                                                 *
  * Unless required by applicable law or agreed to in writing, software distributed under the                          *
  * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,                          *
  * either express or implied. See the License for the specific language governing permissions                         *
  * and limitations under the License.                                                                                 *
  * ********************************************************************************************************************
  */

package scrupal.utils

import java.io.{ File, PrintWriter }

import com.typesafe.config.{ ConfigFactory, ConfigRenderOptions }

import scala.language.implicitConversions
import scala.util.Try

/** Provide some extensions to the Configuration class via the pimp-my-library pattern
  */
class ConfigHelper(config : Configuration) extends ScrupalComponent {

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

  type DBConfig = Map[String, Option[Configuration]]
  val emptyDBConfig = Map.empty[String, Option[Configuration]]

  def forEachDB(f : (String, Configuration) ⇒ Boolean) : DBConfig = {
    val db_config = getDbConfig
    val root_config = db_config.getConfig("db")
    root_config map { rootConfig : Configuration ⇒ internalForEach(rootConfig)(f) }
  }.getOrElse(emptyDBConfig)

  private def internalForEach(rootConfig : Configuration)(f : (String, Configuration) ⇒ Boolean) : DBConfig = {
    for (dbName ← rootConfig.subKeys) yield {
      val dbConf = rootConfig.getConfig(dbName)
      val resolvedConf = dbConf.getOrElse(Configuration.empty)
      if (f(dbName, resolvedConf)) (dbName, dbConf) else (dbName, None)
    }
  }.toMap

  def getDbConfigFile : Option[File] = {
    config.getString(ConfigHelper.scrupal_storage_config_file_key) map { db_config_file_name : String ⇒
      new File(db_config_file_name)
    }
  }

  def getDbConfig : Configuration = Configuration (
    {
      getDbConfigFile map { db_config_file : File ⇒
        if (db_config_file.isFile) {
          ConfigFactory.parseFile(db_config_file)
        } else {
          ConfigFactory.empty
        }
      }
    }.getOrElse(ConfigFactory.empty)
  )

  def setDbConfig(new_config : Configuration, writeTo : Option[File] = None) : Configuration = {
    val result = {
      val data : String = new_config.underlying.root.render (ConfigRenderOptions.concise()) // whew!
      val trimmed_data = data.substring(1, data.length - 1)
      writeTo.orElse(getDbConfigFile) map { db_config_file : File ⇒
        val writer = new PrintWriter(db_config_file)
        try { writer.println(trimmed_data) } finally { writer.close() }
        new_config
      }
    }.getOrElse(Configuration.empty)
    log.debug("DB Config set to " + result)
    result
  }

  def addDbConfig(db_config : Configuration) : Configuration = {
    setDbConfig(getDbConfig ++ db_config)
  }

  def setDbConfig(new_config : Map[String, AnyRef]) : Configuration = {
    import scala.collection.JavaConversions._
    val j_config : java.util.Map[String, AnyRef] = mapAsJavaMap(new_config)
    val cfg = Configuration(ConfigFactory.parseMap(j_config))
    setDbConfig(cfg)
  }

  def validateDBConfiguration : Try[Map[String, Option[Configuration]]] = {
    Try {
      val cfg = getDbConfig
      if (cfg.keys.size == 0)
        throw new Exception("The database configuration is completely empty.")
      val db_cfg = cfg.getConfig("db");
      {
        db_cfg map { the_config : Configuration ⇒
          if (the_config.getConfig("default").isDefined)
            throw new Throwable("The initial, default database configuration was detected.")
          internalForEach(db_cfg.get) { (db : String, db_config : Configuration) ⇒
            val keys : Set[String] = db_config.subKeys
            // Whatever keys are there they must all be strings so validate that (getString will throw if its not a string)
            // and make sure they didn't provide a key with an empty value, also
            for (key ← keys) yield if (db_config.getString(key).getOrElse {
              throw new Exception("Configuration for '" + db + "' is missing a value for '" + key + "'.")
            }.isEmpty) { throw new Exception("Configuration for '" + db + "' has an empty value for '" + key + "'.") }
            // The config needs to at least have a uri key
            if (!keys.contains("uri")) {
              throw new Exception("Configuration for '" + db + "' must specify a value for 'uri' key, at least.")
            } else if (!db_config.getString("uri").get.startsWith("mongodb://")) {
              throw new Exception("Configuration for '" + db + "' must have a mongodb URI")
            }
            // Okay, looks good, include this in the results
            true
          }
        }
      }.getOrElse({ throw new Exception("The database configuration does not contain a top level 'db' key.") })
    }
  }
}

object ConfigHelper {
  implicit def helpYoConfig(config : Configuration) : ConfigHelper = new ConfigHelper(config)
  def apply(config : Configuration) = helpYoConfig(config)

  // The configuration key that says where to get the database configuration data.
  val scrupal_storage_config_file_key = "scrupal.storage.config.file"

}
