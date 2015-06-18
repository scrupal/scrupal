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

package scrupal.store

/** Scrupal Storage Implementation In Transient Memory
  *
  * This package implements the Scrupal Storage API [[scrupal.storage.api]] using only transient memory as the
  * store. It is intended to be used as a handy cache for Scrupal modules that don't need to persist data. The
  * implementation is efficient as it just retains objects directly by reference in ConcurrentHashMap collections.
  * Indexes provide alternative maps into the same referenced objects.
  */
package object mem {

}
