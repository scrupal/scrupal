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

package scrupal

/** Abstract Storage Model For Scrupal Data
  *
  * Scrupal provides a very simple storage model that can be implemented via databases, flat files, memory caches, etc.
  * This package contains the api for accessing these data sources in a generic fashion as well as the
  * implementation of various simple storage systems. Users may provide their own implementations based on other data
  * storage mechanisms. The model imposes few constraints on the underlying storage system. It makes some assumptions
  * about the structure of the information that must be maintained by the concrete implementation. Those assumptions
  * are:
  * - A given type of storage system is represented by the [[scrupal.storage.api.StorageContext]] class
  * - A StorageContext allows [[scrupal.storage.api.Store]] objects to be opened and closed.
  * - The [[scrupal.storage.api.Store]] objects contain named [[scrupal.storage.api.Schema]] instances.
  * - The [[scrupal.storage.api.Schema]] objects contain named [[scrupal.storage.api.Collection]] instances.
  * - The [[scrupal.storage.api.Collection]] objects contain indices to arbitrary objects.
  * - Collections are groups of related objects; related by purpose, not structure or content.
  * - Collections must have one primary index that uses the document's identifier type.
  * - Collections may have additional keys or even multi-field keys based on named fields in the objects.
  * - Documents without an optional key simply don't get put in that index (but will still be in the primary index)
  * - Serialization is done with Scala Pickling to/from either binary or string (JSON) formats.
  * - Management of the storage is beyond the scope of Scrupal. This is only a data manipulation interface
  */
package object storage {
}
