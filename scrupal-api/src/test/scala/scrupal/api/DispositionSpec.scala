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

import org.specs2.mutable.Specification

/** Test Suite for Disposition class */
class DispositionSpec extends Specification {

  "Successful" should {
    "use positive values for success" in {
      Successful.code must beGreaterThan(0)
    }
    "be successful" in {
      Successful.isSuccessful must beTrue
    }
  }

  "Indeterminate" should {
    "not be successful" in {
      Indeterminate.isSuccessful must beFalse
    }
    "not be failure" in {
      Indeterminate.isFailure must beFalse
    }
  }
}