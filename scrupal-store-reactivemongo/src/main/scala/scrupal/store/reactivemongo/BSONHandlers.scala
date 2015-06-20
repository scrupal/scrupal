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

package scrupal.store.reactivemongo


import java.io.File
import java.net.URL
import java.util.concurrent.TimeUnit

import akka.http.scaladsl.model.{MediaTypes, MediaType}
import org.joda.time.DateTime
import play.api.libs.json.JsString
import reactivemongo.api.DefaultDB
import reactivemongo.api.indexes.{Index, IndexType}
import reactivemongo.bson._
import scrupal.api._
import scrupal.api.types.{StructuredType, BundleType}


import scrupal.utils._

import scala.concurrent.duration.Duration
import scala.util.matching.Regex


object BSONHandlers extends ScrupalComponent {

  /** FIXME: Handle reading/writing Type instances to and from BSON.
    * Note that types are a little special. We write them as strings and restore them via lookup. Types are intended
    * to only ever live in memory but they can be references in the database. So when a Type is a field of some
    * class that is stored in the database, what actually gets stored is just the name of the type.
    */
  class BSONHandlerForType[T <: Type] extends BSONHandler[JsString, T] {
    override def write(t: T): BSONString = BSONString(t.id.name)

    override def read(bson: BSONString): T = Type.as(Symbol(bson.value))
  }

  class BSONHandlerForLayout[T <: Layout] extends BSONHandler[BSONString, T] {
    override def write(t: T): BSONString = BSONString(t.id.name)

    override def read(bson: BSONString): T = Layout.as(Symbol(bson.value))
  }

  class BSONHandlerForHtmlTemplate[T <: Template] extends BSONHandler[BSONString, T] {
    override def write(t: T): BSONString = BSONString(t.id.name)

    override def read(bson: BSONString): T = Template.as(Symbol(bson.value))
  }

  implicit val IdentifierConverter = (id: Identifier) ⇒ reactivemongo.bson.BSONString(id.name)
  implicit val BSONObjectIDConverter = (id: BSONObjectID) ⇒ id

  implicit val IdentifierBSONHandler = new BSONHandler[BSONString, Identifier] {
    override def write(t: Identifier): BSONString = BSONString(t.name)

    override def read(bson: BSONString): Identifier = Symbol(bson.value)
  }

  implicit val ShortBSONHandler = new BSONHandler[BSONInteger, Short] {
    override def write(t: Short): BSONInteger = BSONInteger(t.toInt)

    override def read(bson: BSONInteger): Short = bson.value.toShort
  }

  implicit val URLHandler = new BSONHandler[BSONString, URL] {
    override def write(t: URL): BSONString = BSONString(t.toString)

    override def read(bson: BSONString): URL = new URL(bson.value)
  }

  implicit val DateTimeBSONHandler = new BSONHandler[BSONDateTime, DateTime] {
    override def write(t: DateTime): BSONDateTime = BSONDateTime(t.getMillis)

    override def read(bson: BSONDateTime): DateTime = new DateTime(bson.value)
  }

  implicit val AlertKindHandler = new BSONHandler[BSONString, AlertKind.Kind] {
    override def write(t: AlertKind.Kind): BSONString = BSONString(t.toString)

    override def read(bson: BSONString): AlertKind.Kind = AlertKind.withName(bson.value)
  }

  implicit val IconsHandler = new BSONHandler[BSONString, Icons.Kind] {
    override def write(t: Icons.Kind): BSONString = BSONString(t.toString)

    override def read(bson: BSONString): Icons.Kind = Icons.withName(bson.value)
  }

  implicit val RegexHandler: BSONHandler[BSONString, Regex] = new BSONHandler[BSONString, Regex] {
    override def write(t: Regex): BSONString = BSONString(t.pattern.pattern())

    override def read(bson: BSONString): Regex = new Regex(bson.value)
  }

  implicit val DurationHandler: BSONHandler[BSONLong, Duration] = new BSONHandler[BSONLong, Duration] {
    override def write(t: Duration): BSONLong = BSONLong(t.toNanos)

    override def read(bson: BSONLong): Duration = Duration(bson.value, TimeUnit.NANOSECONDS)
  }

  implicit val TypeHandler: BSONHandler[BSONString, Type] = new BSONHandlerForType[Type]
  implicit val IndexableTypeHandler: BSONHandler[BSONString, IndexableType] = new BSONHandlerForType[IndexableType]
  implicit val StructuredTypeHandler: BSONHandler[BSONString, StructuredType] = new BSONHandlerForType[StructuredType]
  implicit val BundleTypeHandler: BSONHandler[BSONString, BundleType] = new BSONHandlerForType[BundleType]


  implicit val EntityHandler: BSONHandler[BSONString, Entity] = new BSONHandler[BSONString, Entity] {
    override def write(m: Entity): BSONString = BSONString(m.id.name)

    override def read(bson: BSONString): Entity = Entity(Symbol(bson.value)).get
  }

