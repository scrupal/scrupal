/** ********************************************************************************************************************
  * This file is part of Scrupal, a Scalable Reactive Web Application Framework for Content Management                 *
  *                                                                                                                  *
  * Copyright (c) 2015, Reactific Software LLC. All Rights Reserved.                                                   *
  *                                                                                                                  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance     *
  * with the License. You may obtain a copy of the License at                                                          *
  *                                                                                                                  *
  *   http://www.apache.org/licenses/LICENSE-2.0                                                                     *
  *                                                                                                                  *
  * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed   *
  * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for  *
  * the specific language governing permissions and limitations under the License.                                     *
  * ********************************************************************************************************************
  */

package scrupal.store.files

import java.io.File
import java.net.URI

import scrupal.storage.api.{WriteResult, Schema, SchemaDesign}
import scrupal.storage.impl.CommonStore
import scrupal.store.files.FilesStorageInfo._

import scala.concurrent.{ExecutionContext, Future}

/** A Data Store In Files */
case class FilesStore private (uri : URI, info : FilesStoreInfo) extends CommonStore with FilesInfo[FilesStoreInfo] {
  def driver = FilesStorageDriver

  private [files] final val kind : String = "Store"

  private [files] final val dir : File = new File(uri.getPath)

  private [files] final val fileName : String = FilesStorageInfo.store_info_file_name

  protected def makeNewSchema(design: SchemaDesign) : Schema = {
    FilesSchema(this, design)
  }

  override def drop(implicit ec: ExecutionContext): Future[WriteResult] = {
    super.drop.map { wr ⇒
      FilesStorageUtils.recursivelyDeleteDirectory(dir)
      WriteResult.success()
    }
  }

  override def dropSchema(name : String)(implicit ec: ExecutionContext) : Future[WriteResult] = {
    _schemas.get(name) match {
      case Some(schema) ⇒
        _schemas.remove(name)
        schema.drop
      case None ⇒
        Future { toss(s"Schema named '$name' not found") }
    }
  }

  private def insertSchema(schema: FilesSchema) = _schemas.put(schema.name, schema)
}

object FilesStore {
  def apply(uri : URI) : FilesStore = {
    val storeDir = new File(uri.getPath)
    val info = {
      if (!storeDir.exists) {
        if (!storeDir.mkdirs())
          toss(s"Storage directory ${storeDir.getAbsolutePath} for $uri could not be created.")
      }
      val infoFile = new File(storeDir, FilesStorageInfo.store_info_file_name)
      if (infoFile.isFile && infoFile.canRead) {
        FilesStorageInfo.from[FilesStoreInfo](infoFile)
      } else {
        val info = FilesStoreInfo(uri.getPath)
        FilesStorageInfo.saveInfo(infoFile, info)
        info
      }
    }
    val store = FilesStore(uri, info)
    for (d ← store.dir.listFiles()) {
      if (d.isDirectory) {
        val schema = FilesSchema(store, d)
        store.insertSchema(schema)
      }
    }
    store
  }
}
