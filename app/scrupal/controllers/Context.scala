/**********************************************************************************************************************
 * This file is part of Scrupal a Web Application Framework.                                                          *
 *                                                                                                                    *
 * Copyright (c) 2013, Reid Spencer and viritude llc. All Rights Reserved.                                            *
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

package scrupal.controllers

import play.api.mvc._
import scrupal.api.{Site, Module}
import scrupal.models.CoreModule
import play.api.Configuration
import play.api.Play.current
import scala.concurrent.Future
import scrupal.db.Alert
import scala.Some
import play.api.mvc.SimpleResult

/** A generic Context trait that all the views and tests and other users of Context can utilize without having to deal
  * with the type parameter needed by ConcreteContext.
  * This is intended to be an augmentation of Request and in most cases the request body is not needed. When it is,
  * they can always use ConcreteRequest[A] which derives from this trait.
  */
trait Context extends RequestHeader {
  def site : Option[Site]
  def user : Option[String]
  def appName : String
  def modules: Seq[Module]
  def themeName : String
  def themeProvider : String
  def config : Configuration

  def alerts : Seq[Alert]

  def suggestURL : String
}

/**
 * A ConcreteContext with a type parameter for the content.
 * This is where the Context construction work gets done.
 */
class ConcreteContext[A](val site : Option[Site] = None, request: Request[A]) extends WrappedRequest[A](request) with
                                                                                      Context
{
  val user: Option[String] = None
  val appName : String = site.map( s => s.id.name ).getOrElse( "Scrupal")
  val modules: Seq[Module] = site.map( s => s.modules ).getOrElse(Seq(CoreModule))
  val themeName: String = "amelia"
  val themeProvider: String = "scrupal"
  val instance: String = "instanceName"
  val config: Configuration = current.configuration

  def alerts : Seq[Alert] = Seq()

  def suggestURL : String = {
    import routes.{Home => rHome}
    import routes.{APIDoc => rAPIDoc}
    import routes.{ConfigWizard => rConfig}
    request.path match {
      case s if s.startsWith("/api") => rAPIDoc.introduction().url
      case s if s.startsWith("/doc") => rAPIDoc.introduction().url
      case s if s.startsWith("/config") => rConfig.configure().url
      case s if s.startsWith("/asset") => rHome.docPage("assets").url
      case s if s.startsWith("/scaladoc") => rHome.scalaDoc("/").url
      case _ => rHome.index().url
    }
  }
}


/** A trait for producing a Request's context via ContextualAction action builder
  * This trait simply defines the ContextualAction object which is an ActionBuilder with a ConcreteContext type. We
  * construct the ConcreteContext using the request provided to invokeBlock[A] and then invoke the block or generate
  * an error if this is more appropriate. Note that we derive from RichResults so the error factories there can be
  * used. This ContextualAction is used by controllers wherever they need Scrupal relevant contextual information.
  * Their action block is provided a ConcreteContext from which information can be derived.
  */
trait ContextProvider extends RichResults {

  object ContextualAction extends ActionBuilder[ConcreteContext] {
    def invokeBlock[A](request: Request[A], block: ConcreteContext[A] => Future[SimpleResult]):
    Future[SimpleResult]
    = {
      if (Global.ScrupalIsConfigured) {
        val parts : Array[String] = request.host.split(":")
        // HTTP Requires the Host Header, but we guard anyway and assume "localhost" if its empty
        val host: String = if (parts.length > 0) parts(0) else "localhost" ;
        // Look up the Site by host requested in the request and deal with what we find
        {
          Global.DataYouShouldNotModify.sites.get(host) map { site: Site =>
            if (site.enabled) {
              if (site.requireHttps) {
                request.headers.get("X-Forwarded-Proto").collect {
                  case "https" => block( new ConcreteContext(Some(site), request) )
                  case _ => {
                    implicit val ctxt = new ConcreteContext(None, request)
                    Future.successful(Forbidden("request to site '" + site.id.name + "'", "it requires https."))
                  }
                } getOrElse {
                  implicit val ctxt = new ConcreteContext(None, request)
                  Future.successful(Forbidden("request to site '" + site.id.name + "'", "it requires https."))
                }
              } else {
                block( new ConcreteContext(Some(site), request) )
              }
            } else {
              implicit val ctxt = new ConcreteContext(None, request)
              Future.successful(Forbidden("browse site '" + site.id.name + "'", "it is disabled"))
            }
          }
        }  getOrElse {
          implicit val ctxt = new ConcreteContext(None, request)
          Future.successful(NotFound("content for site '" + host + "'"))
        }
      } else {
        implicit val ctxt = new ConcreteContext(None, request)
        Future.successful(Redirect(routes.ConfigWizard.configure))
      }
    }
  }

  /** A simple rename of the unwieldy ConcreteContext[AnyContent] that gets used frequently. */
  type AnyContext = ConcreteContext[AnyContent]
}