  implicit val ModuleHandler: BSONHandler[BSONString, Module] = new BSONHandler[BSONString, Module] {
    override def write(m: Module): BSONString = BSONString(m.id.name)

    override def read(bson: BSONString): Module = Module(Symbol(bson.value)).get
  }

  implicit val AppSeqHandler: BSONHandler[BSONArray, Seq[Application]] = new BSONHandler[BSONArray, Seq[Application]] {
    override def write(sa: Seq[Application]): BSONArray = BSONArray(sa map { app ⇒ app._id.name })

    override def read(array: BSONArray): Seq[Application] =
      Application.find(array.values.toSeq.map { v ⇒ Symbol(v.asInstanceOf[BSONString].value) })
  }

  implicit val MediaTypeHandler: BSONHandler[BSONArray, MediaType] = new BSONHandler[BSONArray, MediaType] {
    override def write(mt: MediaType): BSONArray = BSONArray(mt.mainType, mt.subType)

    override def read(bson: BSONArray): MediaType = {
      val key = bson.values(0).asInstanceOf[BSONString].value → bson.values(1).asInstanceOf[BSONString].value
      MediaTypes.getForKey(key) match {
        case Some(mt) ⇒ mt
        case None ⇒ toss(s"MediaType `$key` is unknown.")
      }
    }
  }

  implicit val ArrayByteHandler: BSONHandler[BSONBinary, Array[Byte]] = new BSONHandler[BSONBinary, Array[Byte]] {
    override def write(bytes: Array[Byte]): BSONBinary = BSONBinary(bytes, Subtype.GenericBinarySubtype)

    override def read(bson: BSONBinary): Array[Byte] = bson.value.readArray(bson.value.size)
  }

  implicit val HtmlTemplateHandler = new BSONHandlerForHtmlTemplate[Html.Template]

  implicit val LayoutHandler: BSONHandler[BSONString, Layout] = new BSONHandlerForLayout[Layout]

  implicit val FileHandler: BSONHandler[BSONString, File] = new BSONHandler[BSONString, File] {
    override def write(f: File): BSONString = BSONString(f.getPath)

    override def read(bson: BSONString): File = new File(bson.value)
  }

  implicit val MapOfNamedHtml: BSONHandler[BSONDocument, Map[String, Html.Template]] =
    new BSONHandler[BSONDocument, Map[String, Html.Template]] {
      override def write(elements: Map[String, Html.Template]): BSONDocument = {
        BSONDocument(elements.map { case (name, html) ⇒ name → HtmlTemplateHandler.write(html) })
      }

      override def read(doc: BSONDocument): Map[String, Html.Template] = {
        doc.elements.map { case (name, value) ⇒ name -> HtmlTemplateHandler.read(value.asInstanceOf[BSONString]) }
      }.toMap
    }

  implicit val MapOfNamedDocumentsHandler = new BSONHandler[BSONDocument, Map[String, BSONValue]] {
    override def write(elements: Map[String, BSONValue]): BSONDocument = BSONDocument(elements)

    override def read(doc: BSONDocument): Map[String, BSONValue] = doc.elements.toMap
  }

  implicit val MapOfNamedObjectIDHandler = new BSONHandler[BSONDocument, Map[String, BSONObjectID]] {
    override def write(elements: Map[String, BSONObjectID]): BSONDocument = MapOfNamedDocumentsHandler.write(elements)

    override def read(doc: BSONDocument): Map[String, BSONObjectID] =
      MapOfNamedDocumentsHandler.read(doc).map { case (k, v) ⇒ k -> v.asInstanceOf[BSONObjectID] }
  }

  implicit val EitherNodeRefOrNodeHandler = new BSONHandler[BSONDocument, Either[NodeRef, Node]] {
    def write(e: Either[NodeRef, Node]): BSONDocument = {
      BSONDocument(
        "either" → (if (e.isLeft) BSONString("left") else BSONString("right")),
        "value" → (
          if (e.isLeft)
            NodeRef.nodeRefHandler.write(e.left.get)
          else
            Node.NodeWriter.write(e.right.get))
      )
    }

    def read(doc: BSONDocument): Either[NodeRef, Node] = {
      val value = doc.get("value").get.asInstanceOf[BSONDocument]
      doc.getAs[String]("either").get match {
        case "left" ⇒ Left[NodeRef, Node](NodeRef.nodeRefHandler.read(value))
        case "right" ⇒ Right[NodeRef, Node](Node.NodeReader.read(value))
      }
    }
  }

  implicit val MapOfNamedNodeRefHandler = new BSONHandler[BSONDocument, Map[String, Either[NodeRef, Node]]] {
    override def write(elements: Map[String, Either[NodeRef, Node]]): BSONDocument = {
      BSONDocument(elements.map { case (key, e) ⇒ key → EitherNodeRefOrNodeHandler.write(e) })
    }

    override def read(doc: BSONDocument): Map[String, Either[NodeRef, Node]] = {
      val elems = doc.elements.map { case (k, d) ⇒ k → EitherNodeRefOrNodeHandler.read(d.asInstanceOf[BSONDocument]) }
      elems.toMap
    }
  }

