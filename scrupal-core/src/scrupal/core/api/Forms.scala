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

import reactivemongo.bson._
import scrupal.core.api.Html._
import scrupal.core.html.Forms._
import scrupal.core.types._
import scrupal.utils.Enablee
import spray.http.Uri
import spray.routing.PathMatcher.{Unmatched, Matched}
import spray.routing.PathMatchers
import scalatags.Text.all._
import scalatags.Text.attrs

/** Scrupal Forms
  * This object just contains the form related things. Note that this is a very general notion of forms and not
  * much tied to the HTML ideas. Forms are Inputs inside Sections inside Pages. Inputs are named Types. All of
  * these are storable and are Validators so we can validate a BSON document against a form. This should be used
  * for all input to Scrupal.
  */
object Forms {

  trait FormItem extends BSONValidator[BSONValue] {
    def render(form: Form) : TagContent
  }

  trait FieldItem extends FormItem with Nameable with Describable {
    def inline : Boolean
    def prefix : Boolean
  }

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
    * @param description
    * @param default
    * @param attrs
    */
  case class StringField(
    name: String,
    description: String,
    fieldType: StringType,
    default: BSONString = BSONString(""),
    attrs: AttrList = EmptyAttrList,
    inline : Boolean = false,
    prefix : Boolean = false
  ) extends Field {
    def render(form: Form) : TagContent = {
      text(name, form.values.getString(name), attrs ++ Seq(title:=description))
    }
  }

  case class PasswordField(
    name: String,
    description: String,
    fieldType: StringType = Password_t,
    default: BSONString = BSONString(""),
    attrs: AttrList = EmptyAttrList,
    inline : Boolean = false,
    prefix : Boolean = false
  ) extends Field {
    def render(form: Form) : TagContent = {
      password(name, form.values.getString(name), attrs ++ Seq(title:=description))
    }
  }

  case class TextAreaField(
    name: String,
    description: String,
    fieldType: StringType,
    default: BSONString = BSONString(""),
    attrs: AttrList = EmptyAttrList,
    inline : Boolean = false,
    prefix : Boolean = false
    ) extends Field {
    def render(form: Form) : TagContent = {
      scrupal.core.html.Forms.textarea(name, form.values.getString(name), attrs ++ Seq(title:=description))
    }
  }

  case class BooleanField(
    name: String,
    description: String,
    fieldType: BooleanType,
    default: BSONBoolean = BSONBoolean(value=false),
    attrs: AttrList = EmptyAttrList,
    inline : Boolean = false,
    prefix : Boolean = false
  ) extends Field {
    def render(form: Form) : TagContent = {
      checkbox(name, form.values.getBoolean(name).getOrElse(false), attrs ++ Seq(title:=description))
    }
  }

  case class IntegerField(
    name: String,
    description: String,
    fieldType : RangeType,
    default: BSONLong = BSONLong(0L),
    attrs: AttrList = EmptyAttrList,
    minVal: Long = Long.MinValue,
    maxVal: Long = Long.MaxValue,
    inline : Boolean = false,
    prefix : Boolean = false
    ) extends Field {
    def render(form: Form) : TagContent = {
      number(name, form.values.getDouble(name), minVal.toDouble, maxVal.toDouble, attrs ++ Seq(title:=description))
    }
  }

  case class RealField(
    name: String,
    description: String,
    fieldType : RealType,
    default: BSONDouble = BSONDouble(0.0),
    attrs: AttrList = EmptyAttrList,
    minVal: Double = Double.MinValue,
    maxVal: Double = Double.MaxValue,
    inline : Boolean = false,
    prefix : Boolean = false
    ) extends Field {
    def render(form: Form) : TagContent = {
      number(name, form.values.getDouble(name), minVal, maxVal, attrs ++ Seq(title:=description))
    }
  }

  case class RangeField(
    name: String,
    description: String,
    fieldType : RealType,
    default: BSONDouble = BSONDouble(0.0),
    attrs: AttrList = EmptyAttrList,
    minVal: Double = 0L,
    maxVal: Double = 100L,
    inline : Boolean = false,
    prefix : Boolean = false
  ) extends Field {
    def render(form: Form) : TagContent = {
      range(name, form.values.getDouble(name), minVal, maxVal, attrs ++ Seq(title:=description))
    }
  }

  case class SelectionField(
    name: String,
    description: String,
    fieldType : SelectionType,
    default: BSONString = BSONString(""),
    attrs: AttrList = EmptyAttrList,
    inline : Boolean = false,
    prefix : Boolean = false
    ) extends Field {
    def render(form: Form) : TagContent = {
      val options = fieldType.choices.map { choice ⇒ choice → choice}
      scrupal.core.html.Forms.select(name, form.values.getString(name), options.toMap, attrs ++ Seq(title:=description))
    }
  }

  case class TimestampField(
    name: String,
    description: String,
    fieldType : TimestampType,
    default: BSONString = BSONString(""),
    attrs: AttrList = EmptyAttrList,
    inline : Boolean = false,
    prefix : Boolean = false
  ) extends Field {
    def render(form: Form) : TagContent = {
      datetime(name, form.values.getInstant(name).map { i ⇒ i.toDateTime }, attrs ++ Seq(title:=description))
    }
  }

  case class FieldSet(
    name: String,
    description: String,
    title: String,
    fields: Seq[Field],
    attrs: AttrList = EmptyAttrList,
    inline : Boolean = false,
    prefix : Boolean = false
  ) extends Container {
    require(fields.nonEmpty)
    def render(form: Form) : TagContent = {
      fieldset(scalatags.Text.attrs.title:=description, legend(title), fields.map { field ⇒ field.render(form) } )
    }
  }

