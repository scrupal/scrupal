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

import org.joda.time.DateTime
import reactivemongo.bson._
import scrupal.core.api.Html._
import scrupal.core.html.Forms._
import scrupal.core.types._
import scrupal.db.Storable
import scalatags.Text.all._

/** Scrupal Forms
  * This object just contains the form related things. Note that this is a very general notion of forms and not
  * much tied to the HTML ideas. Forms are Inputs inside Sections inside Pages. Inputs are named Types. All of
  * these are storable and are Validators so we can validate a BSON document against a form. This should be used
  * for all input to Scrupal.
  */
object Forms {

  trait FormItem extends Storable[String] with BSONValidator[BSONValue] {
    def render(form: Form) : TagContent
  }

  trait FieldItem extends FormItem with Nameable with Describable

  trait Container extends FieldItem {
    def fields: Seq[FieldItem]
    def apply(value: BSONValue) : ValidationResult = validateArray(value.asInstanceOf[BSONArray], fields)
  }

  trait Field extends FieldItem {
    def fieldType : Type
    def default: BSONValue
    def attrs: AttrList

    def apply(value: BSONValue) : ValidationResult = {
      fieldType(value)
    }

    require(fieldType.nonTrivial)
  }

  /** A Text Field.
    *
    * This wraps a name and a description around a Type, a default BSONValue for that Type, and options for the
    * type.
    * @param _id
    * @param description
    * @param default
    * @param attrs
    */
  case class StringField(
    _id: String,
    name: String,
    description: String,
    fieldType: StringType,
    default: BSONString = BSONString(""),
    attrs: AttrList = EmptyAttrList
  ) extends Field {
    def render(form: Form) : TagContent = {
      text(name, form.values.getString(name), attrs)
    }
  }

  case class PasswordField(
    _id: String,
    name: String,
    description: String,
    fieldType: StringType = Password_t,
    default: BSONString = BSONString(""),
    attrs: AttrList = EmptyAttrList
  ) extends Field {
    def render(form: Form) : TagContent = {
      password(name, form.values.getString(name), attrs)
    }
  }

  case class TextAreaField(
    _id: String,
    name: String,
    description: String,
    fieldType: StringType,
    default: BSONString = BSONString(""),
    attrs: AttrList = EmptyAttrList
    ) extends Field {
    def render(form: Form) : TagContent = {
      scrupal.core.html.Forms.textarea(name, form.values.getString(name), attrs)
    }
  }

  case class BooleanField(
    _id: String,
    name: String,
    description: String,
    fieldType: BooleanType,
    default: BSONBoolean = BSONBoolean(value=false),
    attrs: AttrList = EmptyAttrList
  ) extends Field {
    def render(form: Form) : TagContent = {
      checkbox(name, form.values.getBoolean(name).getOrElse(false), attrs)
    }
  }

  case class IntegerField(
    _id: String,
    name: String,
    description: String,
    fieldType : RangeType,
    default: BSONLong = BSONLong(0L),
    attrs: AttrList = EmptyAttrList,
    minVal: Long = Long.MinValue,
    maxVal: Long = Long.MaxValue
    ) extends Field {
    def render(form: Form) : TagContent = {
      number(name, form.values.getDouble(name), minVal.toDouble, maxVal.toDouble, attrs)
    }
  }

  case class RealField(
    _id: String,
    name: String,
    description: String,
    fieldType : RealType,
    default: BSONDouble = BSONDouble(0.0),
    attrs: AttrList = EmptyAttrList,
    minVal: Double = Double.MinValue,
    maxVal: Double = Double.MaxValue
    ) extends Field {
    def render(form: Form) : TagContent = {
      number(name, form.values.getDouble(name), minVal, maxVal, attrs)
    }
  }

  case class RangeField(
    _id: String,
    name: String,
    description: String,
    fieldType : RealType,
    default: BSONDouble = BSONDouble(0.0),
    attrs: AttrList = EmptyAttrList,
    minVal: Double = 0L,
    maxVal: Double = 100L
  ) extends Field {
    def render(form: Form) : TagContent = {
      range(name, form.values.getDouble(name), minVal, maxVal, attrs)
    }
  }

  case class SelectionField(
    _id: String,
    name: String,
    description: String,
    fieldType : SelectionType,
    default: BSONString = BSONString(""),
    attrs: AttrList = EmptyAttrList
    ) extends Field {
    def render(form: Form) : TagContent = {
      val options = fieldType.choices.map { choice ⇒ choice → choice}
      scrupal.core.html.Forms.select(name, form.values.getString(name), options.toMap, attrs)
    }
  }

  case class TimestampField(
    _id: String,
    name: String,
    description: String,
    fieldType : TimestampType,
    default: BSONString = BSONString(""),
    attrs: AttrList = EmptyAttrList
  ) extends Field {
    def render(form: Form) : TagContent = {
      datetime(name, form.values.getInstant(name).map { i ⇒ i.toDateTime }, attrs)
    }
  }

  case class FieldSet(
    _id: String,
    name: String,
    description: String,
    title: String,
    fields: Seq[Field],
    default: BSONValue = BSONNull
  ) extends Container {
    require(fields.nonEmpty)
    def render(form: Form) : TagContent = {
      fieldset(legend(title), fields.map { field ⇒ field.render(form) } )
    }
  }

  case class SubmitAction(_id: String, handler: Option[Handler]) extends BSONValidator[BSONValue] {
    def apply(value: BSONValue): ValidationResult = None // TODO: Implement this
  }

  trait AbstractForm extends FormItem {
    def submit: SubmitAction
    def fields: Seq[FormItem]
    def values: Settings
    def defaults: Settings

    def apply(value: BSONValue): ValidationResult = {
      value match {
        case x: BSONDocument if x.getAs[BSONString]("_id").getOrElse(BSONString("")).value != _id ⇒
          Some(Seq(s"Form field '_id' does not correspond to '${_id}'"))
        case x: BSONDocument if x.get("submit").isEmpty =>  Some(Seq(s"Document has no field named 'submit'"))
        case x: BSONDocument if x.get("fields").isEmpty =>   Some(Seq("Document has no field named 'fields'"))
        case x: BSONDocument =>
          val submitVal = x.get("submit").get
          val pagesVal = x.get("fields").get
          submit(submitVal).map {
            result => result ++ {
              pagesVal match {
                case a: BSONArray if a.length != fields.length =>
                  Seq(s"Number of inputs in document doesn't match expected number for section ${_id}")
                case a: BSONArray => validateArray(a, fields).getOrElse(Seq.empty[String])
                case x: BSONValue => Seq(wrongClass("BSONArray", x).getOrElse(""))
              }
            }
          }
        case x: BSONValue => single(value) { _ => wrongClass("BSONDocument", x)}
      }
    }

    def render(form: Form) : TagContent = {
      scalatags.Text.tags.form()
    }
  }

  case class Form(
    _id: String,
    submit: SubmitAction,
    fields: Seq[FieldItem],
    values: Settings = Settings.Empty,
    defaults: Settings = Settings.Empty
  ) extends AbstractForm {
    require(fields.length > 0)
    def render : String = render(this).toString()
    def renderField(fieldContent: TagContent, field_label: TagContent, field_help: String) = {
    }
  }

  // type InputGenerator = (Input) ⇒ Html
  // type InputWrapper = (Input,Html) ⇒ Html
}
