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
import spray.http.{FormData, HttpRequest, HttpMethods}
import scala.concurrent.Future
import scalatags.Text.all._
import scalatags.Text.attrs



/** Abstract Form Item.
  *
  * Each item in a form is nameable, describable, a BSONValidator, and can render itself. A FormItem is
  * not necessarily a field as FieldSet is a FormItem but not a field. FormItem includes anything that can
  * go into a form while a field is something that actually produces a value.
  *
  */
trait FormItem extends Nameable with Describable with BSONValidator with ValidationLocation {
  def render(form: Form) : TagContent
  def defaultValue : BSONValue
  final def hasDefaultValue : Boolean = defaultValue != BSONNull
}

/** Abstract Form Field.
  *
  * A FormField distinguishes a FormItem by providing the type of data the field contains, a list of display attributes,
  * methods for decoding input values, validation and display options
  */
trait FormField extends FormItem  {

  /** Field is inline with label */
  def inline : Boolean

  /** Field comes before label */
  def prefix : Boolean

  /** Field does not require a value */
  def optional : Boolean

  /** Field should have help description shown below it */
  def showHelp: Boolean

  def fieldType : Type
  def attributes(attrs: AttrList) : AttrList = {
    attrs ++ Seq(title:=description) ++ {
      if (!optional) Seq(required:="required") else Seq.empty[AttrPair]}
  }
  def decode(value: String) : BSONValue
  def validate(value: BSONValue) : VR = validate(this, value)
  def validate(ref: ValidationLocation, value: BSONValue) : VR = {
    fieldType.validate(this, value)
  }
  def location = s"form field '${name}'"
  require(fieldType.nonTrivial)
}

/** Container of FormItems
  * This is for Forms and Fieldsets that are logical contains of other FormItems
  */
trait Container extends FormItem {
  def items: Seq[FormItem]
  lazy val fieldMap : Map[String,FormItem] = { items.map { field ⇒ field.name → field } }.toMap
  lazy val defaultValue : BSONDocument = {
    BSONDocument( {
      fieldMap.map {
        case (key,item) ⇒ {
          if (item.isInstanceOf[ Container ])
            item.defaultValue.asInstanceOf[ BSONDocument ].elements
          else
            Seq(key -> item.defaultValue)
        }
      }.flatten
    })
  }

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
      case x: BSONDocument ⇒ validateMaps(this, x, fieldMap, defaultValue)
      case x: BSONValue    ⇒ wrongClass(this, x, "BSONDocument")
    }
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
case class TextFormField(
  name: String,
  description: String,
  fieldType: StringType,
  defaultValue: BSONValue = BSONNull,
  attrs: AttrList = EmptyAttrList,
  optional : Boolean = false,
  inline : Boolean = false,
  prefix : Boolean = false,
  showHelp : Boolean = false
) extends FormField {
  def render(form: Form) : TagContent = {
    text(name, form.values.getString(name), attributes(attrs))
  }
  def decode(value: String) : BSONValue = if (value.nonEmpty) BSONString(value) else BSONNull
}

case class PasswordFormField(
  name: String,
  description: String,
  fieldType: StringType = Password_t,
  defaultValue: BSONValue = BSONNull,
  attrs: AttrList = EmptyAttrList,
  optional : Boolean = false,
  inline : Boolean = false,
  prefix : Boolean = false,
  showHelp : Boolean = false
) extends FormField {
  def render(form: Form) : TagContent = {
    password(name, form.values.getString(name), attributes(attrs))
  }
  def decode(value: String) : BSONValue = if (value.nonEmpty) BSONString(value) else BSONNull
}

case class TextAreaFormField(
  name: String,
  description: String,
  fieldType: StringType,
  defaultValue: BSONValue = BSONNull,
  attrs: AttrList = EmptyAttrList,
  optional : Boolean = false,
  inline : Boolean = false,
  prefix : Boolean = false,
  showHelp : Boolean = false
  ) extends FormField {
  def render(form: Form) : TagContent = {
    scrupal.core.html.Forms.textarea(name, form.values.getString(name), attributes(attrs))
  }
  def decode(value: String) : BSONValue = if (value.nonEmpty) BSONString(value) else BSONNull
}

case class BooleanFormField(
  name: String,
  description: String,
  fieldType: BooleanType,
  defaultValue: BSONValue = BSONNull,
  attrs: AttrList = EmptyAttrList,
  optional : Boolean = false,
  inline : Boolean = false,
  prefix : Boolean = false,
  showHelp : Boolean = false
) extends FormField {
  def render(form: Form) : TagContent = {
    checkbox(name, form.values.getBoolean(name).getOrElse(false), attributes(attrs))
  }
  def decode(value: String) : BSONValue = BSONBoolean(value.nonEmpty)
}

