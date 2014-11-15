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

import play.twirl.api.Html
import scrupal.test.{ScrupalSpecification, FakeContext}
import spray.http.MediaTypes
import scrupal.core.api.utf8

/** Test Cases For The Nodes in CoreNodes module
  * Created by reidspencer on 11/9/14.
  */
class CoreNodesSpec extends ScrupalSpecification("CoreNodeSpec") {

  sequential

  "MessageNode" should {
    "put a message in a <div> element" in FakeContext.fixture("MessageNode1") { context ⇒
      val node = MessageNode('name, "Description", "text-warning", Html("This is boring."))
      node(context) map { bytes =>
        val rendered = new String(bytes,utf8)
        rendered.startsWith("<div") must beTrue
        rendered.endsWith("</div>") must beTrue
      }
      success
    }
    "have a text/html media format" in FakeContext.fixture("MessageNode2") { context ⇒
      val node = MessageNode('name, "Description", "text-warning", Html("This is boring."))
      node.mediaType must beEqualTo(MediaTypes.`text/html`)
      node(context)
      node.mediaType must beEqualTo(MediaTypes.`text/html`)
    }
  }

  "BasicNode" should {
    "echo its content" in FakeContext.fixture("BasicNode") { context ⇒
      val node = BasicNode('name, "Description", Array[Byte]('s','c','r','u','p','a','l'))
      node(context) map { bytes =>
        val rendered = new String(bytes, utf8)
        rendered must beEqualTo("scrupal")
      }
      success
    }
  }

  "AssetNode" should {
    "load a simple file" in FakeContext.fixture("AssetNode") { context ⇒
      val node = AssetNode('name, "Description", new File("fakeAsset.txt"), MediaTypes.`text/plain`)
      node(context) map  { bytes =>
        val rendered = new String(bytes, utf8)
        rendered.startsWith("This") must beTrue
        rendered.length must beEqualTo(80)
      }
      success
    }
  }

  "LinkNode" should {
    "properly render a link" in FakeContext.fixture("LinkNode") { context ⇒
      val node = LinkNode('name, "Description", new URL("http://scrupal.org/"))
      node(context) map  { bytes =>
        val rendered = new String(bytes, utf8)
        rendered.startsWith("<a href=") must beTrue
        rendered.endsWith("</a>") must beTrue
        rendered.contains("/scrupal.org/") must beTrue
      }
      success
    }

  }

  "LayoutNode" should {
    "handle missing tags with missing layout" in FakeContext.fixture("LayoutNode") { context ⇒
      val tags = Map[String,Symbol]("one" -> 'one, "two" -> 'two)
      val node = LayoutNode('name, "Description", tags, 'no_such_layout, MediaTypes.`text/html`)
      node(context) map { bytes =>
        val resolved = tags.map { entry =>
          entry._1 -> MessageNode('warning,"alert-warning", "", Html(s"Could not find node '${entry._1}"))
        }
        val expected = scrupal.core.views.html.pages.defaultLayout(context, resolved).body.getBytes(utf8)
        bytes must beEqualTo(expected)
      }
      success
    }
  }
}
