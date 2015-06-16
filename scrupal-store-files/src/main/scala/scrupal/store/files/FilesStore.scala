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

import java.io.{FilenameFilter, File}
import java.net.URI

import scrupal.storage.api._
import scrupal.storage.impl.CommonStore

/** Title Of Thing.
  *
  * Description of thing
  */
case class FilesStore private[files] (driver : StorageDriver, uri : URI) extends CommonStore {
  require(driver == FilesStorageDriver)

  private final val storeDirectory : File = new File(uri.getPath)

  if (!storeDirectory.exists)
    if (!storeDirectory.mkdirs())
      toss(s"Storage directory for ${uri.toASCIIString} could not be created")

  private final val specialFiles = Seq(".scrupal")

  private[files] object SpecialFilesFilter extends FilenameFilter {
    def accept(dir: File, name: String): Boolean = specialFiles.contains(name)
  }

  def exists : Boolean = {
    val file = new File(uri.getPath)
    if (!file.isDirectory)
      return false
    if (file.list(SpecialFilesFilter).size == specialFiles.size)
      true
    else
      false
  }

  /** Create a new collection for storing objects */
  override def addSchema(design: SchemaDesign): Schema = {
    val schema = driver.makeSchema(this, design.name, design).asInstanceOf[FilesSchema]
    val dir = new File(storeDirectory, design.name)
    if (!dir.isDirectory)
      if (!dir.mkdir)
        toss(s"Storage directory for schema '${design.name}' could not be created in ${storeDirectory.getAbsolutePath}")
    _schemas.put(design.name, schema)
    schema
  }


}
