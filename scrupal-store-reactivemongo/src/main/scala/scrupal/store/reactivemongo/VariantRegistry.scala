/**********************************************************************************************************************
 * This file is part of Scrupal, a Scalable Reactive Content Management System.                                       *
 *                                                                                                                    *
 * Copyright © 2015 Reactific Software LLC                                                                            *
 *                                                                                                                    *
 * Licensed under the Apache License, Version 2.0 (the "License");  you may not use this file                         *
 * except in compliance with the License. You may obtain a copy of the License at                                     *
 *                                                                                                                    *
 *        http://www.apache.org/licenses/LICENSE-2.0                                                                  *
 *                                                                                                                    *
 * Unless required by applicable law or agreed to in writing, software distributed under the                          *
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,                          *
 * either express or implied. See the License for the specific language governing permissions                         *
 * and limitations under the License.                                                                                 *
 **********************************************************************************************************************/

package scrupal.store.reactivemongo

import reactivemongo.bson._
import scrupal.utils.AbstractRegistry

trait VariantReaderWriter[B <: VariantStorable[_], S <: B] {
  def fromDoc(doc: BSONDocument) : S
  def toDoc(obj: B) : BSONDocument
}

/** Registry Of Variants Of Base Class
  *
  * This registry keeps track of how to read and write variant subclasses of a base class so they can all be stored in
  * one collection. Subclasses must register their BSONHandler with the registry
  */
case class VariantRegistry[B <: VariantStorable[_]](name: String)
  extends AbstractRegistry[Symbol, VariantReaderWriter[B,_ <: B]]
  with VariantBSONDocumentReader[B]
  with VariantBSONDocumentWriter[B] {

  def kinds: Seq[String] = { _keys.map { k ⇒ k.name} }.toSeq

  def register[S <: B](kind: Symbol, handler: VariantReaderWriter[B, S]) = {
    super._register(kind, handler)
  }

  def read(doc: BSONDocument) : B = {
    doc.getAs[BSONString]("kind") match {
      case Some(str) ⇒
        super.lookup(Symbol(str.value)) match {
          case Some(handler) ⇒ handler.fromDoc(doc)
          case None ⇒ toss(s"Unknown kind of $name: '${str.value}")
        }
      case None ⇒ toss(s"Field 'kind' is missing from Node: ${doc.toString()}")
    }
  }

  def write(obj: B) : BSONDocument = {
    super.lookup(obj.kind) match {
      case Some(handler) ⇒ handler.toDoc(obj)
      case None ⇒ toss(s"Unknown kind of $name: ${obj.kind.name}")
    }
  }
}
