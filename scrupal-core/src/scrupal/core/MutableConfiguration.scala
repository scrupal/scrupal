package scrupal.core

import java.util.concurrent.atomic.AtomicReference

import reactivemongo.bson.BSONValue
import scrupal.core.api.{BSONValidator, ValidationResult, Type}
import scrupal.utils.Configuration

/** A Configuration that can validate and mutate
 * Created by reidspencer on 11/7/14.
 */
case class MutableConfiguration(types: Map[String,Type], defaults: Map[String,BSONValue]) extends BSONValidator {
  require(types.size == defaults.size)
  private[this] val _config = new AtomicReference[Configuration]
  def apply(doc: BSONValue) : ValidationResult = validateMaps(doc, types, defaults)
}

object EmptyMutableConfiguration extends MutableConfiguration(Map.empty[String,Type], Map.empty[String,BSONValue])
