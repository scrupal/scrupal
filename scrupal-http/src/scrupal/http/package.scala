/**********************************************************************************************************************
 * Copyright © 2014 Reactific Software, Inc.                                                                          *
 *                                                                                                                    *
 * This file is part of Scrupal, an Opinionated Web Application Framework.                                            *
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

package scrupal

/** Scrupal HTTP Interface Project
  * The purpose of scrupal-http is to interface the core abstractions of Scrupal with the web. Because Scrupal uses
  * a very regular information structure, we don't need a fully generalized web processing mechanism. Instead, this
  * module adapts itself to the sites, entities, and modules that have been defined by the user and dynamically
  * arranges for the corresponding web interface to be constructed. Note that this library provides mechanism but
  * not content. This is where the request routing is performed and the vagaries of http processing are hidden.
  * Users of this library simply register the Scrupal entities and provide the responses necessary.
 */
package object http {

}
