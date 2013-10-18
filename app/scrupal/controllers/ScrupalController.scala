package scrupal.controllers

import scrupal.views.html
import play.api.mvc._
import play.api.http.Writeable
import play.api.templates.Html
import scrupal.api.{Module, Type}
import play.api.libs.json.JsString
import play.api.mvc.SimpleResult
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat

/** One line sentence description here.
  * Further description here.
  */
trait ScrupalController extends Controller with ContextProvider {

  def notImplemented(what: JsString)(implicit writable: Writeable[JsString], request: RequestHeader) : SimpleResult = {
    NotImplemented(JsString("NotImplemented: " + what) )
  }

  def notImplemented(what:String)(implicit writable: Writeable[Html], request: RequestHeader ) : SimpleResult = {
    NotImplemented(html.errors.NotImplemented(spaces2underscores(what)))
  }

  def notFound(what:String)(implicit writable: Writeable[Html], request: RequestHeader) : SimpleResult = {
    NotFound(html.errors.NotFound(spaces2underscores(what)))
  }

  def notFound(what: JsString)(implicit writable: Writeable[Html], request: RequestHeader) : SimpleResult = {
    NotFound(JsString("NotFound: " + what.value))
  }

  def movedPermanently(where: String)(implicit writable: Writeable[Html], request: RequestHeader) : SimpleResult = {
     MovedPermanently(where)
  }

  def spaces2underscores(what: String) = what.replaceAll(" ","_")

  def modules = Module.all
  def moduleNames : Seq[String]  = Module.all map { module: Module => module.label }
  def moduleTypeNames(mod:Module)  : Seq[String] = mod.types map { typ => typ.label }

  def types       : Seq[Type]    = Module.all flatMap { module => module.types }
  def typeNames   : Seq[String]  = types map { typ : Type => typ.label }

  def dateStr(millis: Long) : String = new DateTime(millis).toString(ISODateTimeFormat.dateTime)

}
