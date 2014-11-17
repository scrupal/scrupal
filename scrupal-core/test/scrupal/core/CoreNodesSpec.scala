/**********************************************************************************************************************
 * Copyright © 2014 Reactific Software, Inc.                                                                          *
 *                                                                                                                    *
 * This file is part of Scrupal, an Opinionated Web Application Framework.                                            *
 *                                                                                                                    *
 * Scrupal is free software: you can redistribute it and/or modify it under the terms                                 *
 * of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License,   *
 * or (at your option) any later version.                                                                             *
 *                                                                                                                    *
 * Scrupal is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied      *
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more      *
 * details.                                                                                                           *
 *                                                                                                                    *
 * You should have received a copy of the GNU General Public License along with Scrupal. If not, see either:          *
 * http://www.gnu.org/licenses or http://opensource.org/licenses/GPL-3.0.                                             *
 **********************************************************************************************************************/

package scrupal.core

import java.io.File
import java.net.URL
import java.util.concurrent.TimeUnit

import play.twirl.api.Html
import reactivemongo.bson.BSONObjectID
import scrupal.test.{ScrupalSpecification, FakeContext}
import spray.http.MediaTypes
import scrupal.core.api._

import scala.concurrent.{Future, Await}
import scala.concurrent.duration.Duration

import scala.language.existentials

case class Fixture(name: String) extends FakeContext[Fixture](name) {

  val message = MessageNode("Description", "text-warning", Html("This is boring."))
  val html = HtmlNode("Description", Html("scrupal"))
  val file = FileNode("Description", new File("scrupal-core/test/resources/fakeAsset.txt"), MediaTypes
    .`text/plain`)
  val link = LinkNode("Description", new URL("http://scrupal.org/"))
  val tags = Map[String,BSONObjectID]("one" -> BSONObjectID.generate, "two" -> BSONObjectID.generate)
  val layout = LayoutNode("Description", tags, 'no_such_layout, MediaTypes.`text/html`)

}

/** Test Cases For The Nodes in CoreNodes module
  * Created by reidspencer on 11/9/14.
  */
class CoreNodesSpec extends ScrupalSpecification("CoreNodeSpec") {

  sequential

  "MessageNode" should {
    "put a message in a <div> element" in Fixture("MessageNode1") { f : Fixture ⇒
      val future = f.message(f) map {
        case h: HtmlResult ⇒
          val rendered = h.payload.body
          rendered.startsWith("<div") must beTrue
          rendered.endsWith("</div>") must beTrue
          success
        case _ ⇒ failure("Incorrect result type")
      }
      Await.result(future, Duration(1, TimeUnit.SECONDS))
    }
    "have a text/html media format" in Fixture("MessageNode2") { f : Fixture ⇒
      f.message.mediaType must beEqualTo(MediaTypes.`text/html`)
    }
  }

  "BasicNode" should {
    "echo its content" in Fixture("BasicNode") { f : Fixture ⇒
      val future = f.html(f) map {
        case h: HtmlResult ⇒
          h.payload.body must beEqualTo("scrupal")
          success
        case _ ⇒
          failure("Incorrect result type")
      }
      Await.result(future, Duration(1, TimeUnit.SECONDS))
    }
  }

  "AssetNode" should {
    "load a simple file" in Fixture("AssetNode") { f: Fixture ⇒
      val future = f.file(f) map  { result: Result[_] ⇒
        result.mediaType must beEqualTo(MediaTypes.`text/plain`)
        val rendered : String  = result match {
          case t: TextResult ⇒ t.payload
          case o: OctetsResult ⇒ new String(o.payload, utf8)
          case _ ⇒ throw new Exception("Unexpected result type")
        }
        rendered.startsWith("This") must beTrue
        rendered.contains("works or not.") must beTrue
        rendered.length must beEqualTo(79)
      }
      Await.result(future, Duration(1, TimeUnit.SECONDS))
    }
  }

  "LinkNode" should {
    "properly render a link" in Fixture("LinkNode") { f: Fixture ⇒
      val future = f.link(f) map  {
        case t: HtmlResult ⇒
          val rendered = t.payload.body
          rendered.startsWith("<a href=") must beTrue
          rendered.endsWith("</a>") must beTrue
          rendered.contains("/scrupal.org/") must beTrue
          success
        case _ ⇒ failure("Incorrect result type")
      }
      Await.result(future, Duration(1, TimeUnit.SECONDS))
    }

  }

  "LayoutNode" should {
    "handle missing tags with missing layout" in Fixture("LayoutNode") { f: Fixture ⇒
      val future = f.layout(f) map {
        case h: HtmlResult ⇒
          import HtmlHelpers._
          val futures = f.tags.map {
            case(tag,oid) ⇒
            tag-> {
              val node = MessageNode("Missing Node", "alert-warning", s"Could not find node '$tag".toHtml)
              node → node(f)
            }
          }
          val iter_of_F = futures.toSeq.map { case(k,v) ⇒ v._2.map { r ⇒ k → (v._1,r) }  }
          val x2 = (Future.sequence { iter_of_F }).map { seq ⇒ Map(seq:_*)  }
          val resolved = Await.result(x2,Duration(1,TimeUnit.SECONDS))
          val expected = scrupal.core.views.html.pages.defaultLayout(resolved)(f).body
          h.payload.body must beEqualTo(expected)
          success
        case _ ⇒ failure("Incorrect result type")
      }
      Await.result(future, Duration(1, TimeUnit.SECONDS))
    }
  }
}
