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

import io.github.nremond.PBKDF2
import org.mindrot.jbcrypt._
import com.lambdaworks.crypto.SCrypt
import scala.annotation.tailrec
import java.security.SecureRandom
import play.api.Logger

/**
 * A trait that abstractly defines what it means to be a Hahser
 */
trait Hasher extends Registrable {

  /**
   * A class to define the result of hashing a string
   *
   * @param hasher the unique name of the hasher used to hash this string
   * @param encoded the encoded result of hashing the original plaintext string
   * @param salt the optional salt value that was used when hashing
   */
  case class HashingResult(hasher: Symbol, encoded: String, salt: String, complexity: Long = 0)

  /**
   * Hash a string and return it
   *
   * @param plainText the text to hash
   * @return a Result containing the hash encoded text, algorithm name, and salt used
   */
  def hash(plainText: String, salt: Option[String] = None, complexity: Option[Long] = None): HashingResult

  /**
   * Checks whether a supplied password matches the hashed one. This implementation works for Hashers whose matching
   * algorithm can be obtained from its hashing algorithm. This one just compares the two outputs for equality.
   * Hasher subclasses can override this method, but they should use the supplied fullEqualityComparison method to do
   * their string comparisons.
   *
   * @param result the password retrieved from the backing store (by means of UserService)
   * @param suppliedPassword the password supplied by the user trying to log in
   * @return true if the password matches, false otherwise.
   */
  def matches(result: Hasher#HashingResult, suppliedPassword: String): Boolean = {
    (result.hasher == id) && {
      val attempt = hash(suppliedPassword, Some(result.salt), Some(result.complexity))
      attempt.hasher == result.hasher &&
      fullEqualityComparison(attempt.salt, result.salt) &&
      fullEqualityComparison(attempt.encoded, result.encoded)
    }
  }

  /**
   * A utility for subclasses to compute a time based complexity factor for the hashing algorithm. This complexity
   * factor can be used by the hashing algorithm to alter the way in which the algorithm proceeds or how many times it
   * iterates or how hard it tries to compute the hash. Higher values imply a more complex result. This funciton
   * computes a time based complexity such that the initialComplexity is doubled every 18 months, ala Moore's Law.
   * @param initialComplexity - The initial value for the complexity, typically the minimum or default value
   * @param initialTime - The time at which the initialComplexity was established
   * @return
   */
  protected def timeBasedComplexity(initialComplexity : Long, initialTime: Long) : Long = {
    val oneYear : Long = 31556926   // in seconds
    val oneMonth : Long = 2629743   // in seconds
    val eighteenMonths : Long = oneYear + (6 * oneMonth) // in seconds
    val incrementRate : Long = eighteenMonths / initialComplexity // rate at which we double complexity, in seconds
    val elapsedSeconds : Long = (java.lang.System.currentTimeMillis() - initialTime) / 1000

    Logger.debug("incrementRate=" + incrementRate + ", elapsedSeconds=" + elapsedSeconds +
                 ", increment=" + (elapsedSeconds/incrementRate))

    // Return the initialComplexity but augment it based on the current time and the rate of augmentation
    initialComplexity + (elapsedSeconds / incrementRate)
  }


  /**
   * When comparing strings, it is important to have the comparison run in constant time despite the length of the
   * strings and whether they match or not. This prevents attacks that are based on timing. We compare all elements
   * of the strings so that strings of the same length always take the same amount of processing, and consequently the
   * same amount of time.
   * @param a - First string to compare
   * @param b - Second string to compare
   * @return
   */
  protected def fullEqualityComparison(a: String, b: String) : Boolean = {

    /**
     * Define a tail recursive function to iterate over all the elements of the string and compute the sum
     * of the XOR of the character pairs. If the strings are equal, this will yield a zero result at the end. If not,
     * then the strings are not equal. Note that this algorithm depends on a and b being the same length to avoid
     * checking both a and b for being isEmpty.
     * @param a - First string to compare
     * @param b - Second string to compare
     * @param result - The ongoing computed result of the recursion
     */
    @tailrec
    def compute(a: String, b: String, result: Int = 0) : Int = {
      if (a.isEmpty)
        result
      else
        compute(a.tail, b.tail, result | a.head ^ b.head)
    }

    // The strings are equal if they have the same length, and the sum of the XOR computation is zero
    (a.length == b.length) && compute(a, b) == 0
  }
}

