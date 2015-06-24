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

package scrupal.utils

import play.api.libs.json.{ JsObject, JsArray, JsValue }

trait PathWalker[D, A, V] extends ScrupalComponent {
  protected def isDocument(v : V) : Boolean
  protected def isArray(v : V) : Boolean
  protected def asDocument(v : V) : D
  protected def asArray(v : V) : A
  protected def indexDoc(key : String, d : D) : Option[V]
  protected def indexArray(index : Int, a : A) : Option[V]
  protected def arrayLength(a : A) : Int

  protected def error(msg : String, head : Seq[String], paths : Seq[String]) = {
    toss(s"${msg} at path '${head.mkString(".")}.${
      paths.head
    }' with remaining path elements: ${
      paths.tail.mkString(".")
    }")
  }

  protected def getAsDocument(head : Seq[String], paths : Seq[String], value : V) : D = {
    if (isDocument(value)) {
      asDocument(value)
    } else {
      error("Selected value was not a document so it could not be indexed", head, paths)
    }
  }

  protected def getAsArray(head : Seq[String], paths : Seq[String], value : V) : A = {
    if (isArray(value)) {
      asArray(value)
    } else {
      error("Selected value was not an array so it could not be indexed", head, paths)
    }
  }

  protected def walk(head : Seq[String], paths : Seq[String], parents : Seq[D], doc : D) : (String, Int, Seq[D], D, Option[V]) = {
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
    if (paths.length == 1 || result._3.isEmpty)
      (result._1, result._2, parents, doc, result._3)
    else {
      val nextDoc = getAsDocument(head, paths, result._3.get)
      val nextParents : Seq[D] = parents :+ doc
      walk(head :+ name, paths.tail, nextParents, nextDoc)
    }
  }

  def lookup(path : String, document : D) : Option[V] = {
    val parts = path.split('.').toSeq
    val (name, index, parents, doc, value) = walk(Seq.empty[String], parts, Seq.empty[D], document)
    value
  }

}

object MapSeqPathWalker extends PathWalker[Map[String, Any], Seq[Any], Any] {
  protected def isDocument(v : Any) : Boolean = v.isInstanceOf[Map[_, Any]]
  protected def asDocument(v : Any) : Map[String, Any] = v.asInstanceOf[Map[String, Any]]
  protected def indexDoc(key : String, d : Map[String, Any]) : Option[Any] = d.get(key)
  protected def isArray(v : Any) : Boolean = v.isInstanceOf[Seq[Any]]
  protected def asArray(v : Any) : Seq[Any] = v.asInstanceOf[Seq[Any]]
  protected def indexArray(index : Int, a : Seq[Any]) : Option[Any] = Some(a(index))
  protected def arrayLength(a : Seq[Any]) : Int = a.size
  def apply(path : String, doc : Map[String, Any]) : Option[Any] = lookup(path, doc)

}

object JsonPathWalker extends PathWalker[JsObject, JsArray, JsValue] {
  protected def isDocument(v : JsValue) : Boolean = v.isInstanceOf[JsObject]
  protected def isArray(v : JsValue) : Boolean = v.isInstanceOf[JsArray]
  protected def asArray(v : JsValue) : JsArray = v.asInstanceOf[JsArray]
  protected def asDocument(v : JsValue) : JsObject = v.asInstanceOf[JsObject]
  protected def indexDoc(key : String, d : JsObject) : Option[JsValue] = d.value.get(key)
  protected def indexArray(index : Int, a : JsArray) : Option[JsValue] = {
    if (index < 0 || index > a.value.length) None else Some(a.value(index))
  }
  protected def arrayLength(a : JsArray) : Int = a.value.length
  def apply(path : String, doc : JsObject) : Option[JsValue] = lookup(path, doc)
}
