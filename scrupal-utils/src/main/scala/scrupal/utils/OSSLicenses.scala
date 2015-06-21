/** ********************************************************************************************************************
  * This file is part of Scrupal, a Scalable Reactive Content Management System.                                       *
  *                                                                                                                  *
  * Copyright © 2015 Reactific Software LLC                                                                            *
  *                                                                                                                  *
  * Licensed under the Apache License, Version 2.0 (the "License");  you may not use this file                         *
  * except in compliance with the License. You may obtain a copy of the License at                                     *
  *                                                                                                                  *
  *      http://www.apache.org/licenses/LICENSE-2.0                                                                  *
  *                                                                                                                  *
  * Unless required by applicable law or agreed to in writing, software distributed under the                          *
  * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,                          *
  * either express or implied. See the License for the specific language governing permissions                         *
  * and limitations under the License.                                                                                 *
  * ********************************************************************************************************************
  */

package scrupal.utils

import java.net.URI

/** A Registration of the Open Source Software Licenses */
case class OSSLicense(name : String, description : String, uri : URI)
  extends { val id = Symbol(name) } with Registrable[OSSLicense] {
  def registry = OSSLicense
}

object OSSLicense extends Registry[OSSLicense] {
  val registryName = "OSSLicenses"
  val registrantsName = "license"
  val GPLv3 = OSSLicense("GPLv3", "Gnu General Public License Version 3, 29 June 2007",
    new URI("http://www.gnu.org/copyleft/gpl.html"))
  val ApacheV2 = OSSLicense("ApacheV2", "Apache Software Foundation License Version 2.0, January 2004",
    new URI("http://www.apache.org/licenses/LICENSE-2.0"))
  val BSD3Clause = OSSLicense("BSD-3-Clause", "The BSD 3-Clause License",
    new URI("http://opensource.org/licenses/BSD-3-Clause"))
  val MPL2 = OSSLicense("MPL-2.0", "Mozilla Public License 2.0",
    new URI("http://opensource.org/licenses/MPL-2.0"))
  val MIT = OSSLicense("MIT", "The MIT License",
    new URI("http://opensource.org/licenses/MIT"))
}
