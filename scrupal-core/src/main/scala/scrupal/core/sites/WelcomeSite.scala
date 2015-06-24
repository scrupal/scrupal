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

package scrupal.core.sites

import akka.http.scaladsl.server.PathMatcher.{Unmatched, Matched}
import akka.http.scaladsl.server.PathMatchers
import akka.http.scaladsl.model.HttpMethods
import org.joda.time.DateTime
import scrupal.api._
import scrupal.core.html.PlainPage
import scrupal.core.impl.{NodeReactorProvider, FunctionalNodeReactorProvider}
import scrupal.core.nodes.{HtmlNode, MarkedDocNode}
import scrupal.utils.Enablement

import scala.util.matching.Regex
import scalatags.Text.all._

case class WelcomeSite(sym : Identifier)(implicit scrpl: Scrupal) extends Site(sym) {
  val name : String = "Welcome To Scrupal"
  val description : String = "The default 'Welcome To Scrupal' site that is built in to Scrupal"
  val modified : Option[DateTime] = Some(DateTime.now)
  val created : Option[DateTime] = Some(new DateTime(2014, 11, 18, 17, 40))
  override val themeName = "cyborg"
  def hostNames : Regex = ".*".r

  object WelcomeSiteRoot extends HtmlNode(
    "Main index page for Welcome To Scrupal Site",
    WelcomeSite.WelcomePageTemplate,
    modified = Some(DateTime.now),
    created = Some(new DateTime(2014, 11, 18, 18, 0))
  )

  object DocPathToDocs extends FunctionalNodeReactorProvider( { request: Request ⇒
    val path = request.path.toString().split("/").toIterable
    MarkedDocNode("doc", "docs", path)
  }) {
    private val pathMatcher = PathMatchers.Slash ~ "doc"
    override def canProvide(request: Request) : Boolean = {
      (request.method == HttpMethods.GET) && (pathMatcher(request.path) != Unmatched)
    }
  }

  object WelcomeSiteProvider extends NodeReactorProvider(WelcomeSiteRoot) {
    val matcher = PathMatchers.Slash ~ PathMatchers.PathEnd
    override def canProvide(request: Request) : Boolean = {
      request.method == HttpMethods.GET && (matcher(request.path) != Unmatched)
    }
  }

  override def delegates : Iterable[Provider] = {
    super.delegates ++ Iterable(
      DocPathToDocs, WelcomeSiteProvider
    )
  }

  val coreModule = scrupal.Modules('Core)
  val echoEntity = coreModule.flatMap { m ⇒ m.entity('Echo) }
  enable(coreModule)
  enable(echoEntity)
  // enable(echoEntity, coreModule)
}

object WelcomeSite {

  object WelcomePageTemplate
    extends PlainPage('WelcomePage, "Welcome To Scrupal!", "An introduction to Scrupal", Seq(
      div(cls := "panel panel-primary",
        div(cls := "panel-heading",
          h1(cls := "panel-title", "Welcome To Scrupal!")
        ),
        div(cls := "panel-body",
          p("""You are seeing this page because Scrupal has not found an enabled site in its database. There could
            |be lots of reasons why that happened but it is likely that this is a new installation. So, you have a
            |variety of choices you can make from here:""".stripMargin),
          ul(
            li("You can read the ", a(href := "/doc/index.md", "Scrupal Documentation")),
            li("You can ", a(href := "/config", em("Configure Scrupal")),
              """. If you've just installed Scrupal, this is what you want. The Scrupal ConfigWizard will walk you
              |through the steps to having your first, minimal, site constructed. It should take less than 2 minutes.
              |""".stripMargin
            ),
            li("You can access the echo entity. Try ", a(href := "/echoes/foo", em("this link")),
              """and others like it to access the echo entity. This is a very  simple entity that is bundled with
              |Scrupal. It serves as a reference for building Scrupal entity objects. All it does is turn each
              |request into an HTML page that displays the request in a readable format. You can use this to learn
              |Scrupal at the code level or you can use it for benchmarking Scrupal's internal machinery without
              |latency introduced by modules, nodes, databases, etc.""".stripMargin
            )
          ),
          p("""There's lots more you can do with Scrupal, of course, but you need to get configured first or solve the
            |reason why this page came up. Some of the potential reasons are:""".stripMargin),
          ul(
            li("Your MongoDB database that scrupal was using got damaged somehow."),
            li("There is a network failure between your Scrupal machine and your MongoDB server machine."),
            li("A coding problem has made the procedure of finding sites yield an empty list")
          )
        )
      )
    ))
}
