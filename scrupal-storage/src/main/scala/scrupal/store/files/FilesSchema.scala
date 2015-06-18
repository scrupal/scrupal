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
import java.nio.file.Files

import scrupal.storage.impl.CommonSchema

import scrupal.storage.api._
import scrupal.utils.ScrupalComponent

import scala.concurrent.{Future, ExecutionContext}

/** A Schema Of Collections stored in files
  *
  * @param store The FileStore in which the schema is located
  * @param info The Schema information including its design object
  */
case class FilesSchema private (
  store : FilesStore,
  info : FilesSchemaInfo
) extends CommonSchema with FilesInfo[FilesSchemaInfo] {

  final val design : SchemaDesign = info.design

  final val name : String = design.name

  private[files] final val kind : String = "Schema"

  private[files] final val dir : File = new File(store.dir, name)

  private[files] final val fileName : String = FilesStorageInfo.store_info_file_name

  /** FIXME: Find and return a Collection of a specific name */
  override def collectionFor[S <: Storable](name : String) : Option[Collection[S]] = {
    super.collectionFor[S](name) orElse {
      val collDir = new File(dir, name)
      if (collDir.exists) {
        val coll = FilesCollection[S](this, name)
        colls.put(name, coll)
        Some(coll)
      } else {
        None
      }
    }
  }

  def addCollection[S <: Storable](name : String)(implicit ec: ExecutionContext) : Future[Collection[S]] = Future {
    colls.get(name) match {
      case Some(coll) ⇒
        toss(s"Collection $name already exists.")
      case None ⇒
        val coll = FilesCollection[S](this, name)
        colls.put(name, coll)
        coll
    }
  }

  override def drop(implicit ec: ExecutionContext): Future[WriteResult] = {
    super.drop.map { wr ⇒
      FilesStorageUtils.recursivelyDeleteDirectory(dir)
      WriteResult.success()
    }
  }

  override def close() : Unit = {
    for ((name, coll) ← colls) {
      coll.close()
    }
  }

  def dropCollection[S <: Storable](name: String)(implicit ec: ExecutionContext): Future[WriteResult] = Future {
    withCollection(name) { coll : Collection[S] ⇒
      coll.drop
      Files.delete(infoPath)
      Files.delete(dir.toPath)
      WriteResult.success()
    }
  }

  private def insertCollection(coll: Collection[_]) = {
    colls.put(coll.name, coll)
  }
}

object FilesSchema extends ScrupalComponent {
  def apply(store: FilesStore, design: SchemaDesign) : FilesSchema = {
    require(store.dir.isDirectory)
    require(design.name.nonEmpty)
    val schemaDir = new File(store.dir, design.name)
    if (!(schemaDir.isDirectory && schemaDir.canRead)) {
      if (!schemaDir.mkdirs)
        toss(s"Could not create schema directory at ${schemaDir.getAbsolutePath}")
    }
    val schemaFile = new File(schemaDir, FilesStorageInfo.schema_info_file_name)
    val info = FilesSchemaInfo(design)
    FilesStorageInfo.saveInfo(schemaFile, info, overwrite=false)
    FilesSchema(store, info)
  }

  def apply(store: FilesStore, schemaDir: File) : FilesSchema = {
    require(store.dir.isDirectory)
    require(schemaDir.isDirectory)
    val infoFile = new File(schemaDir, FilesStorageInfo.schema_info_file_name)
    require(infoFile.canRead)
    val info = FilesStorageInfo.from[FilesSchemaInfo](infoFile)
    val schema = FilesSchema(store, info)
    for (d ← schema.dir.listFiles()) {
      if (d.isDirectory) {
        val coll = FilesCollection(schema, d)
        schema.insertCollection(coll)
      }
    }
    schema
  }
}
