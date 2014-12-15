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

package scrupal.core

import reactivemongo.bson.BSONDocument

import scala.concurrent.Future

import scrupal.api._
import scrupal.utils.OSSLicense


/** The Echo Entity
  *
  * An entity object that takes in an HTTP request and formats it into HTML content which is the response. As such,
  * it simply echos its input to its output. This can be be used for benchmarking the routing and dispatch and layout
  * parts of Scrupal since the response content is generated computationally without blocking.
  */
object EchoEntity extends Entity('Echo) {

  def kind: Symbol = 'Echo

  val key: String = "Echo"

  def instanceType: BundleType = BundleType.Empty

  def author: String = "Reid Spencer"

  def copyright: String = "© 2014, 2015 Reid Spencer. All Rights Reserved."

  def license: OSSLicense = OSSLicense.GPLv3

  def description: String = "An entity that stores nothing and merely echos its requests"

  override def create(context: Context, id: String, instance: BSONDocument) : Create = {
    new Create(context, id, instance) {
      override def apply() : Future[Result[_]] = {
        Future.successful( HtmlResult(scrupal.core.views.html.echo.create(id, instance)(context)) )
      }
    }
  }

  override def retrieve(context: Context, id: String) : Retrieve = {
    new Retrieve(context, id) {
      override def apply : Future[Result[_]] = {
        Future.successful( HtmlResult(scrupal.core.views.html.echo.retrieve(id)(context)) )
      }
    }
  }

  override def update(context: Context, id: String, fields: BSONDocument) : Update = {
    new Update(context, id, fields) {
      override def apply : Future[Result[_]] = {
        Future.successful( HtmlResult(scrupal.core.views.html.echo.update(id, fields)(context)) )
      }
    }
  }

  override  def delete(context: Context, id: String) : Delete = {
    new Delete(context, id) {
      override def apply : Future[Result[_]] = {
        Future.successful( HtmlResult(scrupal.core.views.html.echo.delete(id)(context)) )
      }
    }
  }


  override def query(context: Context, id: String, fields: BSONDocument) : Query = {
    new Query(context, id, fields) {
      override def apply : Future[Result[_]] = {
        Future.successful( HtmlResult(scrupal.core.views.html.echo.query(id, fields)(context)) )
      }
    }
  }

  /* FIXME:
  override def createFacet(context: Context, id: String,
                           what: Seq[String], instance: BSONDocument) : CreateFacet = {
    new CreateFacet(context, id, what, instance) {
      override def apply : Future[Result[_]] = {
        Future.successful( HtmlResult(scrupal.core.views.html.echo.createFacet(id, what, instance)(context)) )
      }
    }
  }

  override def retrieveFacet(context: Context, id: String, what: Seq[String]) : RetrieveFacet = {
    new RetrieveFacet(context, id, what) {
      override def apply : Future[Result[_]] = {
        Future.successful( HtmlResult(scrupal.core.views.html.echo.retrieveFacet(id, what)(context)) )
      }
    }
  }

  override def updateFacet(context: Context, id: String,
                           what: Seq[String], fields: BSONDocument) : UpdateFacet = {
    new UpdateFacet(context, id, what, fields) {
      override def apply : Future[Result[_]] = {
        Future.successful( HtmlResult(scrupal.core.views.html.echo.updateFacet(id, what, fields)(context)) )
      }
    }
  }

  override def deleteFacet(context: Context, id: String, what: Seq[String]) : DeleteFacet = {
    new DeleteFacet(context, id, what) {
      override def apply : Future[Result[_]] = {
        Future.successful( HtmlResult(scrupal.core.views.html.echo.deleteFacet(id, what)(context)) )
      }
    }
  }

  override def queryFacet(context: Context, id: String,
                          what: Seq[String], args: BSONDocument) : QueryFacet = {
    new QueryFacet(context, id, what, args) {
      override def apply : Future[Result[_]] = {
        Future.successful( HtmlResult(scrupal.core.views.html.echo.queryFacet(id, what, args)(context)) )
      }
    }
  }
  */
}
