/** ********************************************************************************************************************
  * This file is part of Scrupal, a Scalable Reactive Content Management System.                                       *
  *                                                                                              *
  * Copyright © 2015 Reactific Software LLC                                                                            *
  *                                                                                              *
  * Licensed under the Apache License, Version 2.0 (the "License");  you may not use this file                         *
  * except in compliance with the License. You may obtain a copy of the License at                                     *
  *                                                                                              *
  * http://www.apache.org/licenses/LICENSE-2.0                                                                  *
  *                                                                                              *
  * Unless required by applicable law or agreed to in writing, software distributed under the                          *
  * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,                          *
  * either express or implied. See the License for the specific language governing permissions                         *
  * and limitations under the License.                                                                                 *
  * ********************************************************************************************************************
  */

package scrupal.storage.api

import scrupal.utils.ScrupalComponent

/** A Storable Object
  *
  * All entities placed in storage must derive from Storable.
  */
@SerialVersionUID(1L)
trait Storable extends Serializable with Equals {
  private[storage] var primaryId : Long = Storable.undefined_primary_id
  def getPrimaryId() : Long = primaryId
  def canEqual(other : Any) : Boolean = { other.isInstanceOf[Storable] }
  override def equals(other : Any) : Boolean = {
    other match {
      case that: Storable => canEqual(other)
      case _ => false
    }
  }
}

trait StoredFormat {
  def toBytes : Array[Byte]
}

trait StorableTransformer[F <: StoredFormat, S <: Storable] {
  def write(s : S) : F
  def read(data : F) : S
}

object Storable extends ScrupalComponent {
  final val serialVersionUID : Long = 42L

  val undefined_primary_id : Long = Long.MinValue

  def apply[S <: Storable, F <: StoredFormat](data : F)(implicit trans : StorableTransformer[F, S]) : S = {
    trans.read(data)
  }

  def unapply[S <: Storable, F <: StoredFormat](s: S)(implicit trans: StorableTransformer[F, S]) : F = {
    trans.write(s)
  }

  implicit class StorablePimps[S <: Storable](storable : S) {
    final def payload[F <: StoredFormat](implicit trans : StorableTransformer[F, S]) : F = {
      trans.write(storable)
    }
    final def toBytes[F <: StoredFormat](implicit trans : StorableTransformer[F,S]) : Array[Byte] = {
      trans.write(storable).toBytes
    }
  }
}

