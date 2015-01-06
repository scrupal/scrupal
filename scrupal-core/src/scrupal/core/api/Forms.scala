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
import spray.http.HttpMethods
import scala.concurrent.Future
import scalatags.Text.all._
import scalatags.Text.attrs

/** Scrupal Forms
  * This object just contains the form related things. Note that this is a very general notion of forms and not
  * much tied to the HTML ideas. Forms are Inputs inside Sections inside Pages. Inputs are named Types. All of
  * these are storable and are Validators so we can validate a BSON document against a form. This should be used
  * for all input to Scrupal.
  */
object Forms {

  trait FormItem extends Nameable with Describable with BSONValidator with ValidationLocation {
    def render(form: Form) : TagContent
    def defaultValue : BSONValue
  }

  trait Field extends FormItem  {
    def inline : Boolean
    def prefix : Boolean
    def fieldType : Type
    def attrs: AttrList
    def validate(value: BSONValue) : VR = validate(this, value)
    def validate(ref: ValidationLocation, value: BSONValue) : VR = {
      fieldType.validate(this, value) match {
        case x: ValidationSucceeded[BSONValue] ⇒ ValidationSucceeded[BSONValue](this,x.value)
        case x: ValidationFailed[BSONValue] ⇒ ValidationFailed[BSONValue](this, x.value, x.errors)
        case x: ValidationError[BSONValue] ⇒ ValidationError(this, x.value, x.errors)
        case x: ValidationException[BSONValue] ⇒ ValidationException(this, x.value, x.cause)
        case x: TypeValidationError[BSONValue,_] ⇒ TypeValidationError(this, x.value, x.t, x.errors)
      }
    }
    require(fieldType.nonTrivial)
  }

  trait Container extends FormItem {
    def items: Seq[FormItem]
    lazy val fieldMap : Map[String,FormItem] = { items.map { field ⇒ field.name → field } }.toMap

    override def index(key: Int) : Option[ValidationLocation] = {
      key match {
        case i: Int if i >= 0 && i < items.size ⇒ Some(items(key))
        case _ ⇒ None
      }
    }

    override def get(key: String) : Option[ValidationLocation] = {
      items.find { p ⇒ p.name == key }
    }

    def validate( value: BSONValue) : VR = validate(this, value)

    def validate(ref: ValidationLocation, value: BSONValue): VR = {
      value match {
        case x: BSONDocument ⇒
          validateMaps(this, x, fieldMap, defaultValue)
        case x: BSONValue => wrongClass(this, x, "BSONDocument")
      }
    }

    def defaultValue : BSONDocument = {
      BSONDocument(
        for (field <- items) yield { field.name → field.defaultValue }
      )
    }
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
    defaultValue: BSONValue = BSONString(""),
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
    defaultValue: BSONValue = BSONString(""),
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
    defaultValue: BSONValue = BSONString(""),
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
    defaultValue: BSONValue = BSONBoolean(value=false),
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
    defaultValue: BSONValue = BSONLong(0L),
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
    defaultValue: BSONValue = BSONDouble(0.0),
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
    defaultValue: BSONValue = BSONDouble(0.0),
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
    defaultValue: BSONValue = BSONString(""),
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
    defaultValue: BSONValue = BSONString(""),
    attrs: AttrList = EmptyAttrList,
    inline : Boolean = false,
    prefix : Boolean = false
  ) extends Field {
    def render(form: Form) : TagContent = {
      datetime(name, form.values.getInstant(name).map { i ⇒ i.toDateTime }, attrs ++ Seq(title:=description))
    }
  }

  case class ResetField(
    name: String,
    description: String,
    label: String = "Reset",
    attrs: AttrList = EmptyAttrList,
    inline : Boolean = false,
    prefix : Boolean = false
  ) extends FormItem {
    def render(form: Form) : TagContent = {
      reset(name, Some(label), attrs ++ Seq(title:=description))
    }
    override def validate(ref: ValidationLocation, value: BSONValue) : VR = ValidationSucceeded(this, value)
    def defaultValue : BSONValue = BSONNull
  }

  case class SubmitField(
    name: String,
    description: String,
    label: String,
    frmaction: Option[String] = None,
    attrs: AttrList = EmptyAttrList,
    inline : Boolean = false,
    prefix : Boolean = false
  ) extends FormItem {
    def render(form: Form) : TagContent = {
      submit(name, label, attrs ++ Seq(title:=description) ++
        (frmaction match { case Some(x) ⇒ Seq(formaction:=x); case _ ⇒ Seq.empty[AttrPair]} )
      )
    }
    def validate(ref: ValidationLocation, value: BSONValue) : VR = ValidationSucceeded(this, value)
    def defaultValue = BSONNull
  }

