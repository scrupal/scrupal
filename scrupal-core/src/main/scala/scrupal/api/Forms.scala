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

import java.time.Instant

import org.joda.time.DateTime
import play.api.routing.sird._
import scrupal.api.Html._
import scrupal.api.html.Forms._
import scrupal.utils.Enablee
import scrupal.utils.Validation._

import scala.concurrent.{ExecutionContext, Future}
import scalatags.Text.all._
import scalatags.Text.attrs

object Form {

  /** Abstract Form Item.
    *
    * Each item in a form is nameable, describable, a BSONValidator, and can render itself. A FormItem is
    * not necessarily a field as FieldSet is a FormItem but not a field. FormItem includes anything that can
    * go into a form while a field is something that actually produces a value.
    *
    */
  trait Item extends Nameable with Describable {
    def location: Location = TypedLocation(this)

    //  TODO: Extract rendering code out of Form code and make it stand alone
    def render(form: Form): TagContent

  }

  /** Abstract Form Field.
    *
    * A FormField distinguishes a FormItem by providing the type of data the field contains, a list of display
    * attributes,
    *
    * methods for decoding input values, validation and display options
    */
  trait Field extends Item with Validator[Atom] {

    /** Field is inline with label */
    def inline: Boolean

    /** Field comes before label */
    def prefix: Boolean

    /** Field does not require a value */
    def optional: Boolean

    /** Field should have help description shown below it */
    def showHelp: Boolean

    def defaultValue: Atom

    final def hasDefaultValue: Boolean = defaultValue != null

    def fieldType: Type[Atom]

    def attributes(attrs: AttrList): AttrList = {
      attrs ++ Seq(title := description) ++ {
        if (!optional) Seq(required := "required") else Seq.empty[AttrPair]
      }
    }

    def decode(value: String): Atom

    def validate(value: Atom): VResult = validate(location, value)

    def validate(ref: Location, value: Atom): VResult = {
      fieldType.validate(ref, value)
    }

    override def location = SimpleLocation(s"FormField($name)")

    require(fieldType.nonTrivial)
  }

  /** Container of FormItems
    * This is for Forms and Fieldsets that are logical contains of other FormItems
    */
  trait Container extends Item with StringMapValidator[Atom] {
    def items: Seq[Field]

    lazy val fieldMap: Map[String, Field] = {items.map { field ⇒ field.name → field }}.toMap
    lazy val defaultValue: Atom = 0L
    lazy val defaultContents: Map[String, Atom] = {
      fieldMap.map {
        case (key, item) ⇒
          key → item.defaultValue
      }
    }

    /** Validate value of type VType with this validator
      *
      * @param ref The location at which the value occurs
      * @param value the VType to be validated
      * @return Any of the ValidationResults
      */
    def validate(ref: Location, value: Atom): VResult = ??? // FIXME: Need single value validator ?

    def validate(value: Map[String, Atom]): VResult = validate(location, value)

    def validateElement(ref: SelectedLocation[String], k: String, v: Atom): Results[Atom] = {
      fieldMap.get(k) match {
        case Some(field: Field) ⇒
          field.validate(ref, v)
        case None ⇒
          StringFailure(ref, v, s"No validator for field '$k'")
      }
    }
  }

