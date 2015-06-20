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

import java.net.URL

import akka.actor.ActorSystem
import play.api.Configuration
import scrupal.storage.api.{Schema, StoreContext}

import scala.concurrent.ExecutionContext

/** A generic Context trait with just enough defaulted information to render a BasicPage.
  * This allows us, regardless of the error condition of a page, to render custom errors at least in some
  * default way. Classes that mix in Context will override and extend what's available in their context.
  */
trait Context {
  // Scrupal Related things
  val scrupal : Scrupal

  // Site related things
  val site : Option[Site] = None
  val application : Option[Application] = None
  val principal : Option[Principal] = None

  def siteName : String = "<NoSite>"
  val themeProvider : String = "bootswatch"
  val themeName : String = "default"
  val description : String = ""
  val user : String = "guest"

  def suggestURL : URL = new URL("/")

  def withConfiguration[T](f : (Configuration) ⇒ T) : T = { scrupal.withConfiguration(f) }

  def withStorageContext[T](f : (StoreContext) ⇒ T) : T = { scrupal.withStorageContext(f) }

  def withSchema[T](name : String)(f : (StoreContext, Schema) ⇒ T) : T = {
    withStorageContext { ctxt ⇒
      ctxt.withSchema(name) { schema ⇒ f(ctxt, schema) }
    }
  }

  def withExecutionContext[T](f : (ExecutionContext) ⇒ T) : T = { scrupal.withExecutionContext(f) }

  def withActorSystem[T](f : (ActorSystem) ⇒ T) : T = { scrupal.withActorSystem(f) }
}

/** A Basic context which just mixes the Context trait with the WrappedRequest.
  * This information is generally overridden by subclasses, but this is the minimum we need to render a page. Note that
  * because this is a WrappedRequest[A], all the fields of Request are fields of this class too.
  * @param scrupal The Scrupal object
  */
case class SimpleContext(scrupal : Scrupal) extends Context {
  require(scrupal != null)
}

/** A Site context which pulls the information necessary to render something for a site.
  * SiteContext is presumed to be created with a SiteAction from the ContextProvider which will only create on if the
  * conditions are right, otherwise a BasicContext is created and an error returned.
  * @param theSite The site that this request should be processed by
  */
class SiteContext(scrupal : Scrupal, theSite : Site) extends SimpleContext(scrupal) {
  require(theSite != null)
  override val site : Option[Site] = Some(theSite)
  override val siteName : String = theSite.name
  override val description : String = theSite.description
  override val themeProvider : String = theSite.themeProvider
  override val themeName : String = theSite.themeName
}

/** A User context which extends SiteContext by adding specific user information.
  * Details TBD.
  * @param principal The user that initiated the request.
  * @param site The site that this request should be processed by
  */
class UserContext(scrupal : Scrupal, site : Site, principal : Principal)
  extends SiteContext(scrupal,  site) {
  // TODO: Finish UserContext implementation
  override val user : String = principal._id.name
}

/** Some utility applicators for constructing the various Contexts */
object Context {
  def apply(scrupal : Scrupal) =
    new SimpleContext(scrupal)

  def apply(scrupal : Scrupal, site : Site) =
    new SiteContext(scrupal, site)

  def apply(scrupal : Scrupal, site : Site, principal : Principal) =
    new UserContext(scrupal, site, principal)

}
