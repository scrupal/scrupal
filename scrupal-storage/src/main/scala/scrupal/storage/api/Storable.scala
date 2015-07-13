/** ********************************************************************************************************************
  * This file is part of Scrupal, a Scalable Reactive Content Management System.                                       *
  *                                                                                             *
  * Copyright © 2015 Reactific Software LLC                                                                            *
  *                                                                                             *
  * Licensed under the Apache License, Version 2.0 (the "License");  you may not use this file                         *
  * except in compliance with the License. You may obtain a copy of the License at                                     *
  *                                                                                             *
  * http://www.apache.org/licenses/LICENSE-2.0                                                                  *
  *                                                                                             *
  * Unless required by applicable law or agreed to in writing, software distributed under the                          *
  * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,                          *
  * either express or implied. See the License for the specific language governing permissions                         *
  * and limitations under the License.                                                                                 *
  * ********************************************************************************************************************
  */

package scrupal.storage.api

import com.esotericsoftware.kryo.Serializer
import scrupal.utils.ScrupalComponent

/** A Storable Object
  *
  * All entities placed in storage must derive from Storable.
  */
@SerialVersionUID(1L)
trait Storable extends Serializable with Equals {
  private[scrupal] var primaryId : Long = Storable.undefined_primary_id
  def getPrimaryId() : Long = primaryId
  def canEqual(other : Any) : Boolean = { other.isInstanceOf[Storable] }
  override def equals(other : Any) : Boolean = {
    other match {
      case that: Storable => canEqual(other)
      case _ => false
    }
  }
}

trait StorableSerializer[T <: Storable] extends Serializer[T]

trait StorageFormat {
  def toBytes : Array[Byte]
}

trait StorageFormatter[F <: StorageFormat, S <: Storable] {
  def write(s : S) : F
  def read(data : F) : S
}

object Storable extends ScrupalComponent {
  final val serialVersionUID : Long = 42L

  val undefined_primary_id : Long = Long.MinValue

  def apply[S <: Storable, F <: StorageFormat](data : F)(implicit trans : StorageFormatter[F, S]) : S = {
    trans.read(data)
  }

  def unapply[S <: Storable, F <: StorageFormat](s: S)(implicit trans: StorageFormatter[F, S]) : F = {
    trans.write(s)
  }

  implicit class StorablePimps[S <: Storable](storable : S) {
    final def payload[F <: StorageFormat](implicit trans : StorageFormatter[F, S]) : F = {
      trans.write(storable)
    }
    final def toBytes[F <: StorageFormat](implicit trans : StorageFormatter[F,S]) : Array[Byte] = {
      trans.write(storable).toBytes
    }
  }
}