  /** A Text Field.
    * *
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
  case class TextField(
    name: String,
    description: String,
    fieldType: StringType,
    defaultValue: Atom = "",
    attrs: AttrList = EmptyAttrList,
    optional: Boolean = false,
    inline: Boolean = false,
    prefix: Boolean = false,
    showHelp: Boolean = false) extends Field {
    def render(form: Form): TagContent = {
      text(name, form.values.getString(name), attributes(attrs))
    }

    def decode(value: String): Atom = value
  }

  case class PasswordField(
    name: String,
    description: String,
    fieldType: StringType = Password_t,
    defaultValue: Atom = "",
    attrs: AttrList = EmptyAttrList,
    optional: Boolean = false,
    inline: Boolean = false,
    prefix: Boolean = false,
    showHelp: Boolean = false) extends Field {
    def render(form: Form): TagContent = {
      password(name, form.values.getString(name), attributes(attrs))
    }

    def decode(value: String): Atom = value
  }

  case class TextAreaField(
    name: String,
    description: String,
    fieldType: StringType,
    defaultValue: Atom = "",
    attrs: AttrList = EmptyAttrList,
    optional: Boolean = false,
    inline: Boolean = false,
    prefix: Boolean = false,
    showHelp: Boolean = false) extends Field {
    def render(form: Form): TagContent = {
      _root_.scrupal.api.html.Forms.textarea(name, form.values.getString(name), attributes(attrs))
    }

    def decode(value: String): Atom = value
  }

  case class BooleanField(
    name: String,
    description: String,
    fieldType: BooleanType,
    defaultValue: Atom = false,
    attrs: AttrList = EmptyAttrList,
    optional: Boolean = false,
    inline: Boolean = false,
    prefix: Boolean = false,
    showHelp: Boolean = false) extends Field {
    def render(form: Form): TagContent = {
      checkbox(name, form.values.getBoolean(name).getOrElse(false), attributes(attrs))
    }

    def decode(value: String): Atom = value.toBoolean
  }

  case class IntegerField(
    name: String,
    description: String,
    fieldType: RangeType,
    defaultValue: Atom = 0L,
    attrs: AttrList = EmptyAttrList,
    minVal: Long = Long.MinValue,
    maxVal: Long = Long.MaxValue,
    optional: Boolean = false,
    inline: Boolean = false,
    prefix: Boolean = false,
    showHelp: Boolean = false) extends Field {

    def render(form: Form): TagContent = {
      number(name, form.values.getDouble(name), minVal.toDouble, maxVal.toDouble, attributes(attrs))
    }

    def decode(value: String): Atom = value.toLong
  }

  case class RealField(
    name: String,
    description: String,
    fieldType: RealType,
    defaultValue: Atom = 0.0D,
    attrs: AttrList = EmptyAttrList,
    minVal: Double = Double.MinValue,
    maxVal: Double = Double.MaxValue,
    optional: Boolean = false,
    inline: Boolean = false,
    prefix: Boolean = false,
    showHelp: Boolean = false) extends Field {

    def render(form: Form): TagContent = {
      number(name, form.values.getDouble(name), minVal, maxVal, attributes(attrs))
    }

    def decode(value: String): Atom = value.toDouble
  }

  case class RangeField(
    name: String,
    description: String,
    fieldType: RealType,
    defaultValue: Atom = 0.0D,
    attrs: AttrList = EmptyAttrList,
    minVal: Double = 0L,
    maxVal: Double = 100L,
    optional: Boolean = false,
    inline: Boolean = false,
    prefix: Boolean = false,
    showHelp: Boolean = false) extends Field {

    def render(form: Form): TagContent = {
      range(name, form.values.getDouble(name), minVal, maxVal, attributes(attrs))
    }

    def decode(value: String): Atom = value.toDouble
  }

  case class SelectionField(
    name: String,
    description: String,
    fieldType: SelectionType,
    defaultValue: Atom = "",
    attrs: AttrList = EmptyAttrList,
    optional: Boolean = false,
    inline: Boolean = false,
    prefix: Boolean = false,
    showHelp: Boolean = false) extends Field {
    def render(form: Form): TagContent = {
      val options = fieldType.choices.map { choice ⇒ choice → choice }
      _root_.scrupal.api.html.Forms.select(name, form.values.getString(name), options.toMap, attributes(attrs))
    }

    def decode(value: String): Atom = value
  }

  case class TimestampField(
    name: String,
    description: String,
    fieldType: TimestampType,
    defaultValue: Atom = Instant.ofEpochMilli(0L),
    attrs: AttrList = EmptyAttrList,
    optional: Boolean = false,
    inline: Boolean = false,
    prefix: Boolean = false,
    showHelp: Boolean = false) extends Field {
    def render(form: Form): TagContent = {
      datetime(name, form.values.getInstant(name).map { i ⇒ new DateTime(i) }, attributes(attrs))
    }

    def decode(value: String): Atom = Instant.ofEpochMilli(value.toLong)
  }

  case class ResetField(
    name: String,
    description: String,
    label: String = "Reset",
    attrs: AttrList = EmptyAttrList,
    inline: Boolean = false,
    prefix: Boolean = false) extends {
    final val fieldType: BooleanType = Boolean_t
    final val optional: Boolean = false
    final val showHelp: Boolean = false
  } with Field {
    override def attributes(attrs: AttrList): AttrList = {
      attrs ++ Seq(title := description)
    }

    def defaultValue: Atom = false

    def render(form: Form): TagContent = {
      reset(name, Some(label), attributes(attrs))
    }

    def decode(value: String): Atom = value.nonEmpty

    override def validate(ref: Location, value: Atom): VResult = Success(this.location, value)
  }

  case class SubmitField(
    name: String,
    description: String,
    label: String,
    frmaction: Option[String] = None,
    attrs: AttrList = EmptyAttrList,
    inline: Boolean = false,
    prefix: Boolean = false) extends {
    final val fieldType: BooleanType = Boolean_t
    final val optional: Boolean = false
    final val showHelp: Boolean = false
  } with Field {
    override def attributes(attrs: AttrList): AttrList = {
      attrs ++ Seq(title := description) ++
        (frmaction match {case Some(x) ⇒ Seq(formaction := x); case _ ⇒ Seq.empty[AttrPair]})
    }

    def defaultValue = false

    def render(form: Form): TagContent = {
      submit(name, label, attributes(attrs))
    }

    def decode(value: String): Atom = value.nonEmpty

    override def validate(ref: Location, value: Atom): VResult = Success(this.location, value)
  }

  case class FieldSet(
    name: String,
    description: String,
    title: String,
    items: Seq[Field],
    attrs: AttrList = EmptyAttrList,
    inline: Boolean = false,
    prefix: Boolean = false) extends Container {
    require(items.nonEmpty)

    def toMap(mt: Map[String, Atom]): collection.Map[String, Atom] = mt

    def render(form: Form): TagContent = {
      fieldset(scalatags.Text.attrs.title := description, legend(title), items.map { field ⇒ form.renderItem(field) })
    }

    override def location = SimpleLocation(s"FieldSet($name)")


  }

  /** Scrupal Forms
    * This trait brings together all the form related objects nd generically represents an HTML5 Form in Scrupal.
    *
    */
  trait Form extends Container with Enablee with SingularProvider with Html.TemplateGenerator {
    lazy val segment: String = id.name

