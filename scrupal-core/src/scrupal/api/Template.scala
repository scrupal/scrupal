/**********************************************************************************************************************
 * Copyright © 2014 Reactific Software LLC                                                                            *
 *                                                                                                                    *
 * This file is part of Scrupal, an Opinionated Web Application Framework.                                            *
 *                                                                                                                    *
 * Scrupal is free software: you can redistribute it and/or modify it under the terms                                 *
 * of the GNU General Public License as published by the Free Software Foundation,                                    *
 * either version 3 of the License, or (at your option) any later version.                                            *
 *                                                                                                                    *
 * Scrupal is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;                               *
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                          *
 * See the GNU General Public License for more details.                                                               *
 *                                                                                                                    *
 * You should have received a copy of the GNU General Public License along with Scrupal.                              *
 * If not, see either: http://www.gnu.org/licenses or http://opensource.org/licenses/GPL-3.0.                         *
 **********************************************************************************************************************/

package scrupal.api

import play.twirl.api.{Txt, Html}
import reactivemongo.bson.{BSONString, BSONHandler}
import scrupal.utils.{Registry, Registrable}
import spray.http.{MediaTypes, ContentTypes, ContentType}

trait Template[T] extends Registrable[Template[_]] with Describable with ( (Map[String,T], Context) => T ) {
  def contentType : ContentType
  def registry = Template
  def asT : this.type = this
}

object Template extends Registry[Template[_]] {
  val registryName = "Templates"
  val registrantsName = "template"

  type TwirlHtmlTemplateFunction = { def apply(args: Map[String,Html])(implicit ctxt: Context):Html }
  type TwirlTxtTemplateFunction  = { def apply(args: Map[String,Txt])(implicit ctxt: Context):Txt }

  lazy val default = TwirlHtmlTemplate('default, "Default Template", scrupal.api.views.html.defaultTemplate)

  /** Handle reading/writing Template instances to and from BSON.
    * Note that templates are a little special. We write them as strings and restore them via lookup. Templates are
    * intended to only ever live in memory but they can be references in the database. So when a Template is a field
    * of some class that is stored in the database, what actually gets stored is just the name of the template.
    */
  class BSONHandlerForTemplate[T <: Registrable[_]] extends BSONHandler[BSONString,T] {
    override def write(t: T): BSONString = BSONString(t.id.name)
    override def read(bson: BSONString): T = Template.as(Symbol(bson.value))
  }
}

/** Twirl Html Template
  *
  * Since Twirl templates cannot be stored in the database, we use a registry of Template objects to
  * allow them to be referenced from the database. This is essentially a step below an Arranger[P] where instead
  * of producing a Result[P] we produce an Html or Txt or other Twirl supported type. Also, TemplateInvokers are
  * Registrable and must conform to that interface
  */
case class TwirlHtmlTemplate(
  id : Symbol,
  description: String,
  template: Template.TwirlHtmlTemplateFunction
) extends Template[Html] {
  val contentType : ContentType = MediaTypes.`text/html`
  def apply(args: Map[String,Html], context: Context) : Html = { template.apply(args)(context) }
}

/** Twirl Txt Template
  *
  * Since Twirl templates cannot be stored in the database, we use a registry of Template objects to
  * allow them to be referenced from the database. This is essentially a step below an Arranger[P] where instead
  * of producing a Result[P] we produce an Txt or Txt or other Twirl supported type. Also, TemplateInvokers are
  * Registrable and must conform to that interface
  */
case class TwirlTxtTemplate(
  id : Symbol,
  description: String,
  template: Template.TwirlTxtTemplateFunction
) extends Template[Txt] {
  val contentType : ContentType = ContentTypes.`text/plain(UTF-8)`
  def apply(args: Map[String,Txt], context: Context) : Txt = { template.apply(args)(context) }
}