case class IntegerFormField(
  name: String,
  description: String,
  fieldType : RangeType,
  defaultValue: BSONValue = BSONNull,
  attrs: AttrList = EmptyAttrList,
  minVal: Long = Long.MinValue,
  maxVal: Long = Long.MaxValue,
  optional : Boolean = false,
  inline : Boolean = false,
  prefix : Boolean = false,
  showHelp : Boolean = false
) extends FormField {
  def render(form: Form) : TagContent = {
    number(name, form.values.getDouble(name), minVal.toDouble, maxVal.toDouble, attributes(attrs))
  }
  def decode(value: String) : BSONValue = if (value.nonEmpty) BSONLong(value.toLong) else BSONNull
}

case class RealFormField(
  name: String,
  description: String,
  fieldType : RealType,
  defaultValue: BSONValue = BSONNull,
  attrs: AttrList = EmptyAttrList,
  minVal: Double = Double.MinValue,
  maxVal: Double = Double.MaxValue,
  optional : Boolean = false,
  inline : Boolean = false,
  prefix : Boolean = false,
  showHelp : Boolean = false
) extends FormField {
  def render(form: Form) : TagContent = {
    number(name, form.values.getDouble(name), minVal, maxVal, attributes(attrs))
  }
  def decode(value: String) : BSONValue = if (value.nonEmpty) BSONDouble(value.toDouble) else BSONNull
}

case class RangeFormField(
  name: String,
  description: String,
  fieldType : RealType,
  defaultValue: BSONValue = BSONNull,
  attrs: AttrList = EmptyAttrList,
  minVal: Double = 0L,
  maxVal: Double = 100L,
  optional : Boolean = false,
  inline : Boolean = false,
  prefix : Boolean = false,
  showHelp : Boolean = false
) extends FormField {
  def render(form: Form) : TagContent = {
    range(name, form.values.getDouble(name), minVal, maxVal, attributes(attrs))
  }
  def decode(value: String) : BSONValue = if (value.nonEmpty) BSONDouble(value.toDouble) else BSONNull
}

case class SelectionFormField(
  name: String,
  description: String,
  fieldType : SelectionType,
  defaultValue: BSONValue = BSONNull,
  attrs: AttrList = EmptyAttrList,
  optional : Boolean = false,
  inline : Boolean = false,
  prefix : Boolean = false,
  showHelp : Boolean = false
) extends FormField {
  def render(form: Form) : TagContent = {
    val options = fieldType.choices.map { choice ⇒ choice → choice}
    scrupal.core.html.Forms.select(name, form.values.getString(name), options.toMap, attributes(attrs))
  }
  def decode(value: String) : BSONValue = if (value.nonEmpty) BSONString(value) else BSONNull
}

case class TimestampFormField(
  name: String,
  description: String,
  fieldType : TimestampType,
  defaultValue: BSONValue = BSONNull,
  attrs: AttrList = EmptyAttrList,
  optional : Boolean = false,
  inline : Boolean = false,
  prefix : Boolean = false,
  showHelp : Boolean = false
) extends FormField {
  def render(form: Form) : TagContent = {
    datetime(name, form.values.getInstant(name).map { i ⇒ i.toDateTime }, attributes(attrs))
  }
  def decode(value: String) : BSONValue = if (value.nonEmpty) BSONDateTime(value.toLong) else BSONNull
}

case class ResetFormField(
  name: String,
  description: String,
  label: String = "Reset",
  attrs: AttrList = EmptyAttrList,
  inline : Boolean = false,
  prefix : Boolean = false
) extends {
  final val fieldType : BooleanType = Boolean_t
  final val optional : Boolean = false
  final val showHelp : Boolean = false
} with FormField {
  override def attributes(attrs: AttrList) : AttrList = {
    attrs ++ Seq(title := description)
  }
  def defaultValue : BSONValue = BSONBoolean(value=false)
  def render(form: Form) : TagContent = {
    reset(name, Some(label), attributes(attrs))
  }
  def decode(value: String) : BSONValue = BSONBoolean(value.nonEmpty)
  override def validate(ref: ValidationLocation, value: BSONValue) : VR = ValidationSucceeded(this, value)
}

