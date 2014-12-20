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

package scrupal.api.apps

import org.joda.time.DateTime
import scrupal.api.Html.{ContentsGenerator, TemplateGenerator, Contents}
import scrupal.api._
import scrupal.api.nodes.HtmlNode
import scrupal.api.types.BundleType
import shapeless.HList
import spray.routing.PathMatchers.PathEnd

import scalatags.Text.all._


object AdminApp extends Application('admin) {
  val kind: Symbol = 'Admin
  def description: String = "The Scrupal Administrative Application"
  def name: String = "AdminApp"
  val timestamp = Some(new DateTime(2014,12,5,12,20,6))
  def created: Option[DateTime] = timestamp
  def modified: Option[DateTime] = timestamp

  object StatusBar extends Html.Fragment('AdminStatusBar, "Lists the Sites") ( new ContentsGenerator {
    def apply(context: Context): Contents = {
      Seq(span("Scrupal Administration For ", u(context.siteName), " By ", em(context.user)
      ))
    }
  })

  object Configuration extends Html.Fragment('AdminConfiguration, "Configuration") ( new ContentsGenerator {
    def apply(context: Context) : Contents =
      Seq(div(cls:="well",
        for ( (enablee,enablement) <- context.site.get.getEnablementMap) {
        p(enablee.id.name, " is enabled in ", enablement.map{ e => e.id.name }.mkString(", ") )
        }
      ))
    }
  )

  object Database extends Html.Fragment('AdminDatabase, "Database Configuration") ( new ContentsGenerator {
    def apply(context: Context) : Contents =
      context.withSchema { (dbc, schema) ⇒
        Seq(
          div(cls:="well",p("Database Configuration"))
        )
      }
    }
  )

  object Modules extends Html.Fragment('AdminModules, "Modules Administration") ( new ContentsGenerator {
    def apply(context: Context) : Contents =
      Seq(
        div(cls:="well",
          p("Modules Defined:"),
          ul(
            for (mod ← Module.values) {
              Seq(li(mod.id.name, " - ", mod.description, " - ", mod.moreDetailsURL.toString))
            }
          )
        )
      )
    }
  )

  object Applications extends Html.Fragment('AdminApplications, "Applications Administration") ( new ContentsGenerator{
    def apply(context: Context) : Contents =
      Seq(
        div(cls:="well",
          p("Applications:"),
          ul(
            for (app ← context.site.get.applications) {
              li(app.name, " - ", app.pathsToActions.map { p => p.pm.toString() }.mkString(", "))
            }
          )
        )
      )
    }
  )

  object TemplatePage extends Html.Template('ScrupalAdmin, "Scrupal Administration") ( new TemplateGenerator {
    def apply(context: Context, args: Map[String, Html.Fragment]) : Contents =
      Seq(
        div(cls := "container",
          div(cls := "panel panel-primary",
            div(cls := "panel-heading", h1(cls := "panel-title", StatusBar(context)))),
          div(cls := "panel-body",
            div(role := "tabpanel",
              ul(cls := "nav nav-pills", role := "tablist",
                li(cls := "active", role := "presentation",
                  a(href := "#database", aria.controls := "database", role := "tab", data("toggle") := "pill",
                    "Database")),
                li(role := "presentation",
                  a(href := "#configuration", aria.controls := "configuration", role := "tab",
                    data("toggle") := "pill", "Configuration")),
                li(role := "presentation",
                  a(href := "#modules", aria.controls := "modules", role := "tab",
                    data("toggle") := "pill", "Modules")),
                li(role := "presentation",
                  a(href := "#applications", aria.controls := "applications", role := "tab",
                    data("toggle") := "pill", "Applications"))
              ),
              div(cls := "tab-content",
                div(role := "tabpanel", cls := "tab-pane active", scalatags.Text.all.id := "database", Database(context)),
                div(role := "tabpanel", cls := "tab-pane", scalatags.Text.all.id := "configuration",
                  Configuration(context)),
                div(role := "tabpanel", cls := "tab-pane", scalatags.Text.all.id := "modules", Modules(context)),
                div(role := "tabpanel", cls := "tab-pane", scalatags.Text.all.id := "applications",
                  Applications(context))
              )
            )
          )
        )
      )
    }
  )

  object adminLayout extends HtmlNode(
    description = "Layout for Admin application",
    template = TemplatePage,
    args = Map[String, Html.Fragment](
      "StatusBar" → StatusBar,
      "Configuration" → Configuration,
      "Database" → Database,
      "Modules" → Modules,
      "Applications" → Applications
    )
  )

  override val pathsToActions : Seq[PathMatcherToAction[_ <: HList]] = Seq(
    PathToNodeAction(PathEnd, adminLayout)
  )
}

object SiteAdminEntity extends Entity('SiteAdmin) {
  def kind: Symbol = 'SiteAdmin

  val key: String = "Site"

  def description: String = "An entity that handles administration of Scrupal sites."

  def instanceType: BundleType = BundleType.Empty

  /* FIXME:
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

  override def createFacet(context: Context, what: Seq[String], instance: BSONDocument) : CreateFacet = {
    new CreateFacet(context, what, instance) {
      override def apply : Future[Result[_]] = {
        Future.successful( HtmlResult(scrupal.core.views.html.echo.createFacet(what, instance)(context)) )
      }
    }
  }

  override def retrieveFacet(context: Context, what: Seq[String]) : RetrieveFacet = {
    new RetrieveFacet(context, what) {
      override def apply : Future[Result[_]] = {
        Future.successful( HtmlResult(scrupal.core.views.html.echo.retrieveFacet(what)(context)) )
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
