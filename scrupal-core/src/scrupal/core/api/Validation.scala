package scrupal.core.api

import reactivemongo.bson.{BSONArray, BSONDocument, BSONValue}

case class ValidationError[T<:Type](t: T, errors: Seq[String], value: BSONValue) extends Exception {
  def this(t: T, error: String, value: BSONValue) = this(t, Seq(error), value)
  override def getMessage: String = {
    s"Failed to validate type ${t.asT.label}:\n ${errors.map{ e => "\t"+e+"\n"}}\nfor bson data:\n$value"
  }
}

trait SelfValidator {
  def validate() : ValidationResult
}

/** Generic Value Validator as a Function. You can apply these validations in other validations making them
  * composable.
 */
trait ValueValidator[T <: BSONValue] extends ((T) => ValidationResult) {

  protected def validate(values: Seq[T], elemType: ValueValidator[T]) : ValidationResult = {
    val errors = { for (v <- values; e = elemType(v) if e.isDefined) yield { e.get } }.flatten.toSeq
    if (errors.isEmpty)
      None
    else
      Some(errors)
  }

  protected def single(value: T)(validator: (T) => Option[String]): ValidationResult = {
    validator(value) map { theError => Seq(theError) }
  }

  protected def wrongClass(expected: String, actual: T) =
    Some(s"Expected value of type $expected but got type ${actual.getClass.getSimpleName}")
}

trait BSONValidator[BSONValueType <: BSONValue] extends ValueValidator[BSONValueType] {

  type BVT = BSONValidator[BSONValueType]

  /** The validation method for validating a JsArray
    * This traverses the array and validates that each element conforms to the `elemType`
    * @param value The JsArray to be validated
    * @param elemType The Type each element of the array should have. By default
    * @return JsSuccess(true) when valid, JsError otherwise
    */
  protected def validate(value : BSONArray, elemType: BVT) : ValidationResult = {
    val errors = {
      for (
        v <- value.values;
        e = elemType(v.asInstanceOf[BSONValueType]) if e.isDefined
      ) yield { e.get }
    }.flatten.toSeq
    if (errors.isEmpty)
      None
    else
      Some(errors)
  }

  protected def validateArray(a: BSONArray, validators: Seq[BVT]) : ValidationResult = {
    val combine = for (
      item <- validators.zip(a.values);
      result = item._1(item._2.asInstanceOf[BSONValueType]) if result.isDefined
    ) yield result

    val list = {combine.flatMap { _.toSeq }}.flatten
    if (list.isEmpty)
      None
    else
      Some(list)
  }

  protected def validateMaps(document: BSONValueType,
    validators: Map[String,BSONValidator[BSONValue]],
    defaults: Map[String,BSONValue]) : ValidationResult =
  {
    document match {
      case doc: BSONDocument ⇒
        val elems = doc.elements.toMap // read it all in once, we'll look at everything in the typical case
      val combined = for ( (key,validator) ← validators) yield {
          if (elems.contains(key)) {
            validator(elems.get(key).get)
          } else if (defaults.contains(key)) {
            validator(defaults.get(key).get)
          } else {
            Some(Seq(s"Element '$key' is missing and has no default."))
          }
        }
        val list = { combined.flatMap { _.toSeq }}.flatten
        if (list.isEmpty)
          None
        else
          Some(list.toSeq)
      case x: BSONValue ⇒
        Some(Seq(s"Expected a BSONDocument but got: '$x'"))
    }
  }


}
