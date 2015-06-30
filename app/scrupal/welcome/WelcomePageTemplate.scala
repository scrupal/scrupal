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

package scrupal.welcome

import scrupal.api.html.PlainPage

import scalatags.Text.all._

import scrupal.utils.ScrupalUtilsInfo

object WelcomePageTemplate
  extends PlainPage('WelcomePage, s"Welcome To Scrupal ${ScrupalUtilsInfo.version}!", "An introduction to Scrupal", Seq(
    div(cls := "panel panel-primary",
      div(cls := "panel-heading",
        h1(cls := "panel-title", s"Welcome To Scrupal ${ScrupalUtilsInfo.version}!")
      ),
      div(cls := "panel-body",
        p( """You are seeing this page because Scrupal has not found an enabled site in its database. There could
             |be lots of reasons why that happened but it is likely that this is a new installation. So, you have a
             |variety of choices you can make from here:""".stripMargin),
        ul(
          li("You can read the ", a(href := "/doc/index.md", "Scrupal Documentation")),
          li("You can ", a(href := "/config", em("Configure Scrupal")),
            """. If you've just installed Scrupal, this is what you want. The Scrupal ConfigWizard will walk you
              |through the steps to having your first, minimal, site constructed. It should take less than 2 minutes.
              | """.stripMargin
          ),
          li("You can access the echo entity. Try ", a(href := "/echoes/foo", em("this link")),
            """and others like it to access the echo entity. This is a very  simple entity that is bundled with
              |Scrupal. It serves as a reference for building Scrupal entity objects. All it does is turn each
              |request into an HTML page that displays the request in a readable format. You can use this to learn
              |Scrupal at the code level or you can use it for benchmarking Scrupal's internal machinery without
              |latency introduced by modules, nodes, databases, etc.""".stripMargin
          )
        ),
        p( """There's lots more you can do with Scrupal, of course, but you need to get configured first or solve the
             |reason why this page came up. Some of the potential reasons are:""".stripMargin),
        ul(
          li("Your MongoDB database that scrupal was using got damaged somehow."),
          li("There is a network failure between your Scrupal machine and your MongoDB server machine."),
          li("A coding problem has made the procedure of finding sites yield an empty list")
        )
      )
    )
  )
  )