object HasherKinds extends Enumeration {
  type HasherKinds = Value
  val PBKDF2 = Value
  val BCrypt = Value
  val SCrypt = Value
}
/**
 * A hasher based on the PBKDF2 algorithm by Nicolas Rémond
 */
object PBKDF2Hasher extends Hasher {

  // Provide the name of this hasher
  override val id = 'PBKDF2

  def defaultIterations = if ( Hash.fastMode ) 12500 else 25000


  override def hash(plainText: String, saltine: Option[String], complexity: Option[Long]) = {
    // Let's use a 512 bit key, just cause we can and it'll make the cracker's job harder
    val keyLengthInBytes : Int = 64
    val baseIterations : Long = defaultIterations // Recommended minimum for Intel i5 @ 2.6GHz in 2013
    val baseTime : Long = 1356998400000L // January 1, 2013 00:00:00
    val iterations = complexity.getOrElse(timeBasedComplexity(baseIterations, baseTime))
    val salt = saltine.getOrElse( Hash.salt )
    val encrypted = PBKDF2(plainText.getBytes, salt.getBytes, iterations.toInt, keyLengthInBytes)
    HashingResult(id, new String(encrypted), salt, iterations)
  }
}

/**
 * A hasher based on the BCrypt algorithm
 */
object BCryptHasher extends Hasher {

  // Provide the name of this hasher
  override val id = 'BCrypt

  def defaultRounds = if ( Hash.fastMode ) 4 else 10

  // Set up some parameterization of the bcrypt algorithm
  override def hash(plainText: String, saltine: Option[String], complexity: Option[Long]) = {
    val initialTime = 1265000400L   // Feb 1, 2010 - when jBCrypt 0.3 was released
    val initialRounds = Math.pow(2,defaultRounds).toLong
    // Default log_rounds is 10, we compute 2**10 here for linear time-based scaling,
    val newComplexity = timeBasedComplexity(initialRounds, initialTime)
    val log_rounds : Long = complexity.getOrElse(MathHelpers.log2(newComplexity))
    val salt = saltine.getOrElse(BCrypt.gensalt(log_rounds.toInt, Hash.getSecureRandom()))
    HashingResult(id, BCrypt.hashpw(plainText, salt), salt, log_rounds)
  }
}

/**
 * A hasher based on the SCrypt algorithm
 */
object SCryptHasher extends Hasher {

  override val id = 'SCrypt

  def default_args = if (Hash.fastMode)  (10000 << 16) | (1 << 8) | 1  else (1024 << 16) | (8 << 8) | 1

  override def hash(plainText: String, saltine: Option[String], complexity: Option[Long]) = {
    val dkLen = 64
    val salt = saltine.getOrElse( Hash.salt )
    val args : Long = complexity.getOrElse( default_args )
    val N : Int  = (args >> 16).toInt
    val r : Int = ((args & (0x0FFFF)) >> 8).toInt
    val p : Int = (args & 0x00FF).toInt
    val result : String = new String( SCrypt.scrypt(plainText.getBytes(), salt.getBytes(), N, r, p, dkLen) )
    HashingResult(id, result, salt, args)
  }
}

/**
 * The interface to the Hash functions.
 */
object Hash extends Registry[Hasher] {
  override val registryName = "Hashers"
  override val registrantsName = "hasher"

  var fastMode : Boolean = false // Allow making testing go faster by setting this to true

  register(PBKDF2Hasher)
  register(BCryptHasher)
  register(SCryptHasher)

  def apply(plainText : String) : Hasher#HashingResult = {
    val hasher : Hasher = pick
    hasher.hash(plainText)
  }

  def check(result : Hasher#HashingResult, attempt: String) = {
    val hasher : Option[Hasher] = getRegistrant(result.hasher)
    require(hasher.isDefined)
    hasher.get.matches(result, attempt)
  }

  val keyLength = 64

  def getSecureRandom() : SecureRandom = SecureRandom.getInstance("SHA1PRNG", "SUN")

  def salt = new String(getSecureRandom().generateSeed(keyLength))
}