  implicit val EnablementHandler = new BSONHandler[BSONDocument, Enablement[_]] {
    override def write(e: Enablement[_]): BSONDocument =
      Reference.ReferenceHandler.write(new Reference[IdentifiedWithRegistry](e.id, e.registryName))

    override def read(doc: BSONDocument): Enablement[_] =
      Reference.ReferenceHandler.read(doc)().asInstanceOf[Enablement[_]]
  }

  implicit val AlertHandler = handler[Alert]

  case class AlertDAO(db: DefaultDB) extends IdentifierDAO[Alert] {
    implicit val reader: IdentifierDAO[Alert]#Reader = Macros.reader[Alert]
    implicit val writer: IdentifierDAO[Alert]#Writer = Macros.writer[Alert]

    final def collectionName: String = "alerts"

    override def indices: Traversable[Index] = super.indices ++ Seq(
      Index(key = Seq("_id" -> IndexType.Ascending), name = Some("UniqueId"))
    )
  }

  case class FeatureDAO(db: DefaultDB) extends IdentifierDAO[Feature] {
    final def collectionName = "features"

    implicit val reader: IdentifierDAO[Feature]#Reader = Macros.reader[Feature]
    implicit val writer: IdentifierDAO[Feature]#Writer = Macros.writer[Feature]

    override def indices: Traversable[Index] = super.indices ++ Seq(
      Index(key = Seq("module" -> IndexType.Ascending), name = Some("Module"))
    )
  }


  /** Data Access Object For Instances
    * This DataAccessObject sublcass represents the "instances" collection in the database and permits management of
    * that collection as well as conversion to and from BSON format.
    * @param db A parameterless function returning a [[reactivemongo.api.DefaultDB]] instance.
    */
  case class InstanceDAO(db: DefaultDB) extends IdentifierDAO[Instance] {
    final def collectionName = "instances"

    implicit val reader: IdentifierDAO[Instance]#Reader = Macros.reader[Instance]
    implicit val writer: IdentifierDAO[Instance]#Writer = Macros.writer[Instance]

    override def indices: Traversable[Index] = super.indices ++ Seq(
      Index(key = Seq("entity" -> IndexType.Ascending), name = Some("Entity"))
    )
  }

  case class PrincipalDAO(db : ScrupalDB) extends IdentifierDAO[Principal] {
    final def collectionName = "principals"
    implicit val reader : IdentifierDAO[Principal]#Reader = Macros.reader[Principal]
    implicit val writer : IdentifierDAO[Principal]#Writer = Macros.writer[Principal]
  }

  /*
import BSONHandlers._

// def kinds : Seq[String] = { variants.kinds }

//  object variants extends VariantRegistry[Site]("Site")
//  implicit val dtHandler = DateTimeBSONHandler
implicit lazy val SiteReader : VariantBSONDocumentReader[Site] = new VariantBSONDocumentReader[Site] {
  def read(doc : BSONDocument) : Site = variants.read(doc)
}

implicit val SiteWriter : VariantBSONDocumentWriter[Site] = new VariantBSONDocumentWriter[Site] {
  def write(site : Site) : BSONDocument = variants.write(site)
}

/** Data Access Object For Sites
  * This DataAccessObject sublcass represents the "sites" collection in the database and permits management of
  * that collection as well as conversion to and from BSON format.
  * @param db A [[reactivemongo.api.DefaultDB]] instance in which to find the collection
  */
case class SiteDAO(db : DefaultDB) extends VariantIdentifierDAO[Site] {
  final def collectionName : String = "sites"
  implicit val writer = new Writer(variants)
  implicit val reader = new Reader(variants)

  override def indices : Traversable[Index] = super.indices ++ Seq(
    Index(key = Seq("path" -> IndexType.Ascending), name = Some("path")),
    Index(key = Seq("kind" -> IndexType.Ascending), name = Some("kind"))
  )
}*/

  /*
/** Handle reading/writing Template instances to and from BSON.
  * Note that templates are a little special. We write them as strings and restore them via lookup. Templates are
  * intended to only ever live in memory but they can be references in the database. So when a Template is a field
  * of some class that is stored in the database, what actually gets stored is just the name of the template.
  */
class BSONHandlerForTemplate[T <: Registrable[_]] extends BSONHandler[BSONString, T] {
  override def write(t : T) : BSONString = BSONString(t.id.name)
  override def read(bson : BSONString) : T = Template.as(Symbol(bson.value))
}
  */

  /*
 import BSONHandlers._

 case class EntityDao(db : ScrupalDB) extends IdentifierDAO[Entity] {
   implicit val reader : Reader = EntityHandler.asInstanceOf[Reader]
   implicit val writer : Writer = EntityHandler.asInstanceOf[Writer]

   def collectionName : String = "entities"
 }
*/
}
