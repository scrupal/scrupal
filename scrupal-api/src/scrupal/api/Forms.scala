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

package scrupal.api

import reactivemongo.bson._
import scrupal.api.types.BooleanType
import scrupal.db.Storable

/** Scrupal Forms
  * This object just contains the form related things. Note that this is a very general notion of forms and not
  * much tied to the HTML ideas. Forms are Inputs inside Sections inside Pages. Inputs are named Types. All of
  * these are storable and are Validators so we can validate a BSON document against a form. This should be used
  * for all input to Scrupal.
  */
object Forms {

  type FormOptions = Map[Symbol, String]
  val EmptyOptions = Map.empty[Symbol,String]

  object opts {
    def apply(opts: Seq[(Symbol, String)]): FormOptions = opts.toMap

//    def apply(opts: (Symbol, Any)*): FormOptions = opts.toMap

    def apply(opts: FormOptions): FormOptions = opts

    def apply(opts: List[String]): FormOptions = opts.map(v => Symbol(v) -> v).toMap
  }

  object htmlAttrs {
    def apply(input: Input) = {
      val s = new StringBuilder()
      def add(k:String,v:String) = s.append(k).append('"').append(v).append('"').append(' ')
      input.typ match {
        case b: BooleanType ⇒
          add("type","checkbox")
          add("id",input._id)
          add("name", input._id)
          val value = input.opts.getOrElse('value,"true")
          add("value", value)
          if (true) // FIXME: Need to get the value in the form and see if it is set
            s.append("checked")
        case _ ⇒ ???
      }
    }
  }

  trait FormItem extends Storable[String] with BSONValidator[BSONValue] {
    def default: BSONValue
  }


  /** A Description of a Single Input Item.
    *
    * This wraps a name and a description around a Type, a default BSONValue for that Type, and options for the
    * type.
    * @param _id
    * @param description
    * @param typ
    * @param default
    * @param opts
    */
  case class Input(
    _id: String,
    description: String,
    typ: Type,
    default: BSONValue = BSONNull,
    opts: FormOptions = EmptyOptions
  ) extends FormItem {
    require(typ.nonTrivial)

    def apply(value: BSONValue): ValidationResult = typ(value)

    def id: String = opts.getOrElse('id,_id).toString

    def fill[T](t: T): Input = ???
  }

  case class Section(
    _id: String,
    description: String,
    inputs: Seq[Input],
    default: BSONValue = BSONNull
  ) extends FormItem {
    require(inputs.length > 0)

    def apply(value: BSONValue): ValidationResult = {
      value match {
        case a: BSONArray if a.length != inputs.length =>
          single(value) { _ => Some(s"Number of inputs in document doesn't match expected number for section ${_id}")}
        case a: BSONArray => validateArray(a, inputs)
        case x: BSONValue => single(value) { _ => wrongClass("BSONArray", x)}
      }
    }

    def fill[T](t: T): Input = ???
  }

  case class Page(
    _id: String,
    description: String,
    sections: Seq[Section],
    default: BSONValue = BSONNull
  ) extends FormItem {
    require(sections.length > 0)

    def apply(value: BSONValue): ValidationResult = {
      value match {
        case a: BSONArray if a.length != sections.length =>
          single(value) { _ => Some(s"Number of inputs in document doesn't match expected number for page ${_id}")}
        case a: BSONArray => validateArray(a, sections)
        case x: BSONValue => single(value) { _ => wrongClass("BSONArray", x)}
      }
    }

    def fill[T](t: T): Input = ???
  }

  case class SubmitAction(_id: String, handler: Option[Handler]) extends BSONValidator[BSONValue] {
    def apply(value: BSONValue): ValidationResult = None // TODO: Implement this
  }

  case class Form(
    _id: String,
    submit: SubmitAction,
    pages: Seq[Page],
    default: BSONValue = BSONNull
  ) extends FormItem {
    require(pages.length > 0)

    def apply(value: BSONValue): ValidationResult = {
      value match {
        case x: BSONDocument if x.getAs[BSONString]("_id").getOrElse(BSONString("")).value != _id ⇒
          Some(Seq(s"Form field '_id' does not correspond to '${_id}'"))
        case x: BSONDocument if x.get("submit").isEmpty =>  Some(Seq(s"Document has no field named 'submit'"))
        case x: BSONDocument if x.get("pages").isEmpty =>   Some(Seq("Document has no field named 'pages'"))
        case x: BSONDocument if x.get("default").isEmpty ⇒ Some(Seq("Document has no field named 'default'"))
        case x: BSONDocument =>
          val submitVal = x.get("submit").get
          val pagesVal = x.get("pages").get
          submit(submitVal).map {
            result => result ++ {
              pagesVal match {
                case a: BSONArray if a.length != pages.length =>
                  Seq(s"Number of inputs in document doesn't match expected number for section ${_id}")
                case a: BSONArray => validateArray(a, pages).getOrElse(Seq.empty[String])
                case x: BSONValue => Seq(wrongClass("BSONArray", x).getOrElse(""))
              }
            }
          }
        case x: BSONValue => single(value) { _ => wrongClass("BSONDocument", x)}
      }
    }

    def fill[T](t: T): Input = ???
  }

  // type InputGenerator = (Input) ⇒ Html
  // type InputWrapper = (Input,Html) ⇒ Html
}
