/**********************************************************************************************************************
 * Copyright © 2014 Reactific Software LLC                                                                            *
 *                                                                                                                    *
 * This file is part of Scrupal, an Opinionated Web Application Framework.                                            *
 *                                                                                                                    *
 * Scrupal is free software: you can redistribute it and/or modify it under the terms                                 *
 * of the GNU General Public License as published by the Free Software Foundation,                                    *
 * either version 3 of the License, or (at your option) any later version.                                            *
 *                                                                                                                    *
 * Scrupal is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;                               *
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                          *
 * See the GNU General Public License for more details.                                                               *
 *                                                                                                                    *
 * You should have received a copy of the GNU General Public License along with Scrupal.                              *
 * If not, see either: http://www.gnu.org/licenses or http://opensource.org/licenses/GPL-3.0.                         *
 **********************************************************************************************************************/

package scrupal.db

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

  def register[S <: B](kind: Symbol, handler: VariantReaderWriter[B, S]) = {
    super._register(kind, handler)
  }

  def read(doc: BSONDocument) : B = {
    doc.getAs[BSONString]("kind") match {
      case Some(str) =>
        super.lookup(Symbol(str.value)) match {
          case Some(handler) ⇒ handler.fromDoc(doc)
          case None ⇒ toss(s"Unknown kind of $name: '${str.value}")
        }
      case None => toss(s"Field 'kind' is missing from Node: ${doc.toString()}")
    }
  }

  def write(obj: B) : BSONDocument = {
    super.lookup(obj.kind) match {
      case Some(handler) ⇒ handler.toDoc(obj)
      case None ⇒ toss(s"Unknown kind of $name: ${obj.kind.name}")
    }
  }
}
