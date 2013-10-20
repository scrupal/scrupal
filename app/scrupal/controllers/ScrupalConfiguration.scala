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

import java.io.{PrintWriter, File}
import scala.util.{Success, Failure, Try}
import scala.slick.session.Session

import com.typesafe.config.{ConfigRenderOptions, Config, ConfigFactory}

import play.api.mvc.{RequestHeader, Action}
import play.api.{Configuration, Logger}

import scrupal.api.{EssentialSite,Site}
import scrupal.db.{Sketch,CoreSchema}
import scrupal.utils.ConfigHelper
import scrupal.views.html


/** The Entity definition for the Configuration workflow/wizard.
  * This controller handles first-time configuration and subsequent reconfiguration of the essentials of Scrupal. It
  * makes very few assumptions about the running state of Scrupal and has to operate from initial conditions where
  * not even a database is configured.
  * Further description here.
  */
object ScrupalConfiguration extends ScrupalController {

  type SiteMap = Map[Symbol,String]

  object Step extends Enumeration {
    type Kind = Value
    val Zero_Welcome = Value
    val One_Specify_Databases = Value
    val Two_Connect_Databases = Value
    val Three_Install_Schemas = Value
    val Four_Create_Site = Value
    val Five_Create_Page = Value
    val Six_Success = Value

    def numberOfSteps = Step.maxId
    def stepNumber(kind: Kind) = kind.id + 1
    def progress(kind: Kind) = (100 * stepNumber(kind)) / numberOfSteps

    def currentState(kind: Kind) : String = {
      kind match {
        case Zero_Welcome          => "Unconfigured"
        case One_Specify_Databases => "Database(s) Need To Be Defined"
        case Two_Connect_Databases => "Database connection(s) are unverified"
        case Three_Install_Schemas => "Database schemas and configuration needs to be installed"
        case Four_Create_Site      => "The first Site needs to be created"
        case Five_Create_Page      => "The first Page needs to be created"
        case Six_Success           => "Configured"
        case _                     => nextAction(Zero_Welcome)
      }
    }

    def nextAction(kind: Kind) : String = {
      kind match {
        case Zero_Welcome          => "Specify database connection parameters"
        case One_Specify_Databases => "Test database connections"
        case Two_Connect_Databases => "Install database schemas and configuration"
        case Three_Install_Schemas => "Create a site to contain data"
        case Four_Create_Site      => "Create a page to server"
        case Five_Create_Page      => "Show configuration results"
        case Six_Success           => "Start using Scrupal!"
        case _                     => nextAction(Zero_Welcome)
      }
    }

    /** Determine which step we are at based on the Context provided */
    def apply(context: Context) : (Step.Kind,Option[Throwable]) = {
      Try {
        val cfg = context.config
        if (context.site.isDefined || Site.size > 0) {
          // Initial loading found sites so we can assume we've got DB & Schema, skip ahead to step 5 :)
          (Five_Create_Page,None)
        } else if (cfg.getConfig("db").isEmpty) {
          // There isn't even an attempt to configure a databasel, probably a fresh install
          (Zero_Welcome,Some(cfg.reportError("db", "There is no configuration for any databases. This is " +
            "probably because Scrupal is freshly installed.")))
        } else {
          // Something is up with loading the Sites: Config, Database, Schema. Check each
          ConfigHelper(cfg).validateDBs match {
            case Success(siteNames: Set[String]) => {
              if (siteNames.isEmpty) {
                (One_Specify_Databases,Some(cfg.reportError("db",
                  """The database configuration is empty. Probably you erased it on a previous configuration attempt
                    |or the configuration file was manually tampered with.""")))
              } else {
                Logger.info("Got clean database config: " + siteNames )
                // Okay, we validated that we have a clean configuration. So, we can now look at each site and assess
                // if there are URL, connection or schema issues.
                val site_results: Set[(Step.Value,Option[Throwable])] = for ( site <- siteNames ) yield {
                  try
                  {
                    if (site == "default") {
                      (One_Specify_Databases, Some(new Exception("The default database configuration does not permit " +
                        "storage on disk. A real database option must be chosen. ")))
                    } else {
                      context.config.getConfig("db." + site) match {
                        case Some(config) => {
                          val sketch = Sketch(config)
                          sketch.withSession { implicit  session: Session =>
                            val schema = new CoreSchema(sketch)
                            val schema_was_validated = schema.validate
                            schema_was_validated match {
                              case Success(false) => (Three_Install_Schemas, None)
                              case Success(true) => {
                                // FIXME: These two queries use findAll which unloads the contents of the tables into
                                // memory. Why isn't there a COUNT(*) facility available??????
                                if ( schema.Sites.findAll.length > 0) {
                                  if ( schema.Entities.findAll.length > 0) {
                                    (Six_Success, None)
                                  } else {
                                    (Five_Create_Page, Some(new Exception("You have a site defined but there are no " +
                                      "entities created yet so nothing will be served.")))
                                  }
                                } else {
                                  (Four_Create_Site, Some(new Exception("The database is configured correctly but no " +
                                    "sites have been defined yet.")))
                                }
                              }
                              case Failure(x) => (Three_Install_Schemas,Some(x))
                            }
                          }
                        }
                        case None => (One_Specify_Databases, Some(context.config.reportError("db."+site,
                          "No configuration found for this database")))
                      }
                    }
                  }
                  catch { case x : Throwable => (Two_Connect_Databases,Some(x)) }
                }
                // We just collected together a list of the results for each site. now let's find the earliest
                // step amongst them.
                site_results.foldLeft[(ScrupalConfiguration.Step.Value,Option[Throwable])]((Six_Success,None)) {
                  case (step1:(Step.Kind, Option[Throwable]), step2:(Step.Kind, Option[Throwable])) =>
                    if (step1._1 < step2._1) step1 else step2
                }
              }
            }
            case Failure(x)  => (One_Specify_Databases, Some(x))
          }
        }
      } match {
        case Success(x) => x
        case Failure(x) => (One_Specify_Databases, Some(x))
      }
    }
  }

