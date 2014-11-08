package scrupal.core

import java.io.File
import java.nio.charset.Charset

import org.joda.time.DateTime
import play.twirl.api.Html
import scrupal.core.api._
import spray.http.MediaType
import resource._

/** Basic Html Node
  * This is a node that generates HTML content dynamically.
  * @param id
  * @param description
  * @param enabled
  * @param modified
  * @param created
  */
case class HtmlNode(
  id : Identifier,
  description: String,
  payload: Html,
  var enabled : Boolean = true,
  modified : Option[DateTime] = None,
  created: Option[DateTime] = None
) extends Node {

  def apply() : Array[Byte] = {
    payload.body.getBytes(Charset.forName("UTF-8"))
  }
}

/** Static HTML Node
  * This node type generates HTML from a static file or resource
  * @param id
  * @param description
  * @param file
  * @param enabled
  * @param modified
  * @param created
  */
case class FileNode(
  id : Identifier,
  description: String,
  file: File,
  override val mediaType: MediaType,
  var enabled : Boolean = true,
  modified : Option[DateTime] = None,
  created: Option[DateTime] = None
) extends Node {
  def apply() : Array[Byte] = {
    val result = new Array[Byte](file.length().toInt) // NOTE: <= 4GB Okay here?
    managed(scala.io.Source.fromFile(file)) map { source =>
      val body = source.getLines() mkString "\n"
      body.getBytes(Charset.forName("UTF-8"))
    }
  }.now
}
