package scrupal.core.api


import java.util.concurrent.TimeUnit

import scala.concurrent.duration.Duration

import reactivemongo.bson._
import scrupal.core._
import scrupal.utils.ScrupalComponent

trait SettingsInterface {

  def validate(doc: BSONDocument) : ValidationResult
  def validate(path: String) : ValidationResult

  def getString(path: String) : Option[String]
  def getBoolean(path: String) : Option[Boolean]
  def getInt(path: String) : Option[Int]
  def getLong(path: String) : Option[Long]
  def getDouble(path: String) : Option[Double]
  def getNumber(path: String) : Option[Number]
  def getDuration(path: String) : Option[Duration]
  def getMilliseconds(path: String) : Option[Long]
  def getNanoseconds(path: String) : Option[Long]

  def keySet : Set[String] = ???
  def entrySet: Set[BSONValue] = ???

  def getConfiguration(path: String): Option[Settings] = ???
  def getConfigurations(path: String): Option[Seq[Settings]] = ???

  def getBooleans(path: String): Option[Seq[Boolean]] = ???
  def getBytes(path: String) : Option[Seq[Long]] = ???
  def getDoubles(path: String): Option[Seq[Double]] = ???
  def getInts(path: String): Option[Seq[Int]] = ???
  def getLongs(path: String): Option[Seq[Long]] = ???
  def getNumbers(path: String): Option[Seq[Number]] = ???
  def getStrings(path: String): Option[Seq[String]] = ???

  def setString(path: String, value: String) : Unit = ???
  def setBoolean(path: String, value: Boolean) : Unit
  def setInt(path: String, value: Int) : Unit = ???
  def setLong(path: String, value: Long) : Unit = ???
  def setDouble(path: String, value: Double) : Unit = ???
  def setNumber(path: String, value: Number) : Unit = ???
  def setDuration(path: String, value: Duration) : Unit = ???

  def setBooleans(path: String, value: Seq[Boolean]) : Unit = ???
  def setBytes(path: String, value: Seq[Byte]) : Unit = ???
  def setDoubles(path: String, value: Seq[Double]) : Unit = ???
  def setInts(path: String, value: Seq[Int]) : Unit = ???
  def setLongs(path: String, value: Seq[Long]) : Unit = ???
  def setNumbers(path: String, value: Seq[Number]) : Unit = ???
  def setStrings(path: String, value: Seq[String]) : Unit = ???

  def toConfig : com.typesafe.config.Config = ???
}

/**
 * Created by reidspencer on 11/10/14.
 */
case class Settings(
  types: StructuredType,
  defaults: Map[String,BSONValue],
  values: BSONDocument
) extends ScrupalComponent with SettingsInterface with DefaultBSONHandlers with BSONValidator[BSONDocument] {
  require(types.size == defaults.size)
  def apply(doc: BSONDocument) : ValidationResult = validateMaps(doc, types.fields, defaults)

  def validate(doc: BSONDocument) : ValidationResult = this.apply(doc)

  def validate(path: String) : ValidationResult = {
    BSONPathWalker(path,values) match {
      case None ⇒ Some(Seq(s"Path '$path' was not found amongst the values."))
      case Some(bv) ⇒
        TypePathWalker(path,types) match {
          case None ⇒ Some(Seq(s"Path '$path' exists in configuration but the configuration has no type for it"))
          case Some(validator) ⇒ validator(bv)
        }
    }
  }

  implicit val bsonStringReader = BSONStringHandler.asInstanceOf[BSONReader[BSONValue,String]]

  def getString(path: String) : Option[String] = BSONPathWalker(path,values).map{ s ⇒ s.asInstanceOf[BSONString].as[String] }
  def getBoolean(path: String) : Option[Boolean] = BSONPathWalker(path, values).map { b ⇒ b.asInstanceOf[BSONBoolean].as[Boolean] }
  def getInt(path: String) : Option[Int] = BSONPathWalker(path,values).map { i ⇒ i.asInstanceOf[BSONInteger].as[Int] }
  def getLong(path: String) : Option[Long] = BSONPathWalker(path, values).map { l ⇒ l.asInstanceOf[BSONLong].as[Long] }
  def getDouble(path: String) : Option[Double] = BSONPathWalker(path, values).map { l ⇒ l.asInstanceOf[BSONDouble].as[Double] }
  def getNumber(path: String) : Option[Number] = BSONPathWalker(path, values).map { l ⇒ l.asInstanceOf[BSONNumberLike].toDouble }
  def getDuration(path: String) : Option[Duration] = BSONPathWalker(path, values).map { d ⇒ Duration(d.asInstanceOf[BSONLong].value, TimeUnit.SECONDS)  }
  def getMilliseconds(path: String) : Option[Long] = getDuration(path).map { d ⇒ d.toMillis }
  def getNanoseconds(path: String) : Option[Long] = getDuration(path).map { d ⇒ d.toNanos }


  override def keySet : Set[String] = values.elements.map { x ⇒ x._1 } toSet
  override def entrySet: Set[BSONValue] = values.elements.map { x ⇒ x._2 } toSet

  // TODO: Implement remaining methods of the ConfigurationInterface in the Configuration class

  def setBoolean(path: String, value: Boolean) : Unit = ???

}

