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
