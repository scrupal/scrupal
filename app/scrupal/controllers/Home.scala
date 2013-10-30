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

import play.api.mvc.{Request, AnyContent, Action, RequestHeader}
import play.api.{Mode, Play, Routes}
import play.api.Play.current
import scrupal.views.html
import scrupal.api.{InstanceIdentifier, Instance, Site, Module}
import org.joda.time.Duration
import com.typesafe.config.ConfigValue
import scala.collection.immutable.TreeMap
import java.io.File
import scrupal.db.CoreSchema
import play.api.libs.json.JsString

/**
 * A controller to provide the Introduction To Scrupal content
 * Further description here.
 */
object Home extends ScrupalController {

  /** The home page */
	def index = ContextualAction {
    implicit context: AnyContext =>  {
      context.site map {
        site: Site => {
          site.siteIndex map {
            sid: InstanceIdentifier => {
              site.withCoreSchema { schema: CoreSchema =>
                schema.Instances.fetch(sid) map { instance: Instance =>
                  require(instance.entityId == 'Page)
                  val body = (instance.payload \ "body").asInstanceOf[JsString].value
                  Ok(html.page(instance.name.name, instance.description)(body))
                }
              } getOrElse {
                NotFound( "the page entity with index #" + sid, Seq(
                  "you haven't completed initial configuration,",
                  "you deleted your Site (" + site.id.name + ") index," )
                )
              }
            }
          }
        } getOrElse {
          NotFound("the index for site '" + site.id.name + "'")
        }
      }
    }.getOrElse {
      NotFound("Any site with an index",
        suggestions=Seq("make sure Scrupal has an enabled Site with a configured index p"))
    }
  }

  /*
  def instanceById(kind:String, id: Long) = Action {
    implicit request: Request[AnyContent] => {
      context.site map {
        site : Site => {

        }
      }
    }

  }

  def instanceByName(kind: String, id:Long) = Action {

  }

    */


  /** The admin application */
  def admin = ContextualAction { implicit context: AnyContext =>
    Ok(html.admin(Module.all))
  }

  /** The apidoc application */
  def apidoc = ContextualAction { implicit context: AnyContext =>
    Ok(html.apidoc(Module.all))
  }

  lazy val indexAliases = "(^$)|^/(index|top|home)?$".r

  private def gracefulIndex(pathAsRequested: String, index: String) = {
    pathAsRequested match {
      case s if indexAliases.findFirstMatchIn(s).isDefined  => index
      case _    => pathAsRequested
    }
  }

  /** Serve the Scrupal documentation pages
    *
    * @param path Relative path to the page requested
    * @return
    */
  def docPage(path: String) = ContextualAction { implicit context: AnyContext =>
    val path_to_serve = gracefulIndex(path, "index.html")
    Assets.isValidDocAsset(path_to_serve) match {
      case true => Ok(html.docPage(path_to_serve))
      case false => NotFound("Scrupal Documentation", suggestions = Seq(
        "Browse the documentation only with the links provided from the documentation pages.",
        "Review `" + path + "` to see if it contains an error."
      ))
    }
  }

  /** Serve the generated documentation files.
    * In Dev mode these are in `target/scala-2.10/api/` but in production they are in the `share/doc/api` directory.
    * Just match the difference and serve.
    * @param path Path to the requested documentation asset
    * @return The asset
    */
  def scalaDoc(path: String) = ContextualAction { implicit context : AnyContext =>
    def serveDocFile(rootPath: String, file: String) = {
      val fileToServe = new File(rootPath, file)
      if (fileToServe.exists) {
        Ok.sendFile(fileToServe, inline = true).withHeaders(CACHE_CONTROL -> "max-age=3600")
      } else  {
        NotFound("Scala Generated Documentation", Seq(
          "used an old link from a previous version of the software that is no longer relevant?",
          if (Play.current.mode != Mode.Prod) "forgot to build the scaladoc with the `play doc` command?"
          else "moved the scaladoc from the share/doc/api directory?")
        )
      }
    }

    // Deal with recalictrant paths :)
    val path_to_serve = gracefulIndex(path,"index.html")

    Play.current.mode match {
      case Mode.Dev => serveDocFile("target/scala-2.10/api", path_to_serve)
      case _ => serveDocFile("share/doc/api", path_to_serve)
    }
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
        routes.javascript.API.optionsOfAll,

        routes.javascript.API.create,
        routes.javascript.API.fetch,
        routes.javascript.API.update,
        routes.javascript.API.delete,
        routes.javascript.API.optionsOf,

        routes.javascript.API.get,
        routes.javascript.API.put
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
  def dump = ContextualAction { implicit context : AnyContext =>

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
