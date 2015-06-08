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

/** A Registration of the Open Source Software Licenses
  * Created by reid on 11/11/14.
  */
case class OSSLicense(name : String, description : String, url : String)
  extends { val id = Symbol(name) } with Registrable[OSSLicense] {
  def registry = OSSLicense
}

object OSSLicense extends Registry[OSSLicense] {
  val registryName = "OSSLicenses"
  val registrantsName = "license"
  val GPLv3 = OSSLicense("GPLv3", "Gnu General Public License version 3", "http://www.gnu.org/copyleft/gpl.html")
}