trait PathWalker[D <: AnyRef, A <: AnyRef, V <: AnyRef] {
  protected def isDocument(v: V) : Boolean
  protected def isArray(v: V) : Boolean
  protected def asDocument(v: V) : D
  protected def asArray(v: V) : A
  protected def indexDoc(key: String, d: D) : Option[V]
  protected def indexArray(index: Int, a: A) : Option[V]
  protected def arrayLength(a: A) : Int

  def lookup(path: String, document: D) : Option[V] = {

    def error(msg: String, head: Seq[String], paths: Seq[String]) = {
      toss(s"${msg} at path '${head.mkString(".")}.${
        paths.head
      }' with remaining path elements: ${
        paths.tail.mkString(".")
      }")
    }

    def getAsDocument(head: Seq[String], paths: Seq[String], value: V) : D = {
      if (isDocument(value)) {
        asDocument(value)
      } else {
        error("Selected value was not a document so it could not be indexed", head, paths)
      }
    }

    def resolve(head: Seq[String], paths: Seq[String], doc: D) : Option[V] = {
      val name = paths.head
      require(paths.length > 0)
      val deref : Option[V] = if (name.endsWith("]")) {
        val beginIndex = name.lastIndexOf("[") + 1
        if (beginIndex <= 0)
          error("Malformed array subscripts", head, paths)
        else {
          val endIndex = name.length - 1
          val index = name.substring(beginIndex, endIndex).toInt
          val rootName = name.substring(0, beginIndex-1)
          indexDoc(rootName, doc) match {
            case Some(value) ⇒
              if (isArray(value)) {
                val anArray = asArray(value)
                if (index < 0 || index > arrayLength(anArray)-1)
                  error(s"Array subscript out of bounds ", head, paths)
                else {
                  indexArray(index, anArray)
                }
              } else
                error("Selected value was not an array so it could not be indexed", head, paths)
            case None ⇒ None
          }
        }
      } else {
        indexDoc(name, doc) match {
          case Some(value) ⇒ Some(value)
          case None ⇒ None
        }
      }
      if (paths.length == 1) {
        deref
      } else if (deref.isEmpty) {
        None
      } else {
        val nextDoc = getAsDocument(head, paths, deref.get)
        resolve(head :+ name, paths.tail, nextDoc)
      }
    }
    val parts = path.split(".").toSeq
    resolve(Seq.empty[String], parts, document)
  }

}

object BSONPathWalker extends PathWalker[BSONDocument,BSONArray,BSONValue] {
  protected def isDocument(v: BSONValue) : Boolean = v.code == 0x03
  protected def isArray(v: BSONValue) : Boolean = v.code == 0x04
  protected def asArray(v: BSONValue) : BSONArray = v.asInstanceOf[BSONArray]
  protected def asDocument(v: BSONValue) : BSONDocument = v.asInstanceOf[BSONDocument]
  protected def indexDoc(key: String, d: BSONDocument) : Option[BSONValue] = d.get(key)
  protected def indexArray(index: Int, a: BSONArray) : Option[BSONValue] = {
    if (index < 0 || index > a.length) None else Some(a.values(index))
  }
  protected def arrayLength(a: BSONArray) : Int = a.length
  def apply(path: String, doc: BSONDocument) : Option[BSONValue] = lookup(path, doc)
}

object TypePathWalker extends PathWalker[DocumentType, IndexableType, Type] {
  protected def isDocument(v: Type) : Boolean = v.isInstanceOf[DocumentType]
  protected def isArray(v: Type) : Boolean = v.isInstanceOf[IndexableType]
  protected def asArray(v: Type) : IndexableType = v.kind match {
    case 'List ⇒ v.asInstanceOf[ListType]
    case 'Set  ⇒ v.asInstanceOf[SetType]
    case _ ⇒ toss("Attempt to coerce a non-array type into an array type")
  }
  protected def asDocument(v: Type) : DocumentType = v.kind match {
    case 'Bundle ⇒ v.asInstanceOf[BundleType]
    case 'Map ⇒ v.asInstanceOf[MapType]
    case 'Node ⇒ v.asInstanceOf[NodeType]
    case _ ⇒ toss("Attempt to coerce a non-array type into an array type")
  }
  protected def indexDoc(key: String, d: DocumentType) : Option[Type] = d.validatorFor(key)
  protected def indexArray(index: Int, a: IndexableType) : Option[Type] = Some(a.elemType)
  protected def arrayLength(a: IndexableType) : Int = Int.MaxValue // WARNING: Really? MaxValue?
  def apply(path: String, doc: DocumentType) : Option[Type] = lookup(path, doc)
}


object Settings {

  implicit val ConfigurationHandler = Macros.handler[Settings]

  def apply(cfg: com.typesafe.config.Config) : Settings = ???
  // TODO: Implement conversion of Configuration from Typesafe Config with "best guess" at Type from values

  val Empty = Settings(BundleType.Empty,Map.empty[String,BSONValue], BSONDocument() )
}
