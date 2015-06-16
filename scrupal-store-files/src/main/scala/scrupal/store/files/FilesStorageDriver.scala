/** ********************************************************************************************************************
  * This file is part of Scrupal, a Scalable Reactive Web Application Framework for Content Management                 *
  *                                                                                                                   *
  * Copyright (c) 2015, Reactific Software LLC. All Rights Reserved.                                                   *
  *                                                                                                                   *
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance     *
  * with the License. You may obtain a copy of the License at                                                          *
  *                                                                                                                   *
  *    http://www.apache.org/licenses/LICENSE-2.0                                                                     *
  *                                                                                                                   *
  * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed   *
  * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for  *
  * the specific language governing permissions and limitations under the License.                                     *
  * ********************************************************************************************************************
  */

package scrupal.store.files

import java.net.URI

import scrupal.storage.api._
import scrupal.storage.impl.CommonStorageDriver


/** Title Of Thing.
  *
  * Description of thing
  */
object FilesStorageDriver extends CommonStorageDriver {
  def id = 'files
  override val name : String = "Files"
  override val scheme : String = "scrupal-files"
  private val authority : String = "localhost"

  override def isDriverFor(uri: URI) : Boolean = {
    super.isDriverFor(uri) && uri.getAuthority == authority && uri.getPort == -1
  }

  override def storeExists(uri : URI) : Boolean = {
    stores.get(uri) match {
      case Some(fileStore) ⇒ fileStore.exists
      case None ⇒
        val fs = FilesStore(this, uri)
        fs. exists
    }
  }

  def makeReference[S <: Storable](coll : Collection[S], id : ID) : Reference[S] = {
    require(coll.isInstanceOf[FilesCollection[S]])
    StorableReference[S](coll, id)
  }

  def makeContext(id : Symbol, uri : URI, create: Boolean = false) : FilesStorageContext = {
    withStore(uri, create) { store ⇒
      FilesStorageContext(id, uri, store.asInstanceOf[FilesStore])
    }
  }

  def makeStore(uri : URI) : Store = FilesStore(this, uri)

  def makeSchema(store : Store, name : String, design : SchemaDesign) = {
    require(store.isInstanceOf[FilesStore])
    FilesSchema(store.asInstanceOf[FilesStore], name, design)
  }

  def makeCollection[S <: Storable](schema : Schema, name : String) : Collection[S] = {
    require(schema.isInstanceOf[FilesSchema])
    FilesCollection[S](schema, name)
  }

}
