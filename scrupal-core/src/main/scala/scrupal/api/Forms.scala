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

package scrupal.api

/** FIXME: Scrupal Forms - Revive as an API "Input" class that is much simpler and not connected to HTML5 forms
  * This trait brings together all the form related objects nd generically represents an HTML5 Form in Scrupal.
  *

trait Form extends Container with Enablee with Provider with Html.TemplateGenerator {
  lazy val segment : String = id.name
  def actionPath : String
  def items : Seq[FormItem]
  def values : Settings

  type ErrorMap = Map[FormItem, Contents]
  def errorMap : ErrorMap = Map.empty[FormItem, Contents]

  def hasErrors(field : FormItem) : Boolean = errorMap.contains(field)
  def errorsOf(field : FormItem) : Contents = errorMap.getOrElse(field, Seq.empty[Modifier])

  def withErrorMap(errorMap : ErrorMap) : Form

  def apply(context : Context, args : ContentsArgs) : Contents = { Seq() }


  override def location = SimpleLocation(s"Form($name)")
  /** Wrap a field's rendered element with an appropriate label and other formatting. This default is aimed at
    * Twitter bootstrap which is the default formatting for Scrupal.
    *
    * @param field The field whose rendered output should be wrapped
    * @return The TagContent for the final markup for the field
    */
  /*def wrap(field : FormField) : TagContent = {
    val the_label = scrupal.api.html.Forms.label(field.name, field.name, Seq(cls := "control-label text-info"))
    val the_field = field.render(this)
    div(cls := "clearfix form-group" + (if (hasErrors(field)) " text-danger" else ""),
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
      for (error ← errorsOf(field)) yield {
        div(cls := "text-danger small col-md-10", style := "padding-left:0;padding-top:0;margin-top:0;", error)
      },
      if (field.showHelp && field.description.nonEmpty) {
        Seq(div(cls := "help-block text-muted small col-md-10", style := "padding-left:0;padding-top:0;",
          field.description))
      } else {
        Seq.empty[AttrPair]
      }
    )
  }
  def apply(context : Context, args : ContentsArgs) : Contents = { Seq(render) }

  def renderItem(item : FormItem) : TagContent = {
    item match {
      case f : FormField ⇒ wrap(f)
      case f : FormItem  ⇒ f.render(this)
    }
  }

  def render : TagContent = {
    scalatags.Text.tags.form(
      scalatags.Text.attrs.action := actionPath, method := "POST", attrs.name := name,
      "enctype".attr := "application/x-www-form-urlencoded",
      items.map { field ⇒ renderItem(field) }
    )
  }

  def render(form : Form) : TagContent = render()
  /** Provide the form action
    *
    * This method from ActionProvider is invoked when a request is made to display or submit the form.
    * It simply checks the conditions and invokes a method to acquire the Action object corresponding
    * to rendering or submitting the form.
    *
    * @param matchingSegment The path segment used to select this ActionProvider
    * @param context The context of the request
    * @return
    */
  override def provideAction(matchingSegment : String, context : Context) : Option[Action] = {
    if (matchingSegment == singularKey && context.request.unmatchedPath.isEmpty) {
      context.request.request.method match {
        case HttpMethods.GET ⇒ provideRenderFormAction(matchingSegment, context)
        case HttpMethods.POST ⇒ provideAcceptFormAction(matchingSegment, context)
        case _ ⇒ None
      }
    } else {
      None
    }
  }

  def provideRenderFormAction(matchingSegment : String, context : Context) : Option[RenderFormAction] = {
    Some(new RenderFormAction(this, context))
  }

  def provideAcceptFormAction(matchingSegment : String, context : Context) : Option[AcceptFormAction] = {
    Some(new AcceptFormAction(this, context))
  }
  */

/** Abstract Form Item.
  *
  * Each item in a form is nameable, describable, a BSONValidator, and can render itself. A FormItem is
  * not necessarily a field as FieldSet is a FormItem but not a field. FormItem includes anything that can
  * go into a form while a field is something that actually produces a value.
  *
  */