  case class FieldSet(
    name: String,
    description: String,
    title: String,
    items: Seq[Field],
    attrs: AttrList = EmptyAttrList,
    inline : Boolean = false,
    prefix : Boolean = false
    ) extends Container {
    require(items.nonEmpty)
    def render(form: Form) : TagContent = {
      fieldset(scalatags.Text.attrs.title:=description, legend(title), items.map { field ⇒ form.renderItem(field) } )
    }
  }

  trait Form extends Container with Enablee with TerminalActionProvider {
    lazy val segment : String = id.name
    def actionPath : String
    def items: Seq[FormItem]
    def values: Settings

    def hasErrors(field: FormItem) : Boolean = false // TODO: Implement Form.hasErrors
    def errorsOf(field: FormItem) : Seq[String] = Seq.empty[String] // TODO: Implement Form.errorsOf

    /**
     * Wrap a field's rendered element with an appropriate label and other formatting. This default is aimed at
     * Twitter bootstrap which is the default formatting for Scrupal.
     *
     * @param form The form that specifies how to wrap the field
     * @param field The field whose rendered output should be wrapped
     * @return The TagContent for the final markup for the field
     */
    def wrap(field: Field) : TagContent = {
      val the_label = scrupal.core.html.Forms.label(field.name, field.name, Seq(cls:="control-label text-info"))
      val the_field = field.render(this)
      div(cls:="clearfix form-group" + (if (hasErrors(field)) " text-danger" else ""),
        if (field.inline) {
          if (field.prefix) {
            Seq(div(the_field,"&nbsp;",the_label))
          } else {
            Seq(div(the_label,": ", the_field))
          }
        } else {
          if (field.prefix) {
            Seq(div(the_field),the_label)
          } else {
            Seq(the_label, div(the_field))
          }
        },
        for(error ← errorsOf(field)) yield {
          div(cls:="text-danger small col-md-10", style:="padding-left:0;padding-top:0;margin-top:0;", error)
        },
        if (field.description.nonEmpty) {
          Seq(div(cls := "help-block text-muted small col-md-10", style := "padding-left:0;padding-top:0;",
            field.description))
        } else Seq.empty[AttrPair]
        // if(!field.url.isEmpty) { a(target:="_blank", href:=field.url, "More Help&hellip;") }
      )
    }

    def renderItem(item: FormItem) : TagContent = {
      item match {
        case f: Field ⇒ wrap(f)
        case f: FormItem ⇒ f.render(this)
      }
    }

    def render(form: Form) : TagContent = {
      scalatags.Text.tags.form(
        scalatags.Text.attrs.action:=actionPath, method:="POST", attrs.name:=name,
        "enctype".attr:="application/x-www-form-urlencoded",
        items.map { field ⇒ renderItem(field) }
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
    override def provideAction(matchingSegment: String, context: Context) : Option[Action] = {
      if (matchingSegment == singularKey && context.request.unmatchedPath.isEmpty) {
        context.request.request.method match {
          case HttpMethods.GET  ⇒ provideRenderFormAction(matchingSegment, context)
          case HttpMethods.POST ⇒ provideAcceptFormAction(matchingSegment, context)
          case _ ⇒ None
        }
      } else {
        None
      }
    }

    def provideRenderFormAction(matchingSegment: String, context: Context) : Option[RenderFormAction] = {
      Some(new RenderFormAction(this, context))
    }

    def provideAcceptFormAction(matchingSegment: String, context: Context) : Option[AcceptFormAction] = {
      Some(new AcceptFormAction(this, context))
    }
  }

  class RenderFormAction(val form: Form, val context: Context) extends Action {
    def apply() : Future[Result[_]] = Future {
      HtmlResult(form.render.toString(), Successful)
    } (context.scrupal._executionContext)
  }

  class AcceptFormAction(val form: Form, val context: Context) extends Action {
/*
    def decodeFormData(r: HttpRequest) : BSONDocument = {
      import spray.httpx.unmarshalling._
      r.as[FormData] match {
        case Right(formData) ⇒
          formData.fields.map { pair ⇒

          }
        case Left(ContentExpected) ⇒
        case Left(MalformedContent(msg,cause)) ⇒
        case Left(UnsupportedContentType(msg) ⇒
        case Left(x) ⇒ x.map BSONDocument()
      }
    } */

    def apply() : Future[Result[_]] = Future {
      /*val doc = decodeFormData(context.request.request)
      form(doc) match {
        case Some(errors) ⇒
        case None
      }*/


      ErrorResult(s"Submission of form '${form.name}", Unimplemented)
    } (context.scrupal._executionContext)
  }

  case class SimpleForm(
    id: Symbol,
    name: String,
    description: String,
    actionPath: String,
    items: Seq[FormItem],
    values: Settings = Settings.Empty
  ) extends Form {
    require(items.length > 0)
  }

  object emptyForm extends Form {
    val id = 'emptyForm; val name = ""; val description = ""; val actionPath = ""
    val items = Seq.empty[FormItem]
    val values = Settings.Empty
  }
}
