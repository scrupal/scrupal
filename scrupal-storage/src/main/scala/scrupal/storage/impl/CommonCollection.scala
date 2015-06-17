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

package scrupal.storage.impl

import scala.collection.mutable

import scrupal.storage.api._


trait CommonCollection[S <: Storable] extends Collection[S] {

  protected def ensureUniquePrimaryId(s : S) : Unit = {
    pidHoles.synchronized {
      if (s.primaryId == Storable.undefined_primary_id || s.primaryId > pidHWM) {
        s.primaryId = pidHoles.headOption match {
          case Some(l) ⇒
            pidHoles.remove(l)
            l
          case None ⇒
            pidHWM += 1
            pidHWM
        }
      } else if (pidHoles.contains(s.primaryId)) {
        pidHoles.remove(s.primaryId)
      }
    }
  }

  protected def erasePrimaryId(s: S) : ID = {
    val pid = s.getPrimaryId()
    pidHoles.synchronized { pidHoles.add( pid ) }
    s.primaryId = Storable.undefined_primary_id
    pid
  }

  private var pidHWM : Long = 0

  private val pidHoles = mutable.HashSet.empty[ID]

  def close() : Unit = { pidHWM = 0; pidHoles.clear()  }

}
