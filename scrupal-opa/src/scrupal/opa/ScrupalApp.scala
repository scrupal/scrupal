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

package scrupal.opa

import com.greencatsoft.angularjs.core.Scope
import org.scalajs.dom
import org.scalajs.dom.{Element, document}

import scala.scalajs.js
import scala.scalajs.js.JSApp
import scala.scalajs.js.annotation.{JSName, JSExport}
import com.greencatsoft.angularjs.{injectable, inject, Angular}

@injectable("$compile")
trait Bootstrap extends js.Object {
  def apply(element: Element, modules: Seq[String], config: js.Object ): Any = js.native
}

@JSExport
object ScrupalGlobal {

  val ng = Angular.module("ng")

  @JSExport
  def onReady() : Any = {
    println("dom is ready")

    /** We always bootstrap the scrupal module into the document. This gives us a variety of tools that are
      * shared across other Angular modules. For example, the marked.js module is loaded by scrupal and made
      * available with the <marked></marked> elements. This is a fundamental capability we want for all
      * Scrupal pages, so it goes in the scrupal module and therefore does not need to be specially loaded
      * by other modules.
      */
    // ng.bootstrap(document.documentElement, Seq("scrupal"))

    /** In the angularPage.scala.html template we defined the angular/scrupal module that the page wants to
      * use. This basically sets up a require/scrupal/angular module as a one-page-ap. The module is specified
      * as the window variable `scrupal_module_to_load`. So, if we find that value, we load the corresponding
      * module and bootstrap it to the element with the same ID. Easy Peasy. :)
    if ("scrupal_module_to_load" in window) {
      var mod = window.scrupal_module_to_load;
      if (mod !== 'scrupal') {
        require(['/assets/javascripts/' + mod + '/' + mod + '.js'], function() {
          var body_selector = '#' + mod;
          ng.bootstrap( window.document.body.querySelector(body_selector), [mod]);
        });
      }
    }
      */

  }
}



object ScrupalApp extends JSApp {

  val scrupal = Angular.module("scrupal", Seq("ngRoute", "ng.ui"))

  def main(): Unit = {
    // scrupal.controller()
    // scrupal.service()
    installController()
    doit("World")
  }

  def installController() : Unit = {
    val rootNode = document.documentElement
    rootNode.setAttribute("ng-controller", "scrupal")
    rootNode.setAttribute("ng-hide", "true")
    RequireJS.require(js.Array("domReady"), { (domReady: DOMReady) =>
      domReady( ScrupalGlobal.onReady _ )
    })
  }

  @JSExport
  def doit(name: String): Unit = {
    println("Hello world!")
    appendPar(document.body, "Hello, " + name + "!")
  }

  def appendPar(targetNode: dom.Node, text: String): Unit = {
    val parNode = document.createElement("p")
    val textNode = document.createTextNode(text)
    parNode.appendChild(textNode)
    targetNode.appendChild(parNode)
  }

}

