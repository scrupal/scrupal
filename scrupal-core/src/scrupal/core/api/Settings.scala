package scrupal.core.api


import scrupal.db.{BSONSettingsInterface, BSONSettings, BSONPathWalker}

import reactivemongo.bson._
import scrupal.core._
import scrupal.utils.PathWalker

/** Interface To Settings
  * This defines the interface to value extraction from some cache of settings
  *
  * FIXME: Extract the BSON Specific parts of the implementations and put in scrupal-db
  */
trait SettingsInterface extends BSONSettingsInterface {

  def validate(doc: BSONDocument) : ValidationResult
  def validate(path: String) : ValidationResult
}

/**
 * Created by reidspencer on 11/10/14.
 */
case class Settings(
  types: StructuredType,
  override val defaults: BSONDocument,
  override val values: BSONDocument
) extends BSONSettings(values,defaults) with SettingsInterface with BSONValidator[BSONDocument] {
  require(types.size == defaults.elements.size)
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

  val Empty = Settings(BundleType.Empty, BSONDocument(), BSONDocument() )
}
