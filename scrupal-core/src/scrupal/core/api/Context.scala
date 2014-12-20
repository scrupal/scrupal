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

import java.net.URL

import akka.actor.ActorSystem
import scrupal.db.DBContext
import scrupal.utils.Configuration

import spray.http._
import spray.routing.RequestContext

import scala.concurrent.ExecutionContext

/** A generic Context trait with just enough defaulted information to render a BasicPage.
  * This allows us, regardless of the error condition of a page, to render custom errors at least in some
  * default way. Classes that mix in Context will override and extend what's available in their context.
  */
trait Context {
  // Scrupal Related things
  def scrupal: Scrupal

  // Request related things
  val request : Option[RequestContext] = None
  def uri      = Uri("<NoUri>")
  def method   = HttpMethod.custom("NONE",false,true,false)
  def protocol = HttpProtocols.`HTTP/1.1`
  def headers  = Map.empty[String,HttpHeader]

  // Site related things
  val site : Option[Site] = None
  def siteName : String = "<NoSite>"

  val application : Option[Application] = None
  def appName : String = "<NoApplication>"
  def appPath : String = "<NoPath>"

  val themeProvider : String = "bootswatch"
  val themeName : String = "default"
  val user : String = "guest"
  val description : String = ""

  def alerts : Seq[Alert] = Seq()
  def suggestURL : URL = new URL("/")

  def withConfiguration[T](f: (Configuration) ⇒ T) : T = { scrupal.withConfiguration(f) }

  def withDBContext[T](f: (DBContext) ⇒ T) : T = { scrupal.withDBContext(f) }

  def withSchema[T](f: (DBContext, Schema) => T) : T = { scrupal.withSchema(f) }

  def withExecutionContext[T](f: (ExecutionContext) => T) : T = { scrupal.withExecutionContext(f) }

  def withActorSystem[T](f : (ActorSystem) ⇒ T) : T = { scrupal.withActorSystem(f) }

}

/** A Basic context which just mixes the Context trait with the WrappedRequest.
  * This information is generally overridden by subclasses, but this is the minimum we need to render a page. Note that
  * because this is a WrappedRequest[A], all the fields of Request are fields of this class too.
  * @param scrupal The Scrupal object
  * @param rqst The request upon which the context is based
  */
class SprayContext(val scrupal: Scrupal, rqst: RequestContext) extends Context {
  require(scrupal != null)
  require(rqst != null)
  override val request : Option[RequestContext] = Some(rqst)
  override val uri = rqst.request.uri
  override val method = rqst.request.method
  override val protocol = rqst.request.protocol
  override val headers  = rqst.request.headers.map { h ⇒ h.name → h}.toMap
}

/** A Site context which pulls the information necessary to render something for a site.
  * SiteContext is presumed to be created with a SiteAction from the ContextProvider which will only create on if the
  * conditions are right, otherwise a BasicContext is created and an error returned.
  * @param theSite The site that this request should be processed by
  * @param request The request upon which the context is based
  */
class SiteContext(scrupal: Scrupal, request: RequestContext, theSite: Site) extends SprayContext(scrupal, request) {
  require(theSite != null)
  override val site : Option[Site] = Some(theSite)
  override val siteName : String = theSite.name
  override val description : String = theSite.description
  override val themeProvider : String = theSite.themeProvider
  override val themeName: String = theSite.themeName
}

class ApplicationContext(scrupal: Scrupal, request: RequestContext, theSite: Site, app: Application)
  extends SiteContext(scrupal, request, theSite) {
  require(app != null)
  override val application : Option[Application] = Some(app)
  override val appName = app.name
  override val appPath = app.key
}

/** A User context which extends SiteContext by adding specific user information.
  * Details TBD.
  * @param principal The user that initiated the request.
  * @param site The site that this request should be processed by
  * @param request The request upon which the context is based
  */
class UserContext(scrupal: Scrupal, request: RequestContext, site: Site, principal: Principal)
    extends SiteContext(scrupal, request, site) {
  // TODO: Finish UserContext implementation
  override val user: String = principal._id.name
}

/** Some utility applicators for constructing the various Contexts */
object Context {
  def apply(scrupal: Scrupal, request: RequestContext) = new SprayContext(scrupal, request)

  def apply(scrupal: Scrupal, request: RequestContext, site: Site) = new SiteContext(scrupal, request, site)

  def apply(scrupal: Scrupal, request: RequestContext, site: Site, app: Application) =
    new ApplicationContext(scrupal, request, site, app)

  def apply(scrupal: Scrupal, request: RequestContext, site: Site, principal: Principal) =
    new UserContext(scrupal, request, site, principal)

}
