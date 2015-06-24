/** ********************************************************************************************************************
  * This file is part of Scrupal, a Scalable Reactive Content Management System.                                       *
  *                                                                                                       *
  * Copyright © 2015 Reactific Software LLC                                                                            *
  *                                                                                                       *
  * Licensed under the Apache License, Version 2.0 (the "License");  you may not use this file                         *
  * except in compliance with the License. You may obtain a copy of the License at                                     *
  *                                                                                                       *
  * http://www.apache.org/licenses/LICENSE-2.0                                                                  *
  *                                                                                                       *
  * Unless required by applicable law or agreed to in writing, software distributed under the                          *
  * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,                          *
  * either express or implied. See the License for the specific language governing permissions                         *
  * and limitations under the License.                                                                                 *
  * ********************************************************************************************************************
  */

package scrupal.storage.impl

import java.io.{ PrintWriter, File }

import com.typesafe.config.{ ConfigRenderOptions, ConfigFactory }
import play.api.Configuration
import scrupal.utils.ScrupalComponent

import scala.util.Try

/** Extend the Configuration class via the pimp-my-library pattern
  */
class StorageConfigHelper(config : Configuration) extends ScrupalComponent {

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

  type StorageConfig = Map[String, Option[Configuration]]
  val emptyStorageConfig = Map.empty[String, Option[Configuration]]

  def forEachStorage(f : (String, Configuration) ⇒ Boolean) : StorageConfig = {
    val config = getStorageConfig
    val root_config = config.getConfig("storage")
    root_config map { rootConfig : Configuration ⇒ internalForEach(rootConfig)(f) }
  }.getOrElse(emptyStorageConfig)

  private def internalForEach(rootConfig : Configuration)(f : (String, Configuration) ⇒ Boolean) : StorageConfig = {
    for (storageName ← rootConfig.subKeys) yield {
      val storageConf = rootConfig.getConfig(storageName)
      val resolvedConf = storageConf.getOrElse(Configuration.empty)
      if (f(storageName, resolvedConf)) (storageName, storageConf) else (storageName, None)
    }
  }.toMap

  def getStorageConfigFile : Option[File] = {
    config.getString(StorageConfigHelper.scrupal_storage_config_file_key) map { storage_config_file_name : String ⇒
      new File(storage_config_file_name)
    }
  }

  def getStorageConfig : Configuration = Configuration (
    {
      getStorageConfigFile map { storage_config_file : File ⇒
        if (storage_config_file.isFile) {
          ConfigFactory.parseFile(storage_config_file)
        } else {
          ConfigFactory.empty
        }
      }
    }.getOrElse(ConfigFactory.empty)
  )

  def setStorageConfig(new_config : Configuration, writeTo : Option[File] = None) : Configuration = {
    val result = {
      val data : String = new_config.underlying.root.render (ConfigRenderOptions.concise()) // whew!
      val trimmed_data = data.substring(1, data.length - 1)
      writeTo.orElse(getStorageConfigFile) map { storage_config_file : File ⇒
        val writer = new PrintWriter(storage_config_file)
        try { writer.println(trimmed_data) } finally { writer.close() }
        new_config
      }
    }.getOrElse(Configuration.empty)
    log.debug("Storage Config set to " + result)
    result
  }

  def setStorageConfig(new_config : Map[String, AnyRef]) : Configuration = {
    import scala.collection.JavaConversions._
    val j_config : java.util.Map[String, AnyRef] = mapAsJavaMap(new_config)
    val cfg = Configuration(ConfigFactory.parseMap(j_config))
    setStorageConfig(cfg)
  }

  def addStorageConfig(storage_config : Configuration) : Configuration = {
    setStorageConfig(getStorageConfig ++ storage_config)
  }

  def validateStorageConfig : Try[Map[String, Option[Configuration]]] = {
    Try {
      val cfg = getStorageConfig
      if (cfg.keys.size == 0)
        throw new Exception("The storage configuration is completely empty.")
      val storage_cfg = cfg.getConfig("storage");
      {
        storage_cfg map { the_config : Configuration ⇒
          if (the_config.getConfig("default").isDefined)
            throw new Throwable("The initial, default database configuration was detected.")
          internalForEach(storage_cfg.get) { (storage_name : String, storage_config : Configuration) ⇒
            val keys : Set[String] = storage_config.subKeys
            // Whatever keys are there they must all be strings so validate that (getString will throw if its not a string)
            // and make sure they didn't provide a key with an empty value, also
            for (key ← keys) yield if (storage_config.getString(key).getOrElse {
              throw new Exception("Configuration for '" + storage_name + "' is missing a value for '" + key + "'.")
            }.isEmpty) { throw new Exception("Configuration for '" + storage_name + "' has an empty value for '" + key + "'.") }
            // The config needs to at least have a uri key
            if (!keys.contains("uri")) {
              throw new Exception("Configuration for '" + storage_name + "' must specify a value for 'uri' key, at least.")
            } else if (!storage_config.getString("uri").get.startsWith("scrupal-")) {
              throw new Exception("Configuration for '" + storage_name + "' must have a scrupal URI scheme")
            }
            // Okay, looks good, include this in the results
            true
          }
        }
      }.getOrElse({ throw new Exception("The storage configuration does not contain a top level 'storage' key.") })
    }
  }
}

object StorageConfigHelper {
  import scala.language.implicitConversions
  implicit def helpYoConfig(config : Configuration) : StorageConfigHelper = new StorageConfigHelper(config)
  def apply(config : Configuration) = helpYoConfig(config)

  // The configuration key that says where to get the database configuration data.
  val scrupal_storage_config_file_key = "scrupal.storage.config.file"

  def fromConfigFile(localPath: String) : StorageConfigHelper = {
    val params = Map(scrupal_storage_config_file_key → localPath)
    val config = Configuration.from(params)
    StorageConfigHelper(config)
  }

}
