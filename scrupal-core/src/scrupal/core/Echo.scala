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

package scrupal.core

import org.joda.time.DateTime
import reactivemongo.bson.BSONDocument
import scrupal.api._
import scrupal.utils.OSSLicense

import scala.concurrent.Future


/** An Echoing Application
  * Almost the simplest of applications to construct this is mostly a test of the fundamentals but may have some
  * utility as heartbeat between servers. This application servers one entity, an echo entity, which takes in
  * requests, formats them into HTML and responds with the content. When you go to the app you see the GET
  * request for the page you requested as the response. It has one special page which allows you to submit a
  * form. Query args are returned if you provide them on the web page. This could also be used for benchmarking.
  * Created by reid on 11/11/14.
  */
object EchoApp extends Application {

  lazy val id: Symbol = 'Echo

  val name: String = "Echo Application"

  val description: String = "An Application For echoing web requests back to your browser"

  val kind: Symbol = 'Echo

  val requiresAuthentication = false

  override def modules: Seq[Module] = Seq(CoreModule)

  def created: Option[DateTime] = Some(new DateTime(2014,11,11,5,53))

  def modified: Option[DateTime] = None

  EchoEntity.enable(this)
  CoreModule.enable(this)

}

/** The Echo Entity
  * This is really the heart of the EchoApp. All the requests that get echoed go through here.
  */

object EchoEntity extends Entity {

  def id: Symbol = 'Echo

  def kind: Symbol = 'Echo

  def instanceType: BundleType = BundleType.Empty

  def author: String = "Reid Spencer"

  def copyright: String = "© 2014, 2015 Reid Spencer. All Rights Reserved."

  def license: OSSLicense = OSSLicense.GPLv3

  def description: String = "An entity that stores nothing and merely echos its requests"

  override def create(context: ApplicationContext, id: String, instance: BSONDocument) : Create = {
    new Create(context, id, instance) {
      override def apply() : Future[Result[_]] = {
        Future.successful( HtmlResult(scrupal.core.views.html.echo.create(id, instance)(context)) )
      }
    }
  }

  override def retrieve(context: ApplicationContext, id: String) : Retrieve = {
    new Retrieve(context, id) {
      override def apply : Future[Result[_]] = {
        Future.successful( HtmlResult(scrupal.core.views.html.echo.retrieve(id)(context)) )
      }
    }
  }

  override def update(context: ApplicationContext, id: String, fields: BSONDocument) : Update = {
    new Update(context, id, fields) {
      override def apply : Future[Result[_]] = {
        Future.successful( HtmlResult(scrupal.core.views.html.echo.update(id, fields)(context)) )
      }
    }
  }

  override  def delete(context: ApplicationContext, id: String) : Delete = {
    new Delete(context, id) {
      override def apply : Future[Result[_]] = {
        Future.successful( HtmlResult(scrupal.core.views.html.echo.delete(id)(context)) )
      }
    }
  }


  override def query(context: ApplicationContext, id: String, fields: BSONDocument) : Query = {
    new Query(context, id, fields) {
      override def apply : Future[Result[_]] = {
        Future.successful( HtmlResult(scrupal.core.views.html.echo.query(id, fields)(context)) )
      }
    }
  }

  override def createFacet(context: ApplicationContext, id: String,
                           what: Seq[String], instance: BSONDocument) : CreateFacet = {
    new CreateFacet(context, id, what, instance) {
      override def apply : Future[Result[_]] = {
        Future.successful( HtmlResult(scrupal.core.views.html.echo.createFacet(id, what, instance)(context)) )
      }
    }
  }

  override def retrieveFacet(context: ApplicationContext, id: String, what: Seq[String]) : RetrieveFacet = {
    new RetrieveFacet(context, id, what) {
      override def apply : Future[Result[_]] = {
        Future.successful( HtmlResult(scrupal.core.views.html.echo.retrieveFacet(id, what)(context)) )
      }
    }
  }

  override def updateFacet(context: ApplicationContext, id: String,
                           what: Seq[String], fields: BSONDocument) : UpdateFacet = {
    new UpdateFacet(context, id, what, fields) {
      override def apply : Future[Result[_]] = {
        Future.successful( HtmlResult(scrupal.core.views.html.echo.updateFacet(id, what, fields)(context)) )
      }
    }
  }

  override def deleteFacet(context: ApplicationContext, id: String, what: Seq[String]) : DeleteFacet = {
    new DeleteFacet(context, id, what) {
      override def apply : Future[Result[_]] = {
        Future.successful( HtmlResult(scrupal.core.views.html.echo.deleteFacet(id, what)(context)) )
      }
    }
  }

  override def queryFacet(context: ApplicationContext, id: String,
                          what: Seq[String], args: BSONDocument) : QueryFacet = {
    new QueryFacet(context, id, what, args) {
      override def apply : Future[Result[_]] = {
        Future.successful( HtmlResult(scrupal.core.views.html.echo.queryFacet(id, what, args)(context)) )
      }
    }
  }
}
