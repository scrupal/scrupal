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

package scrupal.models

import scala.util.matching.Regex

/** One line sentence description here.
  * Further description here.
  */
object Patterns {
  /** Join a sequence of patterns together */
  def join(r: Regex*) : Regex = {
    val result: StringBuffer = new StringBuffer(1024)
    r.foldLeft(result){
      case (result:StringBuffer, regex:Regex) => result.append(regex.unanchored.pattern.pattern); result
    }.toString().r
  }

  /** Make a pattern into various repeating forms of itself  */
  def anchored(r:Regex) = ("^" + r.pattern.pattern + "$").r
  def group(r:Regex) = ("(?:" + r.pattern.pattern + ")").r
  def capture(r:Regex) = ("(" + r.pattern.pattern + ")").r
  def optional(r: Regex)  = ("(?:" + r.pattern.pattern + ")?").r
  def atLeastOne(r:Regex) = ("(?:" + r.pattern.pattern + ")+").r
  def zeroOrMore(r:Regex) = ("(?:" + r.pattern.pattern + ")*").r
  def alternate(r1:Regex, r2:Regex) = ("(?:" + r1.pattern.pattern + ")|(?:" + r2.pattern.pattern + ")").r

  // From RFC 2822. The pattern names below match the grammar production names and definitions from the RFC
  val atext = "[-A-Za-z0-9!#$%&'*+/=?^_`|~]".r
  val atom = atLeastOne(atext)
  val dot_atom = join(atom, zeroOrMore(join("[.]".r,atom)))
  val no_ws_ctl = "[\\u0001-\\u0008\\u000B-\\u000C\\u000E-\\u001F\\u007F]".r
  val qtext = alternate(no_ws_ctl, "[!\\u0023-\\u005B\\u005D-\\u007E]".r)
  val quoted_string = join("\"".r, zeroOrMore(qtext), "\"".r)
  val local_part = alternate(dot_atom,quoted_string)
  val quoted_pair = "\\\\.".r
  val dtext = join(no_ws_ctl, "[!-Z^-~]".r)
  val dcontent = alternate(dtext,quoted_pair)
  val domain_literal = join("\\[]".r, zeroOrMore(dcontent), "]".r )
  val domain_part = alternate(dot_atom,domain_literal)
  val addr_spec = join(group(local_part), "@".r, group(domain_part))

  val EmailAddress = addr_spec
  val Identifier = "[-A-Za-z0-9_+=|!.^@#%*?]+".r
  val LegalName =  join(Identifier,zeroOrMore(join(" ".r, Identifier)))
  val DomainName = "(?:(?:[a-zA-Z0-9][-a-zA-Z0-9]*[a-zA-Z0-9])\\.){0,126}(?:[A-Za-z0-9][-A-Za-z0-9]*[A-Za-z0-9])".r
  val TcpPort =
    "(?:\\d|\\d{2}|\\d{3}|\\d{4}|[0-5]\\d{4}|6(?:(?:[0-4]\\d{3})|5(?:(?:[0-4]\\d{2})|5(?:(?:[0-2]\\d)|3[0-5]))))".r
  val IPv4Address =
    "(?:(?:[0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}(?:[0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])".r
}
