/**********************************************************************************************************************
 * This file is part of Scrupal a Web Application Framework.                                                          *
 *                                                                                                                    *
 * Copyright (c) 2013, Reid Spencer and viritude llc. All Rights Reserved.                                            *
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

package scrupal.controllers

import play.api.mvc.{Action, RequestHeader}
import play.api.{Play, Routes}
import play.api.Play.current
import scrupal.views.html
import scrupal.api.{Module}
import org.joda.time. Duration
import com.typesafe.config.ConfigValue
import scala.collection.immutable.TreeMap

/**
 * A controller to provide the Introduction To Scrupal content
 * Further description here.
 */
object Home extends ScrupalController {

  /** The home page */
	def index = Action { implicit request =>
  // If we are not yet configured (no sites to serve) then every request leads us to /config. Period :)
    if (Global.ScrupalIsConfigured || Global.DataYouShouldNotModify.devMode ) {
      // TODO: Get the home page url that is defined for the current site
      NotImplemented("Site based main page")
    }
    else
    {
      TemporaryRedirect("/config") // Scrupal needs to be configured !
    }
	}

  def configIndex = Action { implicit request => Ok(html.config.index()) }
  def configDatabase = Action { implicit request => Ok(html.config.database()) }
  def configSite = Action { implicit request => Ok(html.config.site()) }
  def configPage = Action { implicit request => Ok(html.config.page()) }
  def configSuccess = Action { implicit request => Ok(html.config.success()) }


  /** The admin application */
  def admin = Action { implicit request =>
    Ok(html.admin(Module.all))
  }

  /** The apidoc application */
  def apidoc = Action { implicit request =>
    Ok(html.apidoc(Module.all))
  }

  def docPage(path: String) = Action { implicit request =>
    Ok(html.docPage(path))
  }

  def jsRoutes(varName: String = "jsRoutes") = Action { implicit request =>
    Ok(
      Routes.javascriptRouter(varName)(
        routes.javascript.Home.index,

        routes.javascript.Assets.js,
        routes.javascript.Assets.css,
        routes.javascript.Assets.misc,
        routes.javascript.Assets.js_s,
        routes.javascript.Assets.css_s,
        routes.javascript.Assets.img,
        routes.javascript.Assets.theme,

        routes.javascript.API.createAll,
        routes.javascript.API.fetchAll,
        routes.javascript.API.updateAll,
        routes.javascript.API.deleteAll,
        routes.javascript.API.summarizeAll,
        routes.javascript.API.optionsOfAll,

        routes.javascript.API.create,
        routes.javascript.API.fetch,
        routes.javascript.API.update,
        routes.javascript.API.delete,
        routes.javascript.API.summarize,
        routes.javascript.API.optionsOf
      )
    ).as(JAVASCRIPT)
  }

  /** Data structure for sectionsTablePage that provides a group of sections each with a name and a triple that
    * provides the section description, column headings, and rows for the table. This allows the content of a page
    * full of tabular data to be generated
    */
  type SectionsTableData = Map[String,(String,List[String],List[List[String]])]

  /** A debugging aid that just prints out a good quantity of what Scrupal knows about its context.
    * This includes information about the operating system, machine, virtual machine, scala, scrupal, modules,
    * settings, modules, etc. It can return content in either HTML or JSON format, depending on the request.
    * @return HTML or JSON showing a pile of information about this Scrupal instance
    */
  def dump = Action { implicit request : RequestHeader =>

    val elide = "^(akka|java|sun|user|awt|os|path|line).*".r
    Play.configuration.toString
    val configuration = Tuple3(
      "The configuration data that Play! is using after all processing by Scrupal.",
      List("Name", "Value"),
      (TreeMap(Play.configuration.entrySet.toSeq:_* ) filter { x => !elide.findPrefixOf(x._1).isDefined }  map {
        case (key: String, value: ConfigValue) => List(key, value.toString)
      } ).toList )

    import java.lang.management.ManagementFactory
    val rmx = ManagementFactory.getRuntimeMXBean
    val mmx = ManagementFactory.getMemoryMXBean
    val runtime = Tuple3(
      "Information about the Java Virtual Machine.",
      List("Name", "Value"), List(
        List("Vm Name", rmx.getVmName),
        List("Vm Vendor", rmx.getVmVendor),
        List("Vm Version", rmx.getVmVersion),
        List("Start Time", dateStr(rmx.getStartTime)),
        List("Up Time",  Duration.millis(rmx.getUptime).toString),
        List("Objects Pending Finalization",  mmx.getObjectPendingFinalizationCount.toString),
        List("Boot Class Path",  rmx.getBootClassPath),
        List("Class Path", rmx.getClassPath),
        List("Input Arguments", rmx.getInputArguments.toString)
      )
    )

    val heap = mmx.getHeapMemoryUsage
    val non_heap = mmx.getNonHeapMemoryUsage

    val memory = Tuple3(
      "Information about heap and non-heap memory utilization.",
      List("Name", "Value"), List(
        List("Heap Initial" , heap.getInit.toString),
        List("Heap Committed" , heap.getCommitted.toString),
        List("Heap Used" , heap.getUsed.toString),
        List("Heap Maximum" ,heap.getMax.toString),
        List("Non-Heap Initial" , non_heap.getInit.toString),
        List("Non-Heap Committed" , non_heap.getCommitted.toString),
        List("Non-Heap Used" , non_heap.getUsed.toString),
        List("Non-Heap Maximum" , non_heap.getMax.toString)
      )
    )

    val tmx = ManagementFactory.getThreadMXBean
    val threads = Tuple3(
      "Information about the virtual machine's threads.",
      List("Name", "Value"), List(
        List("Current Thread Count" , tmx.getThreadCount.toString),
        List("Peak Thread Count" , tmx.getPeakThreadCount.toString),
        List("Daemon Thread Count" , tmx.getDaemonThreadCount.toString),
        List("Total Started Thread Count" , tmx.getTotalStartedThreadCount.toString)
      )
    )

    val osmx = ManagementFactory.getOperatingSystemMXBean()
    val os = Tuple3(
      "Information about the underlying operating system Scrupal is running on.",
      List("Name", "Value"), List(
        List("Name" , osmx.getName),
        List("Arch" , osmx.getArch),
        List("System Version " , osmx.getVersion),
        List("Available Processors" , osmx.getAvailableProcessors.toString),
        List("System Load Average" , osmx.getSystemLoadAverage.toString)
      )
    )

    // TODO: Make use of these MXBeans in further reports
    //val cmx = ManagementFactory.getCompilationMXBean
    //val gcmx = ManagementFactory.getGarbageCollectorMXBeans
    //val clmx = ManagementFactory.getClassLoadingMXBean

    val info : SectionsTableData = Map(
      "Play! Configuration" -> configuration,
      "VM Runtime" -> runtime,
      "VM Memory" ->  memory,
      "VM Threading" ->  threads,
      "OperatingSystem" -> os
    )
    val title ="Scrupal Data Dump"
    val descr = """|The information below was generated by Scrupal to describe the operating fundamentals of the web
                   |sites Scrupal is managing. This is likely only useful to a web site developer. If you are an end
                   |user, please (go here)[@Home.index].""".stripMargin

    Ok(html.sectionsTablePage(title, descr, info))
  }
}
