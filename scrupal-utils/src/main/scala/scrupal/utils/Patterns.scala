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

import scala.util.matching.Regex

/** One line sentence description here.
  * Further description here.
  */
object Patterns {
  /** Join a sequence of patterns together */
  def join(r : Regex*) : Regex = {
    val result : StringBuffer = new StringBuffer(1024)
    r.foldLeft(result){
      case (result : StringBuffer, regex : Regex) ⇒ result.append(regex.unanchored.pattern.pattern); result
    }.toString().r
  }

  private def serialize(r : Seq[Regex]) : String = r.foldLeft("") { case ((x, y)) ⇒ x + y.pattern.pattern }
  /** Make a pattern into various repeating forms of itself  */
  def anchored(r : Regex*) : Regex = ("^" + serialize(r) + "$").r
  def group(r : Regex*) : Regex = ("(?:" + serialize(r) + ")").r
  def capture(r : Regex, name : String = "") : Regex = {
    if (name.isEmpty)
      s"(${r.pattern.pattern})".r
    else
      s"(?<$name>${r.pattern.pattern})".r
  }
  def optional(r : Regex) = ("(?:" + r.pattern.pattern + ")?").r
  def atLeastOne(r : Regex) = ("(?:" + r.pattern.pattern + ")+").r
  def zeroOrMore(r : Regex) = ("(?:" + r.pattern.pattern + ")*").r
  def between(min : Int, max : Int, r : Regex) = ("(?:" + r.pattern.pattern + s"){$min,$max}").r
  def alternate(r1 : Regex, r2 : Regex) = ("(?:" + r1.pattern.pattern + ")|(?:" + r2.pattern.pattern + ")").r

  // From RFC 2822. The pattern names below match the grammar production names and definitions from the RFC
  val atext = "[-A-Za-z0-9!#$%&'*+/=?^_`|~]".r
  val atom = atLeastOne(atext)
  val dot_atom = join(atom, zeroOrMore(join("[.]".r, atom)))
  val no_ws_ctl = "[\\u0001-\\u0008\\u000B-\\u000C\\u000E-\\u001F\\u007F]".r
  val qtext = alternate(no_ws_ctl, "[!\\u0023-\\u005B\\u005D-\\u007E]".r)
  val quoted_string = join("\"".r, zeroOrMore(qtext), "\"".r)
  val local_part = alternate(dot_atom, quoted_string)
  val quoted_pair = "\\\\.".r
  val dtext = join(no_ws_ctl, "[!-Z^-~]".r)
  val dcontent = alternate(dtext, quoted_pair)
  val domain_literal = join("\\[]".r, zeroOrMore(dcontent), "]".r)
  val domain_part = alternate(dot_atom, domain_literal)
  val addr_spec = join(group(local_part), "@".r, group(domain_part))

  val EmailAddress = addr_spec
  val Identifier = "[-\\w_+=|!.^@#%*?]+".r
  val Markdown = "[-\\s\\w~`!@#$%^&*()_+={}\\[]|\\\\:;\"'<>,.?/]*".r
  val Password = between(6, 64, Markdown)
  val LegalName = join(Identifier, zeroOrMore(join(" ".r, Identifier)))

  // TODO: convert these patterns to use the constructors so we can comprehend them!
  val DomainName = "(?:(?:[a-zA-Z0-9][-a-zA-Z0-9]*[a-zA-Z0-9])\\.){0,126}(?:[A-Za-z0-9][-A-Za-z0-9]*[A-Za-z0-9])".r
  val TcpPort =
    "(?:\\d|\\d{2}|\\d{3}|\\d{4}|[0-5]\\d{4}|6(?:(?:[0-4]\\d{3})|5(?:(?:[0-4]\\d{2})|5(?:(?:[0-2]\\d)|3[0-5]))))".r
  val IPv4Address =
    "(?:(?:[0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}(?:[0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])".r
  val UniformResourceLocator = "^(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]$".r

  val URLPathable = "[-A-Za-z0-9_.~]{1,64}".r

  val Title = between(4, 70, "[-\\s\\w\\d+:%!_{}|;<>,.?]".r)

  val NotAllowedInUrl = "[^-\\w\\d._+|]".r
}
