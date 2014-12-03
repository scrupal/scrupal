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

package scrupal.utils

trait PathWalker[D,A,V] extends ScrupalComponent {
  protected def isDocument(v: V) : Boolean
  protected def isArray(v: V) : Boolean
  protected def asDocument(v: V) : D
  protected def asArray(v: V) : A
  protected def indexDoc(key: String, d: D) : Option[V]
  protected def indexArray(index: Int, a: A) : Option[V]
  protected def arrayLength(a: A) : Int

  protected def error(msg: String, head: Seq[String], paths: Seq[String]) = {
    toss(s"${msg} at path '${head.mkString(".")}.${
      paths.head
    }' with remaining path elements: ${
      paths.tail.mkString(".")
    }")
  }

  protected def getAsDocument(head: Seq[String], paths: Seq[String], value: V) : D = {
    if (isDocument(value)) {
      asDocument(value)
    } else {
      error("Selected value was not a document so it could not be indexed", head, paths)
    }
  }

  protected def getAsArray(head: Seq[String], paths: Seq[String], value: V) : A = {
    if (isArray(value)) {
      asArray(value)
    } else {
      error("Selected value was not an array so it could not be indexed", head, paths)
    }
  }

  protected def walk(head: Seq[String], paths: Seq[String], parents: Seq[D], doc: D) : (String, Int, Seq[D], D, Option[V]) = {
    if (paths.isEmpty)
      return ("", -1, parents, doc, None)
    val name = paths.head
    val result = {
      if (paths.head.endsWith("]")) {
        val beginIndex = name.lastIndexOf("[") + 1
        if (beginIndex <= 0)
          error("Malformed array subscripts", head, paths)
        else {
          val endIndex = name.length - 1
          val index = name.substring(beginIndex, endIndex).toInt
          val rootName = name.substring(0, beginIndex - 1)
          indexDoc(rootName, doc) match {
            case Some(value) ⇒
              val anArray = getAsArray(head, paths, value)
              if (index < 0 || index > arrayLength(anArray) - 1)
                error(s"Array subscript out of bounds ", head, paths)
              else {
                val indexed = indexArray(index, anArray)
                (rootName, index, indexed)
              }
            case None ⇒ (rootName, index, None)
          }
        }
      } else {
        val indexed = indexDoc(name, doc)
        (name, -1, indexed)
      }
    }
    if (paths.length == 1 || !result._3.isDefined)
      (result._1, result._2, parents, doc, result._3)
    else {
      val nextDoc = getAsDocument(head, paths, result._3.get)
      val nextParents : Seq[D] = parents :+ doc
      walk(head :+ name, paths.tail, nextParents, nextDoc)
    }
  }

  def lookup(path: String, document: D) : Option[V] = {
    val parts = path.split("\\.").toSeq
    val (name, index, parents, doc, value) = walk(Seq.empty[String], parts, Seq.empty[D], document)
    value
  }

}

object MapSeqPathWalker extends PathWalker[Map[String,Any],Seq[Any],Any] {
  protected def isDocument(v: Any): Boolean = v.isInstanceOf[Map[_,_]]
  protected def asDocument(v: Any): Map[String,Any] = v.asInstanceOf[Map[String,Any]]
  protected def indexDoc(key: String, d: Map[String,Any]): Option[Any] = d.get(key)
  protected def isArray(v: Any): Boolean = v.isInstanceOf[Seq[Any]]
  protected def asArray(v: Any): Seq[Any] = v.asInstanceOf[Seq[Any]]
  protected def indexArray(index: Int, a: Seq[Any]): Option[Any] = Some(a(index))
  protected def arrayLength(a: Seq[Any]): Int = a.size
  def apply(path: String, doc: Map[String,Any]) : Option[Any] = lookup(path, doc)

}
