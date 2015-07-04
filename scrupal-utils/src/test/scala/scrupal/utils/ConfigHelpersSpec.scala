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

import com.typesafe.config.{ConfigValue, ConfigFactory}
import org.specs2.mutable.Specification

class ConfigHelpersSpec extends Specification {

  "ConfigHelpers" should {
    "provide a default Configuration" in {
      val config = ConfigHelpers.default()
      config.getInt("scrupal.utils.test_value").getOrElse(0) must beEqualTo(42)
    }
    "provide a config from an underlying one" in {
      val cfg = ConfigFactory.parseString("scrupal.foo = 42")
      val config = ConfigHelpers.from(cfg)
      config.getInt("scrupal.foo").getOrElse(0) must beEqualTo(42)
    }
    "can filter interest config" in {
      import ConfigHelpers._
      val config = ConfigHelpers.default()
      config.getString("java.version").nonEmpty must beTrue
      val ifc = config.interestingFlatConfig
      ifc.exists { case (key: String ,v: ConfigValue) ⇒ key.startsWith("java") } must beFalse
    }
  }
}
