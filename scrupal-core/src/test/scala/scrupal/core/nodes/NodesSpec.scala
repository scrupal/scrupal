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

package scrupal.core.nodes

import java.io.{File, StringWriter}
import java.net.URL
import java.util.concurrent.TimeUnit

import akka.http.scaladsl.model.MediaTypes
import org.apache.commons.io.IOUtils
import play.api.libs.iteratee.{Enumerator, Iteratee}
import scrupal.api.Html.{Contents, ContentsArgs}
import scrupal.api._
import scrupal.test.{ScrupalApiSpecification, FakeContext}

import scala.concurrent.{ExecutionContext, Future, Await}
import scala.concurrent.duration.Duration
import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.existentials
import scalatags.Text.all._

/** Test Cases For The Nodes in CoreNodes module
  * Created by reidspencer on 11/9/14.
  */
class NodesSpec extends ScrupalApiSpecification("Nodes") {

  sequential

  case class Fixture(name: String)(implicit val scrupal : Scrupal = testScrupal) extends FakeContext[Fixture] {
    val template = new Html.Template(Symbol(name)) {
      val description = "Describe me"
      def apply(context: Context, args: ContentsArgs) : Contents = Seq(span("scrupal"))
    }

//    val command = CommandNode("Command", "A command node", "echo 'Hello, World!'")
    val message = MessageNode("Message", "A message node", "text-warning", "This is boring.")
    val html = HtmlNode("Html", "An Html Node", template)
    val file = FileNode("File", "A File Node",
                        new File("scrupal-core/src/test/resources/fakeAsset.txt"), MediaTypes.`text/plain`)
    val link = AnchorNode("Link", "A Link Node", new URL("http://scrupal.org/"))
//    val mdn = MarkedDocNode("MarkedDoc", "mdn", "docs", Iterable.empty[String])

    val tags = Map[String,Either[Node.Ref,Node]](
      "one" -> Right(message),
      "two" -> Right(html)
    )

    val layout = LayoutNode("Description", tags, Layout.default)
  }

  def consume(e: Enumerator[Array[Byte]]) : Array[Byte] = {
    val i = Iteratee.fold(Array.empty[Byte]) { (x:Array[Byte],y:Array[Byte]) ⇒ Array.concat(x, y) }
    Await.result(e.run(i), Duration(2, TimeUnit.SECONDS))
  }

  def runProducer(str: String, tags : Map[String,(Node,EnumeratorResponse)] = Map()) : String = {
    val lp = new LayoutProducer(str.getBytes(utf8), tags)
    val en = lp.buildEnumerator
    val raw_data = consume(en)
    new String(raw_data, utf8)
  }

  "LayoutProducer" should {
    "handle empty input correctly" in {
      val data = runProducer("")
      data must not contain "@@@"
      data.length must beEqualTo(0)
    }
    "handle missing tags correctly" in {
      val data = runProducer("This has some @@@missing@@@ tags.")
      data must contain("@@@ Missing Tag 'missing' @@@")
    }
    "substitute tags correctly" in { val f = Fixture("LayoutProducer1")
      val future = f.message(f) map { case h : Response ⇒
        f.message → h.toEnumeratorResponse
      }
      val pair = Await.result(future, Duration(1,TimeUnit.SECONDS))
      val data = runProducer("This has a @@@replaced@@@ tag.", Map("replaced" → pair))
      data must contain("This is boring.")
      data must not contain "@@@"
    }
  }


  "MessageNode" should {
    "put a message in a <div> element" in Fixture("MessageNode")(testScrupal) { f : Fixture ⇒
      val future = f.message(f) map {
        case h: HtmlResponse ⇒
          val rendered = h.content
          rendered.startsWith("<div") must beTrue
          rendered.endsWith("</div>") must beTrue
          success
        case _ ⇒
          failure("Incorrect result type")
      }
      Await.result(future, Duration(1, TimeUnit.SECONDS))
    }
    "have a text/html media format" in Fixture("MessageNode2")(testScrupal) { f : Fixture ⇒
      f.message.mediaType must beEqualTo(MediaTypes.`text/html`)
    }
  }

  "BasicNode" should {
    "echo its content" in { val f = Fixture("BasicNode")
      val future = f.html(f) map {
        case h: HtmlResponse ⇒
          h.content must beEqualTo("<span>scrupal</span>")
          success
        case _ ⇒
          failure("Incorrect result type")
      }
      Await.result(future, Duration(1, TimeUnit.SECONDS))
    }
  }

  "FileNode" should {
    "load a simple file" in { val f = Fixture("AssetNode")
      val future = f.file(f) map  { result: Response ⇒
        result.mediaType must beEqualTo(MediaTypes.`text/plain`)
        val rendered : String  = result match {
          case s: StreamResponse ⇒
            val writer = new StringWriter()
            IOUtils.copy(s.content, writer, utf8)
            writer.toString
          case e: ErrorResponse ⇒ "Error: " + e.formatted
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
    "properly render a link" in { val f = Fixture("LinkNode")
      val future = f.link(f) map  {
        case t: HtmlResponse ⇒
          val rendered = t.content
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
    "handle missing tags with missing layout" in {
      val f = Fixture("LayoutNode")
      val ts1 = System.nanoTime()
      withExecutionContext { implicit ec: ExecutionContext ⇒
        val future : Future[Array[Byte]] = f.layout(f) flatMap { r: Response ⇒
          val i = Iteratee.fold(Array.empty[Byte]) {
            (x:Array[Byte],y:Array[Byte]) ⇒ Array.concat(x, y)
          }
          r.toEnumerator.run(i)
        }
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

  "MarkedDocNode" should {
    "find a directory" in {
      val f = Fixture("MarkedDocNode")
      pending
    }
  }

}
