/** ********************************************************************************************************************
  * Copyright © 2014 Reactific Software, Inc.                                                                          *
  *                                                                                                                 *
  * This file is part of Scrupal, an Opinionated Web Application Framework.                                            *
  *                                                                                                                 *
  * Scrupal is free software: you can redistribute it and/or modify it under the terms                                 *
  * of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License,   *
  * or (at your option) any later version.                                                                             *
  *                                                                                                                 *
  * Scrupal is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied      *
  * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more      *
  * details.                                                                                                           *
  *                                                                                                                 *
  * You should have received a copy of the GNU General Public License along with Scrupal. If not, see either:          *
  * http://www.gnu.org/licenses or http://opensource.org/licenses/GPL-3.0.                                             *
  * ********************************************************************************************************************
  */

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
