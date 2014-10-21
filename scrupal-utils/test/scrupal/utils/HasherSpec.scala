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

import grizzled.slf4j.Logger
import org.specs2.mutable.Specification

import java.security.SecureRandom

/**
 * One line sentence description here.
 * Further description here.
 */
class HasherSpec extends Specification {

  val password = new String(SecureRandom.getInstance("SHA1PRNG", "SUN").generateSeed(16))

  val log = Logger("HasherSpec")

  val h1 = new PBKDF2Hasher
  val h2 = new SCryptHasher
  val h3 = new BCryptHasher

  "Hasher" should {
    "pick random hashers" in {
      val list : Seq[Symbol] = for ( i <- 1 to 100 ) yield Hash.pick.id
      list.distinct.size >= 2 must beTrue
    }

    "Right & Wrong Password Guesses Take the Same Time To Compute" in {
      h1.inFastMode { hasher ⇒
        val result = hasher.hash(password)
        val t1 = System.currentTimeMillis()
        val check1 = Hash.check(result, password)
        val t2 = System.currentTimeMillis()
        val check2 = Hash.check(result, "a")
        val t3 = System.currentTimeMillis()
        log.debug("t1=" + t1 + ", t2=" + t2 + ", t3=" + t3 + ", t2-t1=" + (t2 - t1) + ", t3-t2=" + (t3 - t2))
        val avg = ((t2 - t1) + (t3 - t2)) / 2
        val delta = Math.abs((t2 - t1) - (t3 - t2))
        val ratio = delta.toDouble / avg.toDouble
        log.debug("avg=" + avg + ", delta=" + delta + ", ratio=" + ratio)
        ratio must beLessThan(0.10) // Less than 10% difference in timing
      }
    }

    "PBKDF2 works " in {
      h1.inFastMode { hasher ⇒
        val start = System.currentTimeMillis()
        val result = hasher.hash(password)
        val check = Hash.check(result, password) must beTrue
        log.debug("PBKDF2: " + (System.currentTimeMillis() - start) + " milliseconds.")
        check
      }
    }

    "BCrypt works" in {
      h2.inFastMode { hasher ⇒
        val start = System.currentTimeMillis()
        val result = hasher.hash(password)
        val check = Hash.check(result, password) must beTrue
        log.debug("BCrypt: " + (System.currentTimeMillis() - start) + " milliseconds.")
        check
      }
    }

    "SCrypt works" in {
      h3.inFastMode { hasher ⇒
        val start = System.currentTimeMillis()
        val result = hasher.hash(password)
        val check = Hash.check(result, password) must beTrue
        log.debug("SCrypt: " + (System.currentTimeMillis() - start) + " milliseconds.")
        check
      }
    }
  }
}