case class SubmitFormField(
  name: String,
  description: String,
  label: String,
  frmaction: Option[String] = None,
  attrs: AttrList = EmptyAttrList,
  inline : Boolean = false,
  prefix : Boolean = false
) extends {
  final val fieldType : BooleanType = Boolean_t
  final val optional : Boolean = false
  final val showHelp : Boolean = false
} with FormField {
  override def attributes(attrs: AttrList) : AttrList = {
    attrs ++ Seq(title := description) ++
      (frmaction match { case Some(x) ⇒ Seq(formaction:=x); case _ ⇒ Seq.empty[AttrPair]})
  }
  def defaultValue = BSONBoolean(value=false)
  def render(form: Form) : TagContent = {
    submit(name, label, attributes(attrs) )
  }
  def decode(value: String) : BSONValue = BSONBoolean(value.nonEmpty)
  override def validate(ref: ValidationLocation, value: BSONValue) : VR = ValidationSucceeded(this, value)
}

case class FieldSet(
  name: String,
  description: String,
  title: String,
  items: Seq[FormField],
  attrs: AttrList = EmptyAttrList,
  inline : Boolean = false,
  prefix : Boolean = false
  ) extends Container {
  require(items.nonEmpty)
  def render(form: Form) : TagContent = {
    fieldset(scalatags.Text.attrs.title:=description, legend(title), items.map { field ⇒ form.renderItem(field) } )
  }
  def location = s"field set '$name'"
}

/** Scrupal Forms
  * This trait brings together all the form related objects nd generically represents an HTML5 Form in Scrupal.
  *
  */
trait Form extends Container with Enablee with TerminalActionProvider with Html.TemplateGenerator {
  lazy val segment : String = id.name
  def actionPath : String
  def items: Seq[FormItem]
  def values: Settings

  type ErrorMap = Map[FormItem,Seq[String]]
  def errorMap: ErrorMap  = Map.empty[FormItem,Seq[String]]

  def hasErrors(field: FormItem) : Boolean = errorMap.contains(field)
  def errorsOf(field: FormItem) : Seq[String] = errorMap.getOrElse(field, Seq.empty[String])

  def withErrorMap(errorMap: ErrorMap) : Form


  def location = s"form '$name'"
  /**
   * Wrap a field's rendered element with an appropriate label and other formatting. This default is aimed at
   * Twitter bootstrap which is the default formatting for Scrupal.
   *
   * @param form The form that specifies how to wrap the field
   * @param field The field whose rendered output should be wrapped
   * @return The TagContent for the final markup for the field
   */
  def wrap(field: FormField) : TagContent = {
    val the_label = scrupal.core.html.Forms.label(field.name, field.name, Seq(cls:="control-label text-info"))
    val the_field = field.render(this)
    div(cls:="clearfix form-group" + (if (hasErrors(field)) " text-danger" else ""),
      if (field.name.isEmpty) {
        Seq(div(the_field))
      } else if (field.inline) {
        if (field.prefix) {
          Seq(div(the_field, nbsp, the_label))
        } else {
          Seq(div(the_label, nbsp, the_field))
        }
      } else if (field.prefix) {
        Seq(div(the_field), the_label)
      } else {
        Seq(the_label, div(the_field))
      },
      for(error ← errorsOf(field)) yield {
        div(cls:="text-danger small col-md-10", style:="padding-left:0;padding-top:0;margin-top:0;", error)
      },
      if (field.showHelp && field.description.nonEmpty) {
        Seq(div(cls := "help-block text-muted small col-md-10", style := "padding-left:0;padding-top:0;",
          field.description))
      } else {
        Seq.empty[ AttrPair ]
      }
    )
  }

  def apply(context: Context, args: ContentsArgs) : Contents = { Seq(render) }

  def renderItem(item: FormItem) : TagContent = {
    item match {
      case f: FormField ⇒ wrap(f)
      case f: FormItem ⇒ f.render(this)
    }
  }

  def render : TagContent = {
    scalatags.Text.tags.form(
      scalatags.Text.attrs.action:=actionPath, method:="POST", attrs.name:=name,
      "enctype".attr:="application/x-www-form-urlencoded",
      items.map { field ⇒ renderItem(field) }
    )
  }

  def render(form: Form) : TagContent = render()