  case class ResetField(
    name: String,
    description: String,
    label: String = "Reset",
    attrs: AttrList = EmptyAttrList,
    inline : Boolean = false,
    prefix : Boolean = false
  ) extends FieldItem {
    def render(form: Form) : TagContent = {
      reset(name, Some(label), attrs ++ Seq(title:=description))
    }
    def apply(value: BSONValue) : ValidationResult = None
  }

  case class SubmitField(
    name: String,
    description: String,
    label: String,
    frmaction: Option[String] = None,
    attrs: AttrList = EmptyAttrList,
    inline : Boolean = false,
    prefix : Boolean = false
  ) extends FieldItem {
    def render(form: Form) : TagContent = {
      submit(name, label, attrs ++ Seq(title:=description) ++
        (frmaction match { case Some(x) ⇒ Seq(formaction:=x); case _ ⇒ Seq.empty[AttrPair]} )
      )
    }
    def apply(value: BSONValue) : ValidationResult = None
  }

  trait Form extends Enablee with FormItem with Nameable with Describable with TerminalActionProvider {
    def actionPath : String
    def fields: Seq[FieldItem]
    def values: Settings

    def hasErrors(field: FieldItem) : Boolean = false // TODO: Implement Form.hasErrors
    def errorsOf(field: FieldItem) : Seq[String] = Seq.empty[String] // TODO: Implement Form.errorsOf

    def apply(value: BSONValue): ValidationResult = {
      value match {
        case x: BSONDocument if x.get("action").isEmpty => Some(Seq("Document has no field named 'action'"))
        case x: BSONDocument if x.get("fields").isEmpty => Some(Seq("Document has no field named 'fields'"))
        case x: BSONDocument if !x.get("action").get.isInstanceOf[BSONString] ⇒
          Some(Seq("The 'action' field must be a string"))
        case x: BSONDocument if !x.get("fields").get.isInstanceOf[BSONArray] ⇒
          Some(Seq("The 'fields' field must be an array"))
        case x: BSONDocument ⇒
          val fieldsVal = x.get("fields").get
          fieldsVal match {
            case a: BSONArray if a.length != fields.length =>
              Some(Seq(s"Number of fields in document doesn't match expected number."))
            case a: BSONArray => validateArray(a, fields)
            case x: BSONValue => Some(Seq(wrongClass("BSONArray", x).getOrElse("")))
          }
        case x: BSONValue => single(value) { _ => wrongClass("BSONDocument", x)}
      }
    }

    /**
     * Wrap a field's rendered element with an appropriate label and other formatting. This default is aimed at
     * Twitter bootstrap which is the default formatting for Scrupal.
     *
     * @param form The form that specifies how to wrap the field
     * @param field The field whose rendered output should be wrapped
     * @return The TagContent for the final markup for the field
     */
    def wrap(form: Form, field: FieldItem) : TagContent = {
      val the_label = scrupal.core.html.Forms.label(field.name, field.name, Seq(cls:="control-label text-info"))
      val the_field = field.render(form)
      div(cls:="clearfix form-group" + (if (hasErrors(field)) " text-danger" else ""),
        if (field.inline) {
          if (field.prefix) {
            Seq(div(the_field,"&nbsp;",the_label))
          } else {
            Seq(div(the_label,": ", the_field))
          }
        } else {
          if (field.prefix) {
            Seq(div(field.render(form)),the_label)
          } else {
            Seq(the_label, div(the_field))
          }
        },
        for(error ← form.errorsOf(field)) yield {
          div(cls:="text-danger small col-md-10", style:="padding-left:0;padding-top:0;margin-top:0;", error)
        },
        if (field.description.nonEmpty) {
          Seq(div(cls := "help-block text-muted small col-md-10", style := "padding-left:0;padding-top:0;",
            field.description))
        } else Seq.empty[AttrPair]
        // if(!field.url.isEmpty) { a(target:="_blank", href:=field.url, "More Help&hellip;") }
      )
    }

    def render(form: Form) : TagContent = {
      scalatags.Text.tags.form(scalatags.Text.attrs.action:=actionPath, method:="POST", attrs.name:=name,
                               "enctype".attr:="application/x-www-form-urlencoded",
        fields.map { field ⇒ wrap(form, field) }
      )
    }

    def render : TagContent = render(this)

    /** Resolve an Action
      *
      * Given a path and a context, find the matching PathToAction and then invoke it to yield the corresponding Action.
      * A subclass must implement this method.
      *
      * @param key The key used to select this ActionProvider
      * @param path The path to use to match the PathToAction function
      * @param context The context to use to match the PathToAction function
      * @return
      */
    def actionFor(key: String, path: Uri.Path, context: Context) : Option[Action] = {
      if (key == singularKey || key == pluralKey) {
        PathMatchers.separateOnSlashes(actionPath)(path) match {
          case Matched(pathRest, extractions) ⇒
            if (pathRest.isEmpty)
              action(context)
            else
              None
          case Unmatched ⇒ None
        }
      } else {
        None
      }
    }

    def action(context: Context) : Option[Action]
  }

  case class SimpleForm(
    id: Symbol,
    name: String,
    description: String,
    actionPath: String,
    fields: Seq[FieldItem],
    values: Settings = Settings.Empty
  ) extends Form {
    require(fields.length > 0)
    def action(context: Context) : Option[Action] = {
      log.warn("Action Generation Not Implemented")
      None
    }
  }

  object emptyForm extends Form {
    val id = 'emptyForm; val name = ""; val description = ""; val actionPath = ""
    val fields = Seq.empty[FieldItem]
    val values = Settings.Empty
    def action(context: Context) : Option[Action] = None
  }
}
