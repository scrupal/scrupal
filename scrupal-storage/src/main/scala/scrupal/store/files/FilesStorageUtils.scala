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

import java.io.{IOException, File}
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file._

object FilesStorageUtils {

  def recursivelyDeleteDirectory(dir : File) : Unit = {
    require(dir.isDirectory && dir.canWrite)
    val directory = dir.toPath
    Files.walkFileTree(directory, new SimpleFileVisitor[Path]() {
      override def visitFile(filePath: Path, attrs: BasicFileAttributes) : FileVisitResult = {
        Files.delete(filePath)
        FileVisitResult.CONTINUE
      }
      override def postVisitDirectory(dirPath: Path, exc: IOException) : FileVisitResult  = {
        if (exc != null)
          throw exc
        Files.delete(dirPath)
        FileVisitResult.CONTINUE
      }
    })
  }
}