  /** Provide the form action
    *
    * This method from ActionProvider is invoked when a request is made to display or submit the form.
    * It simply checks the conditions and invokes a method to acquire the Action object corresponding
    * to rendering or submitting the form.
    *
    * @param key The path segment used to select this ActionProvider
    * @param context The context of the request
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
  def decodeFormData(r: HttpRequest) : ValidationResults[BSONValue] = {
    import spray.httpx.unmarshalling._
    type FF = scrupal.core.api.FormField // spray unmarshalling also defines FormField
    val formItems : Map[String,FF] = {
      for (item <- form.items if item.isInstanceOf[FF]) yield { item.name → item.asInstanceOf[FF] }
    }.toMap
    r.as[FormData] match {
      case Right(formData) ⇒
        val data : Seq[ValidationResults[BSONDocument]] =
          for ((name, value) ← formData.fields if value.nonEmpty) yield {
            formItems.get(name) match {
              case Some(item) ⇒
                try {
                  item.decode(value) match {
                    case BSONNull ⇒ ValidationSucceeded(form, BSONDocument())
                    case v: BSONValue ⇒ ValidationSucceeded(form, BSONDocument(name → v))
                  }
                } catch {
                  case x: Throwable ⇒ ValidationException(form, BSONDocument(name → BSONString(value)), x)
                }
              case None ⇒
                ValidationError(form, BSONDocument(name → BSONString(value)), "Spurious field '" + name + "'")
            }
          }
        val elems : Map[String,BSONValue] = data.foldLeft(Seq.empty[(String,BSONValue)]) {
          case (last, results) ⇒ last ++ results.value.elements
        }.toMap
        val errors : Seq[ValidationErrorResults[BSONValue]] = {
          {data.filter { result ⇒ result.isError}.map { r ⇒ r.asInstanceOf[ValidationErrorResults[BSONValue]]}} ++ {
            for (
              (name, item) ← formItems if item.optional;
              value = elems.get(name) if value.isEmpty && !item.hasDefaultValue
            ) yield {
              ValidationError(form, BSONDocument(name → BSONNull).asInstanceOf[BSONValue],
                s"Required field '$name' has no value.")
            }
          }
        }
        val doc = BSONDocument(elems)
        val vr = form.validate(doc)
        if (errors.isEmpty)
          vr
        else
          ValidationFailed(vr.ref, vr.value, errors)

      case Left(ContentExpected) ⇒
        ValidationError(form, BSONDocument(), "Content Expected")
      case Left(MalformedContent(msg,cause)) ⇒ cause match {
        case Some(throwable) ⇒ ValidationException(form, BSONDocument(), throwable)
        case None ⇒ ValidationError(form, BSONDocument(), msg)
      }
      case Left(UnsupportedContentType(msg)) ⇒
        ValidationError(form, BSONDocument(), "Unsupported content type: " + msg)
      case Left(x) ⇒
        ValidationError(form, BSONDocument(), "Unspecified error")
    }
  }

  def formWithErrors(validationResults: ValidationResults[BSONValue]) : Form = {
    val errorMap = validationResults.errorMap map { case (ref,msg) ⇒
        ref.asInstanceOf[FormItem] -> msg
    }
    form.withErrorMap(errorMap)
  }

  def handleValidatedFormData(doc: BSONDocument) : Result[_] = {
    StringResult(s"Submission of '${form.name}' succeeded.", Successful)
  }

  def handleValidationFailure(failure: ValidationFailed[BSONValue]) : Result[_] = {
    val msg : StringBuilder = new StringBuilder()
    for (e ← failure.errors) msg.append(e.message).append("\n")
    FormErrorResult(failure, Unacceptable)
  }

  def apply() : Future[Result[_]] = Future {
    decodeFormData(context.request.request) match {
      case ValidationSucceeded(ref, doc) ⇒
        handleValidatedFormData(doc.asInstanceOf[BSONDocument])
      case err: ValidationFailed[BSONValue] ⇒
        handleValidationFailure(err)
      case vr: ValidationErrorResults[BSONValue] ⇒
        handleValidationFailure(ValidationFailed(form, BSONNull, Seq(vr)))
    }
  } (context.scrupal._executionContext)
}

case class SimpleForm(
  id: Symbol,
  name: String,
  description: String,
  actionPath: String,
  items: Seq[FormItem],
  values: Settings = Settings.Empty,
  override val errorMap: Map[FormItem,Seq[String]] = Map.empty[FormItem,Seq[String]]
) extends Form {
  require(items.length > 0)

  def withErrorMap(em: ErrorMap) : Form = copy(errorMap = em)
}

object emptyForm extends Form {
  val id = 'emptyForm; val name = ""; val description = ""; val actionPath = ""
  val items = Seq.empty[FormItem]
  val values = Settings.Empty
  def withErrorMap(em: ErrorMap) : Form = this
}

