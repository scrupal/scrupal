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

package scrupal.store.files

import java.io.{FileOutputStream, FileInputStream, File}
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.{Files, Path}
import java.time.Instant

import com.esotericsoftware.kryo.io.{Output, Input}

import scala.language.existentials

import scrupal.storage.api.{ID, SchemaDesign}
import scrupal.storage.impl.KryoFormatter
import scrupal.utils.ScrupalComponent

trait FilesStorageInfo {
  val version = 1
}

case class FilesStoreInfo(name: String) extends FilesStorageInfo

case class FilesSchemaInfo(design: SchemaDesign) extends FilesStorageInfo

case class FilesCollectionInfo(
  name: String,
  indexes: Map[String,(Class[_],FilesIndex[_])] = Map.empty[String,(Class[_],FilesIndex[_])]
) extends FilesStorageInfo

case class FilesIndex[ET](typ: Class[_], entries: Map[ET,ID] ) extends FilesStorageInfo

trait FilesInfo[T <: FilesStorageInfo] extends ScrupalComponent {

  private[files] def kind : String

  private[files] def dir : File

  private[files] def fileName : String

  private[files] def infoFile : File = { new File(dir, fileName) }

  private[files] def infoPath: Path = { infoFile.toPath }

  protected def info : T

  def created: Instant = {
    Files.readAttributes(infoPath, classOf[BasicFileAttributes]).creationTime().toInstant
  }

  def exists : Boolean = {
    dir.isDirectory && infoFile.isFile
  }
}

object FilesStorageInfo extends ScrupalComponent {
  final val store_info_file_name = ".scrupal_store"
  final val schema_info_file_name = ".scrupal_schema"
  final val collection_info_file_name = ".scrupal_collection"

  def registerSerializers = {
    import scrupal.storage.impl.KryoFormatter.kryo
    kryo.register(classOf[FilesStoreInfo], 200)
    kryo.register(classOf[FilesSchemaInfo], 201)
    kryo.register(classOf[FilesCollectionInfo], 202)
    kryo.register(classOf[FilesIndex[_]], 203)
  }

  def saveInfo[FSI <: FilesStorageInfo](file: File, info: FSI, overwrite: Boolean = false) : Unit = {
    if (file.exists && !overwrite)
      toss(s"Refusing to overwrite storage info at ${file.getAbsolutePath}")
    val fo = new FileOutputStream(file)
    val out = new Output(fo)
    try {
      KryoFormatter.kryo.writeClassAndObject(out, info)
    } finally {
      out.close()
      fo.close()
    }
  }

  def from[AS <: FilesStorageInfo](file: File) : AS = {
    if (file.isFile && file.canRead) {
      val fi = new FileInputStream(file)
      val inp = new Input(fi)
      try {
        KryoFormatter.kryo.readClassAndObject(inp).asInstanceOf[AS]
      } finally {
        inp.close()
        fi.close()
      }
    } else {
      toss(s"Expected a file at ${file.getAbsolutePath}")
    }
  }
}

