package scrupal.core.api


import scrupal.db.DisambiguousStorable
import scrupal.utils.{Registrable, Registry}
import spray.http.{MediaTypes, MediaType}

import scala.concurrent.Future

/** A function that generates content
  *
  * This is the basic characteristics of a Node. It is simply a function that receives a Context
  * and produces content as a Byte array. The Context provides the setting in which it is
  * generating the content. All dynamic content in Scrupal is generated through a Generator.
  */
trait Generator extends ((Context) => Future[Array[Byte]])

/** Content Generating Node
  *
  * This is the fundamental type of a node. It has some housekeeping information and can generate
  * some content in the form of an Array of Bytes. Modules define their own kinds of nodes by deriving
  * subclasses from this trait. As such, the Node class hierarchy defines the types of Nodes that
  * are possible to use with Scrupal. Note that Node instances are stored in the database and can be
  * very numerous. For that reason, they are not registered in an object registry.
  */
abstract class Node extends DisambiguousStorable[Identifier] with Describable with Modifiable with Enablable with Generator {
  val mediaType : MediaType
}

trait Arranger extends ((Context, Map[String,Node]) => Array[Byte])

trait Layout extends Registrable[Layout] with Describable with Arranger {
  def id : Identifier
  val description: String
  val mediaType : MediaType
  def registry = Layout
  def asT = this
}

object Layout extends Registry[Layout] {
  def registryName = "Layouts"
  def registrantsName = "layout"

  object default extends Layout {
    def id = 'default
    val description = "Default Layout"
    val mediaType = MediaTypes.`text/html`
    def apply(ctxt: Context, args: Map[String,Node]) : Array[Byte] = {
      scrupal.core.views.html.pages.defaultLayout(ctxt, args).body.getBytes(utf8)
    }
  }

}
