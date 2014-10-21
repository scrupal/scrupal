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

/** Version numbering for Scrupal Modules.
  * Versions are ordered by major and minor number; update is not factored into the ordering as it is intended to be
  * used only for those changes that do not add features but just fix bugs. Consequently if major and minor version
  * number are the same, the two versions are considered equivalent. It is important for module developers to use the
  * version numbering scheme correctly so that incompatibilities between modules are not created. In particular, any
  * time a new version of a module alters or removes functionality, the major version number must be incremented. Also,
  * whenever a new feature is added, not previously available in prior releases, the minor version number must be
  * incremented as long as no functionality has been removed or changed (major change). If a change is neither major (
  * breaks backwards compatibility) nor minor (retains backwards compatibility but adds features), then it is simply an
  * update that fixes a bug or otherwise improves a stable release so only the update number need be incremented.
  * @param major The major version number that identifies a release that provides new major features and breaks
  *              backwards compatibility with prior releases either by changing functionality or removing functionality.
  * @param minor The minor version number that identifies a release that maintains backwards compatibility with
  *              prior releases but provides additional features.
  * @param update The update version number that identifies the fix/patch level without introducing new features
  *               nor breaking backwards compatibility.
  */
case class Version(major : Int, minor : Int, update: Int) extends Ordered[Version] {

/** Compare to Version instances
    * Comparison only uses the minor and major numbers because updates are considered to be bug fixes that do not
    * provide new functionality but just correct existing functionality. Consequently versions with the same major
    * and minor numbers will yield 0 from this function
    * @param that The Version to compare `this` to
    * @return <0 if `this` < `that`, 0 if they are equal, > 0 if `this` > `that`
    */

  override def compare(that: Version) : Int = {
    if (this.major != that.major)
      this.major - that.major
    else
      this.minor - that.minor
  }

  /** The usual equals operator
   * @param other The other object to compare against
   * @return true iff `other` is equal to `this`
   */
  override def equals(other: Any) = {
    other match {
      case v: Version =>
        val that = other.asInstanceOf[Version]
        (this.major == that.major) && (this.minor == that.minor)
      case _ =>
        false
    }
  }

  /** Compute hashcode for versions, not that we'll need it much.
    * Since we don't expect major version numbers to be very large, just retain their low order bits and shift them
    * up into the high order bits of the result. We reserve most of the bits for the minor number
    * @return integer hashcode for Versions
    */
  override def hashCode : Int = major << 24 + minor << 8 + update

  /** Generate the typical dot notation for versions */
  override def toString = major + "." + minor + "." + update
}
