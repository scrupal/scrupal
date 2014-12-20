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

package scrupal.core.api

import java.io.{StringWriter, File}
import java.net.URL
import java.util.concurrent.TimeUnit

import play.api.libs.iteratee.{Enumerator, Iteratee}
import scalatags.Text.all._
import scrupal.core.api.Html.{Fragment, TemplateGenerator}
import scrupal.core.nodes.{FileNode, MessageNode, LinkNode, HtmlNode}
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

    val templateF = new TemplateGenerator {
      def apply(context: Context, args: Map[String, Fragment]) = Seq(span("scrupal"))
    }

    val template = Html.Template(Symbol(name), "Describe me")(templateF)

    val message = MessageNode("Description", "text-warning", "This is boring.")
    val html = HtmlNode("Description", template, args=Map.empty[String,Fragment])
    val file = FileNode("Description",
                        new File("scrupal-core/test/resources/fakeAsset.txt"), MediaTypes.`text/plain`)
    val link = LinkNode("Description", new URL("http://scrupal.org/"))

    val tags = Map[String,Either[NodeRef,Node]](
      "one" -> Right(message),
      "two" -> Right(html)
    )

    // val layout = LayoutNode("Description", tags, Layout.default)
  }

  def consume(e: Enumerator[Array[Byte]]) : Array[Byte] = {
    val i = Iteratee.fold(Array.empty[Byte]) { (x:Array[Byte],y:Array[Byte]) ⇒ Array.concat(x, y) }
    Await.result(e.run(i),FiniteDuration(2, TimeUnit.SECONDS))
  }

  /*
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
    "substitute tags correctly" in { val f = Fixture("LayoutProducer1")
      val future = f.message(f) map { case h : Result[_] ⇒
        f.message → h.apply()
      }
      val pair = Await.result(future, Duration(1,TimeUnit.SECONDS))
      val data = runProducer("This has a @@@replaced@@@ tag.", Map("replaced" → pair))
      data must contain("This is boring.")
      data must not contain("@@@")
    }
  }

 */

  "MessageNode" should {
    "put a message in a <div> element" in { val f = Fixture("MessageNode1")
      val future = f.message(f) map {
        case h: HtmlResult ⇒
          val rendered = h.payload
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
    "echo its content" in { val f = Fixture("BasicNode")
      val future = f.html(f) map {
        case h: HtmlResult ⇒
          h.payload must beEqualTo("<span>scrupal</span>")
          success
        case _ ⇒
          failure("Incorrect result type")
      }
      Await.result(future, Duration(1, TimeUnit.SECONDS))
    }
  }

  "FileNode" should {
    "load a simple file" in { val f = Fixture("AssetNode")
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
    "properly render a link" in { val f = Fixture("LinkNode")
      val future = f.link(f) map  {
        case t: HtmlResult ⇒
          val rendered = t.payload
          rendered.startsWith("<a href=") must beTrue
          rendered.endsWith("</a>") must beTrue
          rendered.contains("/scrupal.org/") must beTrue
          success
        case _ ⇒ failure("Incorrect result type")
      }
      Await.result(future, Duration(1, TimeUnit.SECONDS))
    }

  }

  /*
  "LayoutNode" should {
    "handle missing tags with missing layout" in {
      val f = Fixture("LayoutNode")
      val ts1 = System.nanoTime()
      val future : Future[Array[Byte]] = f.layout(f) flatMap { r: Result[_] ⇒
        val i = Iteratee.fold(Array.empty[Byte]) { (x:Array[Byte],y:Array[Byte]) ⇒ Array.concat(x, y) }
        r.asInstanceOf[EnumeratorResult].payload.run(i)
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
*/

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
}
