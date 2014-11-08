package scrupal.core.api

import scrupal.utils.Registry
import spray.http.{MediaTypes, MediaType}

/** Content Generating Node
  *
  * A node is a content generating function. It takes no input and generates output as an Array of Byte. It must
  * also declare a stable (val) MediaType that it generates that is understood by Spray.
 */
trait Node extends (()=>Array[Byte])
  with StorableRegistrable[Node]
  with Describable with Modifiable with Enablable
{
  def registry : Registry[Node] = Node
  def asT : Node = this
  val mediaType : MediaType = MediaTypes.`text/html`
}

object Node extends Registry[Node] {
  def registryName = "Nodes"
  def registrantsName = "node"

  val `application/bson` = MediaType.custom("application", "bson", fileExtensions = Seq("bson"),
    binary = true, compressible = true, allowArbitrarySubtypes = false)

}
