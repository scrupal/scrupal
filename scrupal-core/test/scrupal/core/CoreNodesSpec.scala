package scrupal.core

import java.io.File
import java.net.URL

import play.twirl.api.Html
import scrupal.fakes.{ScrupalSpecification, FakeContext}
import spray.http.MediaTypes
import scrupal.core.api.utf8

/** Test Cases For The Nodes in CoreNodes module
  * Created by reidspencer on 11/9/14.
  */
class CoreNodesSpec extends ScrupalSpecification("CoreNodeSpec") {

  val ctxt = new FakeContext

  "MessageNode" should {
    "put a message in a <div> element" in {
      val node = MessageNode('name, "Description", "text-warning", Html("This is boring."))
      node(ctxt) map { bytes =>
        val rendered = new String(bytes,utf8)
        rendered.startsWith("<div") must beTrue
        rendered.endsWith("</div>") must beTrue
      }
      success
    }
    "have a text/html media format" in {
      val node = MessageNode('name, "Description", "text-warning", Html("This is boring."))
      node.mediaType must beEqualTo(MediaTypes.`text/html`)
      node(ctxt)
      node.mediaType must beEqualTo(MediaTypes.`text/html`)
    }
  }

  "BasicNode" should {
    "echo its content" in {
      val node = BasicNode('name, "Description", Array[Byte]('s','c','r','u','p','a','l'))
      node(ctxt) map { bytes =>
        val rendered = new String(bytes, utf8)
        rendered must beEqualTo("scrupal")
      }
      success
    }
  }

  "AssetNode" should {
    "load a simple file" in {
      val node = AssetNode('name, "Description", new File("fakeAsset.txt"), MediaTypes.`text/plain`)
      node(ctxt) map  { bytes =>
        val rendered = new String(bytes, utf8)
        rendered.startsWith("This") must beTrue
        rendered.length must beEqualTo(80)
      }
      success
    }
  }

  "LinkNode" should {
    "properly render a link" in {
      val node = LinkNode('name, "Description", new URL("http://scrupal.org/"))
      node(ctxt) map  { bytes =>
        val rendered = new String(bytes, utf8)
        rendered.startsWith("<a href=") must beTrue
        rendered.endsWith("</a>") must beTrue
        rendered.contains("/scrupal.org/") must beTrue
      }
      success
    }

  }

  "LayoutNode" should {
    "handle missing tags with missing layout" in {
      val tags = Map[String,Symbol]("one" -> 'one, "two" -> 'two)
      val node = LayoutNode('name, "Description", tags, 'no_such_layout, MediaTypes.`text/html`)
      node(ctxt) map { bytes =>
        val resolved = tags.map { entry =>
          entry._1 -> MessageNode('warning,"alert-warning", "", Html(s"Could not find node '${entry._1}"))
        }
        val expected = scrupal.core.views.html.pages.defaultLayout(ctxt, resolved).body.getBytes(utf8)
        bytes must beEqualTo(expected)
      }
      success
    }
  }
}