    def actionPath: String

    def items: Seq[Field]

    def values: Settings

    type ErrorMap = Map[Item, Contents]

    def errorMap: ErrorMap = Map.empty[Item, Contents]

    def hasErrors(field: Item): Boolean = errorMap.contains(field)

    def errorsOf(field: Item): Contents = errorMap.getOrElse(field, Seq.empty[Modifier])

    def withErrorMap(errorMap: ErrorMap): Form

    override def location = SimpleLocation(s"Form($name)")

    def toMap(mt: Map[String, Atom]): collection.Map[String, Atom] = mt


    /** Wrap a field's rendered element with an appropriate label and other formatting. This default is aimed at
      * Twitter bootstrap which is the default formatting for Scrupal.
      *
      * @param field The field whose rendered output should be wrapped
      * @return The TagContent for the final markup for the field
      */
    def wrap(field: Field): TagContent = {
      val the_label = _root_.scrupal.api.html.Forms.label(field.name, field.name, Seq(cls := "control-label text-info"))
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

    def apply(context: Context, args: ContentsArgs): Contents = { Seq(render) }

    def renderItem(item: Item): TagContent = {
      item match {
        case f: Field ⇒ wrap(f)
        case f: Item ⇒ f.render(this)
      }
    }

    def render: TagContent = {
      scalatags.Text.tags.form(
        scalatags.Text.attrs.action := actionPath, method := "POST", attrs.name := name,
        "enctype".attr := "application/x-www-form-urlencoded",
        items.map { field ⇒ renderItem(field) }
      )
    }

    def render(form: Form): TagContent = render()

    /** Provide Routing For Form Actions
      *
      * GET to the form name path renders the form. POST to the form name path decodes submitted form data.
      */
    def singularRoutes: ReactionRoutes = {
      case GET(p"$rest") ⇒ provideRenderFormAction(singularPrefix + rest)
      case POST(p"$rest") ⇒ provideAcceptFormAction(singularPrefix + rest)
    }

    /** Provide the form action
      *
      * This method from ActionProvider is invoked when a request is made to display or submit the form.
      * It simply checks the conditions and invokes a method to acquire the Action object corresponding
      * to rendering or submitting the form.
      *
      * @param matchingSegment The path segment used to select this ActionProvider
      * @return
      */

    def provideRenderFormAction(matchingSegment: String): RenderFormReaction = {
      new RenderFormReaction(this)
    }

    def provideAcceptFormAction(matchingSegment: String): AcceptReaction = {
      new AcceptReaction(this)
    }

  }