trait FormItem extends Nameable with Describable with Validator[JsValue] {
  implicit val scrupal : Scrupal
  def location : Location = TypedLocation(this)
//  TODO: EXtract rendering code out of Form code and make it stand alone
// def render(form : Form) : TagContent
  def defaultValue : JsValue
  final def hasDefaultValue : Boolean = defaultValue != JsNull
}

/** Abstract Form Field.
  *
  * A FormField distinguishes a FormItem by providing the type of data the field contains, a list of display attributes,
  * methods for decoding input values, validation and display options
  */
trait FormField extends FormItem {

  /** Field is inline with label */
  def inline : Boolean

  /** Field comes before label */
  def prefix : Boolean

  /** Field does not require a value */
  def optional : Boolean

  /** Field should have help description shown below it */
  def showHelp : Boolean

  def fieldType : Type[JsValue]

  /*
  def attributes(attrs : AttrList) : AttrList = {
    attrs ++ Seq(title := description) ++ {
      if (!optional) Seq(required := "required") else Seq.empty[AttrPair]
    }
  }
  */
  def decode(value : String) : JsValue
  def validate(value : JsValue) : VResult = validate(location, value)
  def validate(ref : Location, value : JsValue) : VResult = {
    fieldType.validate(ref, value)
  }
  override def location = SimpleLocation(s"FormField($name)")

  require(fieldType.nonTrivial)
}

/** Container of FormItems
  * This is for Forms and Fieldsets that are logical contains of other FormItems
  */
trait Container extends FormItem {
  def items : Seq[FormItem]
  lazy val fieldMap : Map[String, FormItem] = { items.map { field ⇒ field.name → field } }.toMap
  lazy val defaultValue : JsObject = {
    JsObject({
      fieldMap.flatMap {
        case (key, item) ⇒ {
          if (item.isInstanceOf[Container])
            item.defaultValue.asInstanceOf[JsObject].value.toSeq
          else
            Seq(key -> item.defaultValue)
        }
      }.toSeq
    })
  }

  def validate(value : JsValue) : VResult = validate(location, value)

  def validate(ref : Location, value : JsValue) : VResult = {
    value match {
      case x : JsObject ⇒
        val validator = new JsObjectValidator {
          def validateElement(ref : SelectedLocation[String], k: String, v : JsValue) : Results[JsValue] = {
            fieldMap.get(k) match {
              case Some(dator) ⇒ dator.validate(ref, v)
              case None ⇒ StringFailure(ref, v, s"No validator for field '$k'")
            }
          }
        }
        validator.validate(ref, x).asInstanceOf[VResult]
      case x : JsValue ⇒
        wrongClass(this.location, x, "JsObject")
    }
  }
}

/** A Text Field.
  *   *
  * This wraps a name and a description around a Type, a default JsValue for that Type, and options for the
  * type.
  * @param name
  * @param description
  * @param fieldType
  * @param defaultValue
  * @param attrs
  * @param optional
  * @param inline
  * @param prefix
  * @param showHelp
  */
case class TextFormField(
  name : String,
  description : String,
  fieldType : StringType,
  defaultValue : JsValue = JsNull,
  attrs : AttrList = EmptyAttrList,
  optional : Boolean = false,
  inline : Boolean = false,
  prefix : Boolean = false,
  showHelp : Boolean = false) extends FormField {
/*  def render(form : Form) : TagContent = {
    text(name, form.values.getString(name), attributes(attrs))
  }*/
  def decode(value : String) : JsValue = if (value.nonEmpty) JsString(value) else JsNull
}

