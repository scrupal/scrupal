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

package scrupal.storage.impl

import java.io.File

import org.specs2.mutable.Specification
import play.api.Configuration

/** Test Suite for the ConfigHelper class  */
class StorageConfigHelperSpec extends Specification {

  def makeTestConfig(file : String) = Configuration.from(
    Map(StorageConfigHelper.scrupal_storage_config_file_key -> ("scrupal-storage/src/test/resources/storage/config/" + file))
  )

  "ConfigHelper" should {
    "Correctly extract database configuration from default.conf file" in {
      val helper = StorageConfigHelper(makeTestConfig("default.conf"))
      val conf = helper.getStorageConfig
      conf.getConfig("storage").isDefined must beTrue
      conf.getString("storage.default.uri").isDefined must beTrue
      conf.getString("storage.default.uri").get must beEqualTo("scrupal-mem://localhost/scrupal")
    }

    "Correctly extract empty config from empty_conf.conf" in {
      val helper = StorageConfigHelper(makeTestConfig("empty_conf.conf"))
      helper.getStorageConfig.getConfig("storage").isDefined must beFalse
    }

    "Reflect get/set/get for valid.conf" in {
      val helper = StorageConfigHelper(makeTestConfig("valid.conf"))
      val tmpFile = File.createTempFile("scrupal", ".conf")
      tmpFile.deleteOnExit()
      val get1 = helper.getStorageConfig
      helper.setStorageConfig(get1, Some(tmpFile))
      val helper2 = StorageConfigHelper(Configuration.from(
        Map(StorageConfigHelper.scrupal_storage_config_file_key -> tmpFile.getCanonicalPath))
      )
      val get2 = helper2.getStorageConfig
      get1 must beEqualTo(get2)
    }
  }

  "ConfigHelper.forEachDB" should {
    "Not iterate on empty_conf.conf" in {
      val helper = StorageConfigHelper(makeTestConfig("empty_conf.conf"))
      val result = helper.forEachStorage {
        case (name : String, config : Configuration) ⇒
          println("You should never see this!")
          failure
          false
      }
      result.size must beEqualTo(0)
    }

    "Find only one value in valid.conf" in {
      val helper = StorageConfigHelper(makeTestConfig("valid.conf"))
      val result = helper.forEachStorage {
        case (name : String, config : Configuration) ⇒
          println("You should see this only once!")
          true
      }
      result.size must beEqualTo(1)
    }

    "Find multiple values in multiple.conf" in {
      val helper = StorageConfigHelper(makeTestConfig("multiple.conf"))
      val result = helper.forEachStorage {
        case (name : String, config : Configuration) ⇒
          true
      }
      result.size must beGreaterThan(1)
    }

    "Not return configurations when function returns false" in {
      val helper = StorageConfigHelper(makeTestConfig("multiple.conf"))
      val result = helper.forEachStorage {
        case (name : String, config : Configuration) ⇒
          false
      }
      result.size must beEqualTo(2)
      result.count { case (name : String, config : Option[Configuration]) ⇒ config.isDefined } must beEqualTo(0)
    }
  }

  "ConfigHelper.validateDBConfiguration" should {
    "Return Failure(x) for empty configuration" in {
      val helper = StorageConfigHelper(makeTestConfig("empty_conf.conf"))
      val result = helper.validateStorageConfig
      result must beAFailedTry
    }

    "Return Failure(x) for default configuration" in {
      val helper = StorageConfigHelper(makeTestConfig("default.conf"))
      val result = helper.validateStorageConfig
      result must beAFailedTry
    }

    "Return Failure(x) for bad configuration" in {
      val helper = StorageConfigHelper(makeTestConfig("bad.conf"))
      val result = helper.validateStorageConfig
      result must beAFailedTry
    }

    "Return Success(x) for valid configuration" in {
      val helper = StorageConfigHelper(makeTestConfig("valid.conf"))
      val result = helper.validateStorageConfig
      result must beASuccessfulTry
    }
  }
}
