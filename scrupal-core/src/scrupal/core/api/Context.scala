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

package scrupal.core.api

import java.net.URL

import scrupal.core.{CoreSchema, Scrupal}
import scrupal.db.DBContext
import scrupal.utils.Configuration

import spray.http._

import scala.concurrent.ExecutionContext

/** A generic Context trait with just enough defaulted information to render a BasicPage.
  * This allows us, regardless of the error condition of a page, to render custom errors at least in some
  * default way. Classes that mix in Context will override and extend what's available in their context.
  */
trait Context {
  val scrupal: Scrupal
  val site : Option[Site] = None
  val appName : String = "Application"
  val siteName : String = "Scrupal"
  val themeProvider : String = "scrupal"
  val themeName : String = "amelia"
  val user : String = "guest"
  val description : String = ""
  val uri: Uri = "http://localhost/"
  val method: HttpMethod = HttpMethods.GET
  val protocol: HttpProtocol = HttpProtocols.`HTTP/1.1`
  val headers: Map[String,HttpHeader] = Map.empty[String,HttpHeader]

  def alerts : Seq[Alert] = Seq()
  def suggestURL : URL = new URL("/")

  def withScrupalStuff[T]( f: (Configuration, DBContext, CoreSchema, ExecutionContext) => T) : T = {
     scrupal.withConfiguration { config =>
       scrupal.withCoreSchema { (dbc, db, cs) =>
         scrupal.withExecutionContext { ec =>
           f(config, dbc, cs, ec)
         }
       }
     }
  }
}

/** A Basic context which just mixes the Context trait with the WrappedRequest.
  * This information is generally overridden by subclasses, but this is the minimum we need to render a page. Note that
  * because this is a WrappedRequest[A], all the fields of Request are fields of this class too.
  * @param request The request upon which the context is based
  */
case class HttpContext(scrupal: Scrupal, request: HttpRequest) extends Context {
  def secure : Boolean = false
  val config = Settings.Empty
  override val uri = request.uri
  override val method = request.method
  override val protocol = request.protocol
  override val headers = request.headers.map { h ⇒ h.name → h } toMap
}

/** A Site context which pulls the information necessary to render something for a site.
  * SiteContext is presumed to be created with a SiteAction from the ContextProvider which will only create on if the
  * conditions are right, otherwise a BasicContext is created and an error returned.
  * @param theSite The site that this request should be processed by
  * @param request The request upon which the context is based
  */
class SiteContext(scrupal: Scrupal, theSite: Site, request: HttpRequest) extends HttpContext(scrupal, request) {
  override val site : Option[Site] = Some(theSite)
  override val siteName : String = theSite.label
  override val description : String = theSite.description
  override val themeProvider : String = "scrupal" // FIXME: Should be default theme provider for site
  override val themeName: String = "cyborg" // FIXME: Should be default theme for site
  val modules: Seq[Module] = Module.all //FIXME: Should be just the ones for the site
}

/** A User context which extends SiteContext by adding specific user information.
  * Details TBD.
  * @param user The user that initiated the request.
  * @param site The site that this request should be processed by
  * @param request The request upon which the context is based
  */
class UserContext(scrupal: Scrupal, override val user: String, site: Site, request: HttpRequest)
    extends SiteContext(scrupal, site, request) {
  val principal  = Nil // TODO: Finish UserContext implementation
}

/** Some utility applicators for constructing the various Contexts */
object Context {
  def apply(scrupal: Scrupal, request: HttpRequest) = new HttpContext(scrupal, request)
  def apply(scrupal: Scrupal, site: Site, request: HttpRequest) = new SiteContext(scrupal, site, request)
  def apply(scrupal: Scrupal, user: String, site: Site, request: HttpRequest) =
    new UserContext(scrupal, user, site, request)


}


/* A trait for producing a Request's context via ContextualAction action builder
  * This trait simply defines the ContextualAction object which is an ActionBuilder with a ConcreteContext type. We
  * construct the ConcreteContext using the request provided to invokeBlock[A] and then invoke the block or generate
  * an error if this is more appropriate. Note that we derive from RichResults so the error factories there can be
  * used. This ContextualAction is used by controllers wherever they need Scrupal relevant contextual information.
  * Their action block is provided a ConcreteContext from which information can be derived.

trait ContextProvider extends ((HttpRequest) ⇒ Context) {
  val scrupal: Scrupal

  def apply(request: HttpRequest) : Context = {
    if (scrupal.isReady) {
      // HTTP Requires the Host Header, but we guard anyway and assume "localhost" if its empty
      val host = request.uri.authority.host.address
      val port = request.uri.authority.port
      val site = Site.forHost(host) map { site: Site ⇒
        if (site.enabled) {
          if (site.requireHttps) {
            request.protocol.value {
              case "https" ⇒ Context(site, request)
              case _ => Context(request)
            }
          } else if (request.protocl{
            site.withCoreSchema { schema: CoreSchema =>
              block( Context(schema, site, request) )
            }
              }
            } else {
              implicit val ctxt = Context(request)
              Future.successful(Forbidden("browse site '" + site.data._id.name + "'", "it is disabled"))
            }
          }
        }  getOrElse {
          implicit val ctxt = Context(request)
          Future.successful(NotFound("content for site '" + host + "'"))
        }
      } else {
        implicit val ctxt = Context(request)
        Future.successful(Redirect(routes.ConfigWizard.configure))
      }
    }
  }
  object SiteAction extends SiteAction

  /** A simple rename of the unwieldy ConcreteContext[AnyContent] that gets used frequently. */
  type AnySiteContext = SiteContext[AnyContent]

  type UserActionBlock[A] = (UserContext[A]) => Future[Result]

  class UserAction extends ActionBuilder[UserContext] {
    def invokeBlock[A](request: HttpRequest, block: UserActionBlock[A]) : Future[Result] = {
      SiteAction.invokeBlock(request, {
        context: SiteContext[A] => {
          val user = "guest" // FIXME: Need to look up the actual user
          block( Context(user, context.schema, context.site, request))
        }
      })
    }
  }
  object UserAction extends UserAction

  type AnyUserContext = UserContext[AnyContent]
}
*/