  /** This Configuration action
    * This is a special action that does not have a route. It is invoked from Global.onRouteRequest whenever that
    * code decides that the administrator needs to configure the system. This is generally only true before the first
    * site has been defined. After that normal routing occurs.
    *
    * In deciding what to do, it uses the Config.Step enumeration to determine the step in the configuration that
    * corresponds to the state of affairs of Scrupal's installation.
    * @return One of the Configuration Pages
    */
  def configure() = Action { implicit request : RequestHeader =>
    val (step,error) : (Step.Kind,Option[Throwable]) = ScrupalConfiguration.Step.apply(context)
    import ScrupalConfiguration.Step._
    step match {
      case Zero_Welcome          => Ok(html.config.index(step,error))
      case One_Specify_Databases => Ok(html.config.database(step,error))
      case Two_Connect_Databases => Ok(html.config.connect(step,error))
      case Three_Install_Schemas => Ok(html.config.schema(step,error))
      case Four_Create_Site      => Ok(html.config.site(step,error))
      case Five_Create_Page      => Ok(html.config.page(step,error))
      case Six_Success           => Ok(html.config.success(step,error))
      case _                     => Ok(html.config.index(step,error)) // just in case
    }
  }

  val db_config_file = new File("conf/databases.conf")

  private def setDbConfig(config: Map[String,Any]) : Configuration = {
    val existing_config : Config = if (db_config_file.isFile) {
      ConfigFactory.parseFile(db_config_file)
    } else {
      ConfigFactory.empty()
    }

    import collection.JavaConversions._

    val new_config : Config = ConfigFactory.parseMap(config)
    val merged_config : Config  = new_config.withFallback(existing_config)
    val data: String = merged_config.root.render (ConfigRenderOptions.concise()) // whew!
    val trimmed_data = data.substring(1, data.length-1)
    val writer = new PrintWriter(db_config_file.getCanonicalPath())
    try  { writer.println(trimmed_data) } finally { writer.close }
    Configuration(merged_config)
  }

  private def doShortCutConfiguration() = {
    val default_db_conf = Map(
      "db.default.url" ->  "jdbc:h2:~/scrupal",
      "db.default.driver" -> "org.h2.Driver",
      "db.default.user" -> "",
      "db.default.pass" -> ""
    )
    val new_config = setDbConfig(default_db_conf)
    val default_config = new_config.getConfig("db.default")
    val sketch = Sketch(default_config.get)
    sketch.withSession { implicit session: Session =>
      val schema = new CoreSchema(sketch)
      schema.create(session)
      val site = EssentialSite('default, "Scrupal Default Site", 8000, "localhost", 8000, false, true)
      schema.Sites.insert(site)
      // TODO: Insert the first "Welcome To Scrupal" page entity.
    }
  }

  /** Initial Configuration for Step 1
    * This is empty so as to make the state machine go to step 1 which provides the JDBC configuration
    * @return
    */
  private def doInitialConfiguration() = {
    val empty_conf = Map (
      "db.default.url"    -> "jdbc:h2:mem:scrupal",
      "db.default.driver" -> "org.h2.Driver",
      "db.default.user"   -> "",
      "db.default.pass"   -> ""
    )
    setDbConfig(empty_conf)
  }

  def configAction() = Action { implicit request =>
    val formData : Map[String,Seq[String]] = request.body.asFormUrlEncoded.getOrElse(Map())
    if (formData.contains("step"))
    {
      val steps = formData.get("step").getOrElse(Seq())
      if (steps.size==1) {
        val step = Step.withName(steps(0))
        import ScrupalConfiguration.Step._
        step match {
          case Zero_Welcome          => {
            if (formData.contains("how")) {
              val hows = formData.get("how").get
              if (hows.size==1) {
                hows(0) match {
                  case "shortcut" => doShortCutConfiguration()
                  case "configure" => { doInitialConfiguration() }
                }
              }
            }
          }
          case One_Specify_Databases =>
          case Two_Connect_Databases =>
          case Three_Install_Schemas =>
          case Four_Create_Site      =>
          case Five_Create_Page      =>
          case Six_Success           =>
          case _                     =>  // just in case
        }
      }
    }
    Redirect("/configure")
  }

}
