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

package scrupal.api

import java.util.concurrent.TimeUnit

import org.specs2.mutable.Specification
import scrupal.test.ScrupalApiSpecification

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

class NodeSpec extends ScrupalApiSpecification("Node") {

  "NodeSpec" should {
    "have some test examples" in {
      pending
    }
  }
  /*
  "Node" should {
    "disambiguate variants" in {
      val f = Fixture("Node")
      f.withSchema { (dbc, schema) ⇒
        val o1 = f.message
        val f1 = schema.nodes.insert(o1) flatMap { wr ⇒
          wr.ok must beTrue
          schema.nodes.fetch(o1._id) map { optNode ⇒
            optNode match {
              case Some(node) ⇒
                node.isInstanceOf[MessageNode] must beTrue
              case None ⇒
                failure("not found")
            }
            optNode.isDefined must beTrue
          }
        }
        val f2 = schema.nodes.insert(f.html) flatMap { wr ⇒
          wr.ok must beTrue
          schema.nodes.fetch(f.html._id) map { optNode ⇒
            optNode.isDefined must beTrue
            optNode.get.isInstanceOf[HtmlNode] must beTrue
          }
        }
        val f3 = schema.nodes.insert(f.file) flatMap { wr ⇒
          wr.ok must beTrue
          schema.nodes.fetch(f.file._id) map { optNode ⇒
            optNode.isDefined must beTrue
            optNode.get.isInstanceOf[FileNode] must beTrue
          }
        }
        val f4 = schema.nodes.insert(f.link) flatMap { wr ⇒
          wr.ok must beTrue
          schema.nodes.fetch(f.link._id) map { optNode ⇒
            optNode.isDefined must beTrue
            optNode.get.isInstanceOf[LinkNode] must beTrue
          }        }
        /*val f5 = schema.nodes.insert(f.layout) flatMap { wr ⇒
           wr.ok must beTrue
           schema.nodes.fetch(f.layout._id) map { optNode ⇒
             optNode.isDefined must beTrue
             optNode.get.isInstanceOf[LayoutNode] must beTrue
           }
         }*/
        val futures = Future sequence List(f1,f2,f3,f4)
        val result = Await.result(futures, Duration(2,TimeUnit.SECONDS))
        val summary = result.foldLeft(true) { (last,next) ⇒ last && next }
        summary must beTrue
      }
    }
  }
  */
}