case class PasswordFormField(
  name : String,
  description : String,
  defaultValue : JsValue = JsNull,
  attrs : AttrList = EmptyAttrList,
  optional : Boolean = false,
  inline : Boolean = false,
  prefix : Boolean = false,
  showHelp : Boolean = false) extends FormField {
  /*def render(form : Form) : TagContent = {
    password(name, form.values.getString(name), attributes(attrs))
  }*/
  val fieldType : Type[_] = scrupal.Types.lookup('Password).get
  def decode(value : String) : JsValue = if (value.nonEmpty) JsString(value) else JsNull
}

case class TextAreaFormField(
  name : String,
  description : String,
  fieldType : StringType,
  defaultValue : JsValue = JsNull,
  attrs : AttrList = EmptyAttrList,
  optional : Boolean = false,
  inline : Boolean = false,
  prefix : Boolean = false,
  showHelp : Boolean = false) extends FormField {
  /*def render(form : Form) : TagContent = {
    scrupal.api.html.Forms.textarea(name, form.values.getString(name), attributes(attrs))
  }*/
  def decode(value : String) : JsValue = if (value.nonEmpty) JsString(value) else JsNull
}

case class BooleanFormField(
  name : String,
  description : String,
  fieldType : BooleanType,
  defaultValue : JsValue = JsNull,
  attrs : AttrList = EmptyAttrList,
  optional : Boolean = false,
  inline : Boolean = false,
  prefix : Boolean = false,
  showHelp : Boolean = false) extends FormField {
  /*def render(form : Form) : TagContent = {
    checkbox(name, form.values.getBoolean(name).getOrElse(false), attributes(attrs))
  }*/
  def decode(value : String) : JsValue = JsBoolean(value.nonEmpty)
}

case class IntegerFormField(
  name : String,
  description : String,
  fieldType : RangeType,
  defaultValue : JsValue = JsNull,
  attrs : AttrList = EmptyAttrList,
  minVal : Long = Long.MinValue,
  maxVal : Long = Long.MaxValue,
  optional : Boolean = false,
  inline : Boolean = false,
  prefix : Boolean = false,
  showHelp : Boolean = false) extends FormField {
  /*def render(form : Form) : TagContent = {
    number(name, form.values.getDouble(name), minVal.toDouble, maxVal.toDouble, attributes(attrs))
  }*/
  def decode(value : String) : JsValue = if (value.nonEmpty) JsNumber(value.toLong) else JsNull
}

case class RealFormField(
  name : String,
  description : String,
  fieldType : RealType,
  defaultValue : JsValue = JsNull,
  attrs : AttrList = EmptyAttrList,
  minVal : Double = Double.MinValue,
  maxVal : Double = Double.MaxValue,
  optional : Boolean = false,
  inline : Boolean = false,
  prefix : Boolean = false,
  showHelp : Boolean = false) extends FormField {
  /*def render(form : Form) : TagContent = {
    number(name, form.values.getDouble(name), minVal, maxVal, attributes(attrs))
  }*/
  def decode(value : String) : JsValue = if (value.nonEmpty) JsNumber(value.toDouble) else JsNull
}

case class RangeFormField(
  name : String,
  description : String,
  fieldType : RealType,
  defaultValue : JsValue = JsNull,
  attrs : AttrList = EmptyAttrList,
  minVal : Double = 0L,
  maxVal : Double = 100L,
  optional : Boolean = false,
  inline : Boolean = false,
  prefix : Boolean = false,
  showHelp : Boolean = false) extends FormField {
  /*def render(form : Form) : TagContent = {
    range(name, form.values.getDouble(name), minVal, maxVal, attributes(attrs))
  }*/
  def decode(value : String) : JsValue = if (value.nonEmpty) JsNumber(value.toDouble) else JsNull
}

case class SelectionFormField(
  name : String,
  description : String,
  fieldType : SelectionType,
  defaultValue : JsValue = JsNull,
  attrs : AttrList = EmptyAttrList,
  optional : Boolean = false,
  inline : Boolean = false,
  prefix : Boolean = false,
  showHelp : Boolean = false) extends FormField {
  /*def render(form : Form) : TagContent = {
    val options = fieldType.choices.map { choice ⇒ choice → choice }
    scrupal.api.html.Forms.select(name, form.values.getString(name), options.toMap, attributes(attrs))
  }*/
  def decode(value : String) : JsValue = if (value.nonEmpty) JsString(value) else JsNull
}

case class TimestampFormField(
  name : String,
  description : String,
  fieldType : TimestampType,
  defaultValue : JsValue = JsNull,
  attrs : AttrList = EmptyAttrList,
  optional : Boolean = false,
  inline : Boolean = false,
  prefix : Boolean = false,
  showHelp : Boolean = false) extends FormField {
  /*def render(form : Form) : TagContent = {
    datetime(name, form.values.getInstant(name).map { i ⇒ i.toDateTime }, attributes(attrs))
  }*/
  def decode(value : String) : JsValue = if (value.nonEmpty) JsNumber(value.toLong) else JsNull
}

case class ResetFormField(
  name : String,
  description : String,
  label : String = "Reset",
  attrs : AttrList = EmptyAttrList,
  inline : Boolean = false,
  prefix : Boolean = false) extends {
  final val fieldType : BooleanType = Boolean_t
  final val optional : Boolean = false
  final val showHelp : Boolean = false
} with FormField {
  /*override def attributes(attrs : AttrList) : AttrList = {
    attrs ++ Seq(title := description)
  }*/
  def defaultValue : JsValue = JsBoolean(value = false)
  /*def render(form : Form) : TagContent = {
    reset(name, Some(label), attributes(attrs))
  }*/
  def decode(value : String) : JsValue = JsBoolean(value.nonEmpty)
  override def validate(ref : Location, value : JsValue) : VResult = Success(this.location, value)
}

case class SubmitFormField(
  name : String,
  description : String,
  label : String,
  frmaction : Option[String] = None,
  attrs : AttrList = EmptyAttrList,
  inline : Boolean = false,
  prefix : Boolean = false) extends {
  final val fieldType : BooleanType = Boolean_t
  final val optional : Boolean = false
  final val showHelp : Boolean = false
} with FormField {
  /*override def attributes(attrs : AttrList) : AttrList = {
    attrs ++ Seq(title := description) ++
      (frmaction match { case Some(x) ⇒ Seq(formaction := x); case _ ⇒ Seq.empty[AttrPair] })
  }*/
  def defaultValue = JsBoolean(value = false)
  /*def render(form : Form) : TagContent = {
    submit(name, label, attributes(attrs))
  }*/
  def decode(value : String) : JsValue = JsBoolean(value.nonEmpty)
  override def validate(ref : Location, value : JsValue) : VResult = Success(this.location, value)
}

case class FieldSet(
  name : String,
  description : String,
  title : String,
  items : Seq[FormField],
  attrs : AttrList = EmptyAttrList,
  inline : Boolean = false,
  prefix : Boolean = false) extends Container {
  require(items.nonEmpty)
  /*def render(form : Form) : TagContent = {
    fieldset(scalatags.Text.attrs.title := description, legend(title), items.map { field ⇒ form.renderItem(field) })
  }*/
  override def location = SimpleLocation(s"FieldSet($name)")
}


}
/*

class RenderFormAction(val form : Form, val context : Context) extends Action {
def apply() : Future[Result[_]] = Future {
  HtmlResult(form.render.toString(), Successful)
} (context.scrupal._executionContext)
}

class AcceptFormAction(val form : Form, val context : Context) extends Action {
def decodeFormData(r : HttpRequest) : Failure[JsValue] = {
  import akka.http.scaladsl.unmarshalling._
  type FF = scrupal.api.FormField // spray unmarshalling also defines FormField
  val formItems : Map[String, FF] = {
    for (item ← form.items if item.isInstanceOf[FF]) yield { item.name → item.asInstanceOf[FF] }
  }.toMap
  val formData = FormData(r.uri.query)
  val data : LinearSeq[Results[JsObject]] = {
    for ((name, value) ← formData.fields if value.nonEmpty) yield {
      formItems.get(name) match {
        case Some(item) ⇒
          try {
            item.decode(value) match {
              case JsNull ⇒ Success(form.location, emptyJsObject)
              case v: JsValue ⇒ Success(form.location, JsObject(Map(name → v)))
            }
          } catch {
            case x: Throwable ⇒ ThrowableFailure(form.location, JsObject(Map(name → JsString(value))), x)
          }
        case None ⇒
          StringFailure(form.location, JsObject(Map(name → JsString(value))), "Spurious field '" + name + "'")
      }
    }
  }
    val elems : Map[String, JsValue] = data.foldLeft(Seq.empty[(String, JsValue)]) {
        case (last, results) ⇒ last ++ results.value.value
      }.toMap
      val errors : Seq[Failure[JsValue]] = {
        { data.filter { result ⇒ result.isError }.map { r ⇒ r.asInstanceOf[Failure[JsValue]] } } ++ {
          for (
            (name, item) ← formItems if item.optional;
            value = elems.get(name) if value.isEmpty && !item.hasDefaultValue
          ) yield {
            StringFailure(form.location, JsObject(Seq(name → JsNull)), s"Required field '$name' has no value.")
          }
        }
      }
      val doc = JsObject(elems)
      val vr = form.validate(doc)
      if (errors.isEmpty)
        vr
      else
        Failures(vr.ref, vr.value, errors:_*)

    case Left(RequestEntityExpectedRejection) ⇒
      StringFailure(form.location, emptyJsObject, "Content Expected")
    case Left(MalformedRequestContentRejection(msg, cause)) ⇒ cause match {
      case Some(throwable) ⇒ ThrowableFailure(form.location, emptyJsObject, throwable)
      case None ⇒ StringFailure(form.location, emptyJsObject, msg)
    }
    case Left(UnacceptedResponseContentTypeRejection(msg)) ⇒
      StringFailure(form.location, emptyJsObject, "Unsupported content type: " + msg)
    case Left(x) ⇒
      StringFailure(form.location, emptyJsObject, "Unspecified error")
  }
}

def formWithErrors(validationResults : Failure[JsValue]) : Form = {
  val errorMap = validationResults.errorMap map {
    case (ref, msg) ⇒
      ref.asInstanceOf[FormItem] -> msg
  }
  form.withErrorMap(errorMap)
}

def handleValidatedFormData(doc : JsObject) : Result[_] = {
  StringResult(s"Submission of '${form.name}' succeeded.", Successful)
}

def handleValidationFailure(failure : ValidationFailed[JsValue]) : Result[_] = {
  val msg : StringBuilder = new StringBuilder()
  for (e ← failure.errors) msg.append(e.msgBldr).append("\n")
  FormErrorResult(failure, Unacceptable)
}

def apply() : Future[Result[_]] = Future {
  decodeFormData(context.request.request) match {
    case Success(ref, doc) ⇒
      handleValidatedFormData(doc.asInstanceOf[JsObject])
    case err : ValidationFailed[JsValue] ⇒
      handleValidationFailure(err)
    case vr : Failure[JsValue] ⇒
      handleValidationFailure(ValidationFailed(form, JsNull, Seq(vr)))
  }
} (context.scrupal._executionContext)
}
*/

case class SimpleForm(
  id : Symbol,
  name : String,
  description : String,
  actionPath : String,
  items : Seq[FormItem],
  values : Settings = Settings.Empty,
  override val errorMap : Map[FormItem, Contents] = Map.empty[FormItem, Contents]) extends Form {
  require(items.nonEmpty)

  def withErrorMap(em : ErrorMap) : Form = copy(errorMap = em)
}

object emptyForm extends Form {
  val id = 'emptyForm; val name = ""; val description = ""; val actionPath = ""
  val items = Seq.empty[FormItem]
  val values = Settings.Empty
  def withErrorMap(em : ErrorMap) : Form = this
}

*/