  class RenderFormReaction(val form: Form) extends Reactor {
    def name = "RenderForm"

    def description = "A Reaction that renders a form"

    def apply(stimulus: Stimulus): Future[Response] = {
      stimulus.context.withExecutionContext { implicit ec: ExecutionContext ⇒
        Future {
          HtmlResponse(form.render.toString(), Successful)
        }
      }
    }
  }

  class AcceptReaction(val form: Form) extends Reactor {
    def name = "AcceptForm"

    def description = "A Reaction that decodes submitted form data"

    /* FIXME: Rewrite Form Accept Handling to use Play
    def decodeFormData(r: Stimulus): Failure[JsValue] = {
      type FF = _root_.scrupal.api.FormField // spray unmarshalling also defines FormField
      val formItems: Map[String, FF] = {
          for (item ← form.items if item.isInstanceOf[FF]) yield {item.name → item.asInstanceOf[FF]}
        }.toMap
      val formData = FormData(r.uri.query)
      val data: LinearSeq[Results[JsObject]] = {
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
      val elems: Map[String, JsValue] = data.foldLeft(Seq.empty[(String, JsValue)]) {
        case (last, results) ⇒ last ++ results.value.value
      }.toMap
      val errors: Seq[Failure[JsValue]] = {
        {data.filter { result ⇒ result.isError }.map { r ⇒ r.asInstanceOf[Failure[JsValue]] }} ++ {
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
        Failures(vr.ref, vr.value, errors: _*)

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

  def formWithErrors(validationResults: Failure[JsValue]): Form = {
    val errorMap = validationResults.errorMap map {
      case (ref, msg) ⇒
        ref.asInstanceOf[FormItem] -> msg
    }
    form.withErrorMap(errorMap)
  }

  def handleValidatedFormData(doc: JsObject): Result[_] = {
    StringResult(s"Submission of '${form.name}' succeeded.", Successful)
  }

  def handleValidationFailure(failure: ValidationFailed[JsValue]): Result[_] = {
    val msg: StringBuilder = new StringBuilder()
    for (e ← failure.errors) msg.append(e.msgBldr).append("\n")
    FormErrorResult(failure, Unacceptable)
  }
*/
    def apply(stimulus: Stimulus): Future[Response] = {
      stimulus.context.withExecutionContext { implicit ec: ExecutionContext ⇒
        Future {
          ErrorResponse("Form Submission is not implemented", Unimplemented)
        }
      }
    }

    /*
    decodeFormData(stimulus.context) match {
      case Success(ref, doc) ⇒
        handleValidatedFormData(doc.asInstanceOf[JsObject])
      case err: ValidationFailed[JsValue] ⇒
        handleValidationFailure(err)
      case vr: Failure[JsValue] ⇒
        handleValidationFailure(ValidationFailed(form, JsNull, Seq(vr)))
    }
  }(context.scrupal._executionContext)
  */
  }

  case class Simple(
    id: Symbol,
    name: String,
    description: String,
    actionPath: String,
    items: Seq[Field],
    values: Settings = Settings.empty,
    override val errorMap: Map[Item, Contents] = Map.empty[Item, Contents]) extends Form {
    require(items.nonEmpty)

    def withErrorMap(em: ErrorMap): Form = copy(errorMap = em)
  }

  object emptyForm extends { val id = 'emptyForm } with Form {
    val name = ""
    val description = ""
    val actionPath = ""
    val items = Seq.empty[Field]
    val values = Settings.empty

    def withErrorMap(em: ErrorMap): Form = this
  }

}
