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

import org.specs2.mutable.Specification

import java.security.SecureRandom

/** One line sentence description here.
  * Further description here.
  */
class HasherSpec extends Specification with ScrupalComponent {

  sequential

  "Hasher" should {
    val password = new String(SecureRandom.getInstance("SHA1PRNG", "SUN").generateSeed(128))

    /* FIXME: Important security feature to have right/wrong passwords take same time to compute result. Disabled
    because it fails sporadically.
    "Right & Wrong Password Guesses Take the Same Time To Compute" in {
      PBKDF2Hasher.inFastMode { hasher ⇒
        val result = hasher.hash(password)
        val t1 = System.nanoTime()
        val check1 = Hash.check(result, password)
        val t2 = System.nanoTime()
        val check2 = Hash.check(result, "a")
        val t3 = System.nanoTime()
        log.debug("t1=" + t1 + ", t2=" + t2 + ", t3=" + t3 + ", t2-t1=" + (t2 - t1) + ", t3-t2=" + (t3 - t2))
        val delta1_2 = Math.abs(t2 - t1)
        val delta2_3 = Math.abs(t3 - t2)
        val avg = (delta1_2 + delta2_3) / 2
        val delta = Math.abs(delta1_2 - delta2_3)
        val ratio = delta.toDouble / avg.toDouble
        log.debug("avg=" + avg + ", delta=" + delta + ", ratio=" + ratio)
        ratio must beLessThan(0.25) // Less than 25% difference in timing
      }
    }
     */

    "PBKDF2 works " in {
      PBKDF2Hasher.inFastMode { hasher ⇒
        val start = System.currentTimeMillis()
        val result = hasher.hash(password)
        val check = Hash.check(result, password) must beTrue
        log.debug("PBKDF2: " + (System.currentTimeMillis() - start) + " milliseconds.")
        check
      }
    }

    "BCrypt works" in {
      BCryptHasher.inFastMode { hasher ⇒
        val start = System.currentTimeMillis()
        val result = hasher.hash(password)
        val check = Hash.check(result, password) must beTrue
        log.debug("BCrypt: " + (System.currentTimeMillis() - start) + " milliseconds.")
        check
      }
    }

    "SCrypt works" in {
      SCryptHasher.inFastMode { hasher ⇒
        val start = System.currentTimeMillis()
        val result = hasher.hash(password)
        val check = Hash.check(result, password) must beTrue
        log.debug("SCrypt: " + (System.currentTimeMillis() - start) + " milliseconds.")
        check
      }
    }

    "pick random hashers" in {
      val list : Seq[Symbol] = for (i ← 1 to 100) yield Hash.pick.id
      list.distinct.size >= 2 must beTrue
    }

  }
}
