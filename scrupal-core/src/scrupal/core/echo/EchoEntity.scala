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

package scrupal.core.echo

import reactivemongo.bson.BSONDocument
import scrupal.api.HtmlResult
import scrupal.core.BundleType
import scrupal.api._
import scrupal.utils.OSSLicense

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
      override def apply : HtmlResult = {
        HtmlResult(scrupal.core.echo.html.create(id, instance)(context))
      }
    }
  }

  override def retrieve(context: ApplicationContext, id: String) : Retrieve = {
    new Retrieve(context, id) {
      override def apply : HtmlResult = {
        HtmlResult(scrupal.core.echo.html.retrieve(id)(context))
      }
    }
  }

  override def update(context: ApplicationContext, id: String, fields: BSONDocument) : Update = {
    new Update(context, id, fields) {
      override def apply : HtmlResult = {
        HtmlResult(scrupal.core.echo.html.update(id, fields)(context))
      }
    }
  }

  override  def delete(context: ApplicationContext, id: String) : Delete = {
    new Delete(context, id) {
      override def apply : HtmlResult = {
        HtmlResult(scrupal.core.echo.html.delete(id)(context))
      }
    }
  }


  override def query(context: ApplicationContext, id: String, fields: BSONDocument) : Query = {
    new Query(context, id, fields) {
      override def apply : HtmlResult = {
        HtmlResult(scrupal.core.echo.html.query(id, fields)(context))
      }
    }
  }

  override def createFacet(context: ApplicationContext, id: String,
                           what: List[String], instance: BSONDocument) : CreateFacet = {
    new CreateFacet(context, id, what, instance) {
      override def apply : HtmlResult = {
        HtmlResult(scrupal.core.echo.html.createFacet(id, what, instance)(context))
      }
    }
  }


  override def retrieveFacet(context: ApplicationContext, id: String, what: List[String]) : RetrieveFacet = {
    new RetrieveFacet(context, id, what) {
      override def apply : HtmlResult = {
        HtmlResult(scrupal.core.echo.html.retrieveFacet(id, what)(context))
      }
    }
  }

  override def updateFacet(context: ApplicationContext, id: String,
                           what: List[String], fields: BSONDocument) : UpdateFacet = {
    new UpdateFacet(context, id, what, fields) {
      override def apply : HtmlResult = {
        HtmlResult(scrupal.core.echo.html.updateFacet(id, what, fields)(context))
      }
    }
  }

  override def deleteFacet(context: ApplicationContext, id: String, what: List[String]) : DeleteFacet = {
    new DeleteFacet(context, id, what) {
      override def apply : HtmlResult = {
        HtmlResult(scrupal.core.echo.html.deleteFacet(id, what)(context))
      }
    }
  }

  override def queryFacet(context: ApplicationContext, id: String,
                          what: List[String], args: BSONDocument) : QueryFacet = {
    new QueryFacet(context, id, what, args) {
      override def apply : HtmlResult = {
        HtmlResult(scrupal.core.echo.html.queryFacet(id, what, args)(context))
      }
    }
  }
}
