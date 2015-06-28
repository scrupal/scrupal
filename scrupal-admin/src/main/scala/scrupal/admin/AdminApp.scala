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

package scrupal.admin

import org.joda.time.DateTime
import scrupal.api.Html.{Contents, ContentsArgs}
import scrupal.api._
import scrupal.core.html.BootstrapPage
import scrupal.utils.OSSLicense

import scalatags.Text.all._

class AdminApp(implicit scrupal: Scrupal) extends Application('admin) {
  val kind : Symbol = 'Admin
  val author = "Reactific Software LLC"
  val copyright = "© 2013-2015 Reactific Software LLC. All Rights Reserved."
  val license = OSSLicense.ApacheV2

  def description : String = "The Scrupal Administrative Application"

  def name : String = "AdminApp"

  val timestamp = Some(new DateTime(2014, 12, 5, 12, 20, 6))

  def created : Option[DateTime] = timestamp

  def modified : Option[DateTime] = timestamp

  /* FIXME: Reinstate body of AdminApp

object StatusBar extends Html.Template('AdminStatusBar) {
  val description = "Lists the Sites"

  class SiteSelectionForm extends SimpleForm('SiteSelectionForm, "SiteSelection",
    "A form for selecting the site to administrate", "/admin/siteselectionform",
    Seq(
      SelectionFormField("Site: ", "Select a site to administrate", Site_t, inline = true)
    )
  )

  lazy val siteSelectionForm = new SiteSelectionForm

  def apply(context : Context, args : ContentsArgs = EmptyContentsArgs) : Contents = {
    Seq(siteSelectionForm.render)
  }
}

StatusBar.siteSelectionForm.enable(this)

object SiteConfig extends Html.Template('AdminSite) {
  val description = "Configuration"
  def apply(context : Context, args : ContentsArgs = EmptyContentsArgs) : Contents = {
    Seq(div(cls := "well",
      for ((enablee, enablement) ← context.site.get.getEnablementMap) {
        p(enablee.id.name, " is enabled in ", enablement.map { e ⇒ e.id.name }.mkString(", "))
      }
    ))
  }
}

class DBForm extends SimpleForm('database_form, "Database Form", "Description", "/admin/database_form", Seq(
  TextFormField("Host:", "The hostname where your MongoDB server is running",
    DomainName_t, BSONString("localhost"), optional = true, inline = true, attrs = Seq(placeholder := "localhost")),
  IntegerFormField("Port:", "The port number at which your MongoDB server is running",
    TcpPort_t, BSONLong(5253), optional = true, inline = true, attrs = Seq(placeholder := "5253")),
  TextFormField("Name:", "The name of the database you want to connect to",
    Identifier_t, BSONString("scrupal"), optional = true, inline = true, attrs = Seq(placeholder := "scrupal")),
  TextFormField("User:", "The user name for the MongoDB server authentication",
    Identifier_t, BSONString("admin"), optional = true, inline = true, attrs = Seq(placeholder := "admin")),
  PasswordFormField("Password:", "The password for the MongoDB server authentication", Password_t, inline = true),
  SubmitFormField("", "Submit database configuration to Scrupal server.", "Configure Database")
)) {
  override def provideAcceptFormAction(matchingSegment : String, context : Context) : Option[AcceptFormAction] = {
    Some(new DataBaseFormAcceptance(this, context))
  }
}


case class DataBaseFormAcceptance(override val form : Form, override val context : Context)
  extends AcceptFormAction(form, context) {
  override def handleValidatedFormData(doc : BSONDocument) : Result[_] = {
    super.handleValidatedFormData(doc)
  }

  override def handleValidationFailure(errors : ValidationFailed[BSONValue]) : Result[_] = {
    val node = adminLayout(formWithErrors(errors))
    val contents = node.results(context)
    HtmlResult(contents, Successful)
  }
}

object Database extends Html.Template('AdminDatabase) {
  val description = "Database Configuration"
  def apply(context : Context, args : ContentsArgs = EmptyContentsArgs) : Contents = {
    context.withSchema { (dbc, schema) ⇒
      Seq(
        div(cls := "well", tag("DatabaseForm", context, args))
      )
    }
  }
}

object Modules extends Html.Template('AdminModules) {
  val description = "Modules Administration"
  def apply(context : Context, args : ContentsArgs = EmptyContentsArgs) : Contents = {
    Seq(
      div(cls := "well",
        p("Modules Defined:"),
        ul(
          for (mod ← Module.values) yield {
            li(mod.id.name, " - ", mod.description, " - ", mod.moreDetailsURL.toString)
          }
        )
      )
    )
  }
}

object Applications extends Html.Template('AdminApplications) {
  val description = "Applications Administration"
  def apply(context : Context, args : ContentsArgs = EmptyContentsArgs) : Contents = {
    Seq(
      div(cls := "well",
        p("Applications:"),
        ul(
          for (app ← context.site.get.applications) yield {
            li(app.name, " - ", app.delegates.map { p ⇒ p.describe }.mkString(", ")) // FIXME: describe?
          }
        )
      )
    )
  }
}
*/
  object AdminPage extends BootstrapPage('AdminPage, "Scrupal Admin", "Scrupal Administration") {
    override def body_content(context : Context, args : ContentsArgs) : Contents = {
      Seq(
        div(cls := "container",
          div(cls := "panel panel-primary",
            div(cls := "panel-heading", h1(cls := "panel-title", tag("StatusBar", context, args)))),
          div(cls := "panel-body",
            div(role := "tabpanel",
              ul(cls := "nav nav-pills", role := "tablist", scalatags.Text.attrs.id := "AdminTab",
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
                div(role := "tabpanel", cls := "tab-pane active", scalatags.Text.all.id := "database",
                  tag("Database", context, args)),
                div(role := "tabpanel", cls := "tab-pane", scalatags.Text.all.id := "configuration",
                  tag("Configuration", context, args)),
                div(role := "tabpanel", cls := "tab-pane", scalatags.Text.all.id := "modules",
                  tag("Modules", context, args)),
                div(role := "tabpanel", cls := "tab-pane", scalatags.Text.all.id := "applications",
                  tag("Applications", context, args))
              )
            )
          )
        )
      )
    }
  }

/* FIXME: Reinstate DBFORM in AdminApp
  def adminLayout(dbForm : Form) = {
    new HtmlNode(
      description = "Layout for Admin application",
      template = AdminPage
    ) {
      override def args : Map[String, Html.Generator] = Map(
        "StatusBar" → StatusBar,
        "Configuration" → SiteConfig,
        "DatabaseForm" → dbForm,
        "Database" → Database,
        "Modules" → Modules,
        "Applications" → Applications
      )
    }
  }

  override def delegates : Seq[Provider] = {
    def dbForm = new DBForm
    dbForm.enable(this)
    super.delegates ++ Seq(
      NodeReactorProvider(PathEnd, adminLayout(dbForm))
    )
  }
  */
}

class SiteAdminEntity(implicit scrupal: Scrupal) extends Entity('SiteAdmin) {
  def kind : Symbol = 'SiteAdmin

  def description : String = "An entity that handles administration of Scrupal sites."

  val author = "Reactific Software LLC"
  val copyright = "© 2013-2015 Reactific Software LLC. All Rights Reserved."
  val license = OSSLicense.ApacheV2

  def instanceType : BundleType = BundleType.empty

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
