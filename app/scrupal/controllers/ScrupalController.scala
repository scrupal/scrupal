package scrupal.controllers

import play.api.libs.json.JsString
import scrupal.views.html
import play.api.mvc.{SimpleResult, Controller, RequestHeader}
import play.api.http.Writeable
import play.api.templates.Html
import scrupal.api.{Type, Module}
import org.omg.CosNaming.NamingContextPackage.NotFound

/** One line sentence description here.
  * Further description here.
  */
class ScrupalController extends Controller  {

  def notImplemented(what: JsString)(implicit writeable: Writeable[JsString]) : SimpleResult = {
    NotImplemented(JsString("NotImplemented: " + what) )
  }

  def notImplemented(what:String)(implicit writeable: Writeable[Html], request: RequestHeader ) : SimpleResult = {
    NotImplemented(html.errors.NotImplemented(spaces2underscores(what)))
  }

  def notFound(what:String)(implicit writeable: Writeable[Html], request: RequestHeader) : SimpleResult = {
    NotFound(html.errors.NotFound(spaces2underscores(what)))
  }

  def notFound(what: JsString)(implicit writeable: Writeable[Html],  request: RequestHeader) : SimpleResult = {
    NotFound(JsString("NotFound: " + what))
  }

  def spaces2underscores(what: String) = what.replaceAll(" ","_")

  def modules = Module.all
  def moduleNames : Seq[String]  = Module.all map { module: Module => module.label }
  def moduleTypeNames(mod:Module)  : Seq[String] = mod.types map { typ => typ.label }

  def types       : Seq[Type]    = Module.all flatMap { module => module.types }
  def typeNames   : Seq[String]  = types map { typ : Type => typ.label }

}
