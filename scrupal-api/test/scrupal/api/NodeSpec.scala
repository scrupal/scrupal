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

package scrupal.api

import java.io.{StringWriter, File}
import java.net.URL
import java.util.concurrent.TimeUnit

import play.api.libs.iteratee.{Enumerator, Iteratee}
import play.twirl.api.Html
import scrupal.api.Template.TwirlHtmlTemplateFunction
import scrupal.test.{FakeContext, ScrupalSpecification}
import spray.http.MediaTypes
import org.apache.commons.io.IOUtils

import scala.concurrent.duration.{FiniteDuration, Duration}
import scala.concurrent.{Await, Future}
import scala.language.existentials

/** Test Cases For The Nodes in CoreNodes module
  * Created by reidspencer on 11/9/14.
  */
class NodeSpec extends ScrupalSpecification("NodeSpec") {

  sequential

  case class Fixture(name: String) extends FakeContext[Fixture](name) {

    val templateF : TwirlHtmlTemplateFunction = new {
      def apply(args: Map[String, Html])(implicit txt: Context) = Html("scrupal")
    }

    val template = TwirlHtmlTemplate(Symbol(name), "Describe me", templateF)

    val message : MessageNode = MessageNode("Description", "text-warning", Html("This is boring."))
    val html = HtmlNode("Description", template, args=Map.empty[String,Html])
    val file = FileNode("Description",
                        new File("scrupal-api/test/resources/fakeAsset.txt"), MediaTypes.`text/plain`)
    val link = LinkNode("Description", new URL("http://scrupal.org/"))
    val tags = Map[String,Either[NodeRef,Node]](
      "one" -> Right(message),
      "two" -> Right(html)
    )

    val layout = LayoutNode("Description", tags, Layout.default)

  }


  def consume(e: Enumerator[Array[Byte]]) : Array[Byte] = {
    val i = Iteratee.fold(Array.empty[Byte]) { (x:Array[Byte],y:Array[Byte]) ⇒ Array.concat(x, y) }
    Await.result(e.run(i),FiniteDuration(2, TimeUnit.SECONDS))
  }

  def runProducer(str: String, tags : Map[String,(Node,EnumeratorResult)] = Map()) : String = {
    val lp = new LayoutProducer(str.getBytes(utf8), tags)
    val en = lp.buildEnumerator
    val raw_data = consume(en)
    new String(raw_data, utf8)
  }

  "LayoutProducer" should {
    "handle empty input correctly" in {
      val data = runProducer("")
      data must not contain("@@@")
      data.length must beEqualTo(0)
    }
    "handle missing tags correctly" in {
      val data = runProducer("This has some @@@missing@@@ tags.")
      data must contain("@@@ Missing Tag 'missing' @@@")
    }
    "substitute tags correctly" in Fixture("LayoutProducer1") { f : Fixture ⇒
      val future = f.message(f) map { case h : Result[_] ⇒
        f.message → h.apply()
      }
      val pair = Await.result(future, Duration(1,TimeUnit.SECONDS))
      val data = runProducer("This has a @@@replaced@@@ tag.", Map("replaced" → pair))
      data must contain("This is boring.")
      data must not contain("@@@")
    }
  }


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

  "FileNode" should {
    "load a simple file" in Fixture("AssetNode") { f: Fixture ⇒
      val future = f.file(f) map  { result: Result[_] ⇒
        result.contentType.mediaType must beEqualTo(MediaTypes.`text/plain`)
        val rendered : String  = result match {
          case s: StreamResult ⇒
            val writer = new StringWriter()
            IOUtils.copy(s.payload, writer, utf8)
            writer.toString
          case e: ErrorResult ⇒ "Error: " + e.formatted
          case _ ⇒ throw new Exception("Unexpected result type")
        }
        rendered.startsWith("This") must beTrue
        rendered.contains("works or not.") must beTrue
        rendered.length must beEqualTo(80)
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
      val future : Future[Array[Byte]] = f.layout(f) flatMap { r: Result[_] ⇒
        val i = Iteratee.fold(Array.empty[Byte]) { (x:Array[Byte],y:Array[Byte]) ⇒ Array.concat(x, y) }
        r.asInstanceOf[EnumeratorResult].payload.run(i)
      }
      val ts1 = System.nanoTime()
      val data = Await.result(future, Duration(3, TimeUnit.SECONDS))
      val ts2 = System.nanoTime()
      val dt = (ts2 - ts1).toDouble / 1000000.0
      log.info(s"Resolve layout time = $dt milliseconds")
      val str = new String(data, utf8)
      str.contains("@@one@@") must beFalse
      str.contains("@@two@@") must beFalse
    }
  }
}
