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
import scala.util.{Success, Failure}
import scala.slick.session.Session

import com.typesafe.config.{ConfigRenderOptions, Config, ConfigFactory}

import play.api.mvc.{RequestHeader, Action}
import play.api.{Configuration}
import play.api.data._
import play.api.data.Forms._

import scrupal.api.{EssentialSite}
import scrupal.db.{Sketch,CoreSchema,SupportedDatabases}
import scrupal.utils.ConfigHelper
import scrupal.views.html
import scrupal.models.CoreModule


/** The Entity definition for the Configuration workflow/wizard.
  * This controller handles first-time configuration and subsequent reconfiguration of the essentials of Scrupal. It
  * makes very few assumptions about the running state of Scrupal and has to operate from initial conditions where
  * not even a database is configured.
  * Further description here.
  */
object ConfigWizard extends ScrupalController {

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
  }

  type DBConfig = Map[String,Option[Configuration]]
  val emptyDBConfig = Map.empty[String,Option[Configuration]]

  /** Get the names of the configured database, or error information
    * Reads the database.conf file and validates the configuration information contained therein. If it checks out,
    * returns the list of database names in the third part of the triple. Otherwise,
    * the first to parts of the triple provide the current state and error message to go with it.
    * @return A triple providing information for the next part of configuration
    */
  def getDatabaseNames(config: Configuration) : (Step.Kind, Option[Throwable], DBConfig) = {
    import Step._
    val cfg = Configuration(getDbConfig(config)._1)
    val db_cfg = cfg.getConfig("db")
    if (db_cfg.isEmpty)
      (Zero_Welcome, Some(new Throwable("The database configuration is completely empty.")), emptyDBConfig)
    else if (db_cfg.get.getConfig("default").isDefined)
      (Zero_Welcome, Some(new Throwable("The initial, default database configuration was detected.")), emptyDBConfig)
    else ConfigHelper(cfg).validateDBs match {
      case Failure(x)  => (One_Specify_Databases, Some(x), emptyDBConfig)
      case Success(x)  => (Two_Connect_Databases, None, x)
    }
  }

  def checkConnections(config: Configuration) : (Step.Kind, Option[Throwable], DBConfig) = {
    val (state, err, names) = getDatabaseNames(config)
    if (names.isEmpty)
      (state, err, names)
    else {
      try {
        names.foreach { case (db_name: String, config: Option[Configuration]) =>
          config match {
            case Some(config) => {
              val sketch = Sketch(config)
              sketch.withSession { implicit  session: Session =>
                true // result of the foreach
              }
            }
            case None => throw new Exception("Could not find database configuration for '" + db_name)
          }
        }
      } catch {
        case x: Throwable => (Step.Two_Connect_Databases, Some(x), emptyDBConfig)
      }
      (Step.Three_Install_Schemas, None, names)
    }
  }

  def checkSchemas(fullConfig: Configuration) : (Step.Kind, Option[Throwable], DBConfig) = {
    val (state, err, dbConfigs) = getDatabaseNames(fullConfig)
    if (dbConfigs.isEmpty)
      (state, err, dbConfigs)
    else {
      // Okay, we validated that we have a clean configuration. So, we can now look at each site and assess
      // if there are URL, connection or schema issues.
      val db_results = for ( (db:String, dbConfig: Option[Configuration]) <- dbConfigs ) yield {
        try
        {
          dbConfig match {
            case Some(config) => {
              val sketch = Sketch(config)
              sketch.withSession { implicit  session: Session =>
                val schema = new CoreSchema(sketch)
                val metaTables = schema.getMetaTables
                if (metaTables.isEmpty) {
                  (Step.Three_Install_Schemas, Some(new Exception("Database is empty")), emptyDBConfig)
                } else {
                  schema.validate match {
                    case Success(false) =>
                      (Step.Three_Install_Schemas, Some(new Exception("Schema validation failed.")), emptyDBConfig)
                    case Success(true) => {
                      // FIXME: These two queries use findAll which unloads the contents of the tables into
                      // memory. Why isn't there a COUNT(*) facility available??????
                      if ( schema.Sites.findAll.length > 0) {
                        if ( schema.Instances.findAll.length > 0) {
                          // Finally, at this point, we know everything is working.
                          (Step.Six_Success, None, dbConfigs)
                        } else {
                          (Step.Five_Create_Page, Some(new Exception("You have a site defined but there are no " +
                            "entities created yet so nothing will be served.")), dbConfigs)
                        }
                      } else {
                        (Step.Four_Create_Site, Some(new Exception("The database is configured correctly but no " +
                          "sites have been defined yet.")), dbConfigs)
                      }
                    }
                    case Failure(x) => (Step.Three_Install_Schemas,Some(x), emptyDBConfig)
                  }
                }
              }
            }
            case None => (Step.One_Specify_Databases,
              Some(new Exception("No configuration found for database '" + db + "'.")), emptyDBConfig)
          }
        }
        catch { case x : Throwable => (Step.Two_Connect_Databases,Some(x), emptyDBConfig) }
      }
      // We just collected together a list of the results for each site. now let's find the earliest
      // step amongst them.
      db_results.foldLeft[(Step.Value,Option[Throwable],DBConfig)]((Step.Six_Success,None,emptyDBConfig)) {
        case (step1, step2) => if (step1._1 < step2._1) step1 else step2
      }
    }
  }

  /** Determine which step we are at based on the Context provided */
  def computeState(implicit context: Context) : (Step.Kind,Option[Throwable],DBConfig) = {
    checkSchemas(context.config)
  }

  // The configuration key that says where to get the database configuration data.
  lazy val scrupal_database_config_file = "scrupal.database.config.file"

  def getDbConfigFile(config: Configuration) : Option[File] = {
    config.getString(scrupal_database_config_file) map { db_config_file_name: String =>
      new File(db_config_file_name)
    }
  }

  def getDbConfig(config: Configuration) : (Config,Option[File]) = {
    getDbConfigFile(config) map { db_config_file: File =>
      if (db_config_file.isFile) {
        (ConfigFactory.parseFile(db_config_file),Some(db_config_file))
      } else {
        (ConfigFactory.empty(),None)
      }
    }
  }.getOrElse((ConfigFactory.empty(),None))

  private def setDbConfig(x : (Config, Option[File]), config: Map[String,Any]) : Configuration = {
    import collection.JavaConversions._

    val new_config : Config = ConfigFactory.parseMap(config)
    val merged_config : Config  = new_config.withFallback(x._1)
    val data: String = merged_config.root.render (ConfigRenderOptions.concise()) // whew!
    val trimmed_data = data.substring(1, data.length-1)
    x._2 map { file: File =>
      val writer = new PrintWriter(file)
      try  { writer.println(trimmed_data) } finally { writer.close }
      Configuration(merged_config)
    }
  }.getOrElse(Configuration.empty)

  private def doShortCutConfiguration(config: Configuration) = {
    val default_db_conf = Map(
      "db.scrupal.url" ->  "jdbc:h2:~/scrupal",
      "db.scrupal.driver" -> "org.h2.Driver",
      "db.scrupal.user" -> "",
      "db.scrupal.pass" -> ""
    )
    val new_config = setDbConfig(getDbConfig(config), default_db_conf)
    val default_config = new_config.getConfig("db.scrupal")
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
  private def doInitialConfiguration(config: Configuration) = {
    val initial_conf = Map (
      "db.scrupal.url"    -> "jdbc:h2:~/scrupal",
      "db.scrupal.driver" -> "org.h2.Driver",
      "db.scrupal.user"   -> "",
      "db.scrupal.pass"   -> ""
    )
    setDbConfig((ConfigFactory.empty(), getDbConfigFile(config)), initial_conf)
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
    if (CoreModule.ConfigWizard.isEnabled) {
      val (step,error,dbs) : (Step.Kind,Option[Throwable],DBConfig) = computeState(context)
      import ConfigWizard.Step._
      step match {
        case Zero_Welcome          => Ok(html.config.index(step,error))
        case One_Specify_Databases => Ok(html.config.database(makeDatabasesForm(dbs), step, error))
        case Two_Connect_Databases => Ok(html.config.connect(step,error))
        case Three_Install_Schemas => Ok(html.config.schema(step,error))
        case Four_Create_Site      => Ok(html.config.site(step,error))
        case Five_Create_Page      => Ok(html.config.page(step,error))
        case Six_Success           => Ok(html.config.success(step,error))
        case _                     => Ok(html.config.index(step,error)) // just in case
      }
    } else {
      Redirect(routes.Home.index)
    }
  }

  /** Take Conifguration Action
    * This is the POST method handler for the configuration wizard. It is invoked when the user provides some input
    * to the configuration process. This is where the work gets done.
    * @return An Action
    */
  def configAction() = Action { implicit request =>
    if (CoreModule.ConfigWizard.isEnabled) {
      // First, figure out where we are, step wise, by computing the state.
      val (step,error,dbs) : (Step.Kind,Option[Throwable],DBConfig) = computeState(context)
      import ConfigWizard.Step._
      step match {
        case Zero_Welcome          => {
          val formData : Map[String,Seq[String]] = request.body.asFormUrlEncoded.getOrElse(Map())
          if (formData.contains("how")) {
            val hows = formData.get("how").get
            if ( hows.size == 1 ) {
              hows(0) match {
                case "shortcut" => doShortCutConfiguration(context.config)
                case "configure" => doInitialConfiguration(context.config)
                case _ => {}
              }
            }
          }
          // No matter what, the next action is to go to the configure page and let it figure out the new state.
          // This ensures that they get the config wizard page that matches their state AFTER the change made here
          Redirect(routes.ConfigWizard.configure)
        }

        // They are posting database configuration data.
        case One_Specify_Databases => {
          databasesForm.bindFromRequest.fold(
            formWithErrors => { // binding failure, send bad request
              BadRequest(html.config.database(formWithErrors, step, error))
            },
            dbData => { //binding success, build a new configuration file
              Redirect(routes.ConfigWizard.configure)
            }
          )
        }

        // They are testing database configuration
        case Two_Connect_Databases => {

          // No matter what, the next action is to go to the configure page and let it figure out the new state.
          // This ensures that they get the config wizard page that matches their state AFTER the change made here
          Redirect(routes.ConfigWizard.configure)
        }
        case Three_Install_Schemas => {

          // No matter what, the next action is to go to the configure page and let it figure out the new state.
          // This ensures that they get the config wizard page that matches their state AFTER the change made here
          Redirect(routes.ConfigWizard.configure)
        }
        case Four_Create_Site      => {

          // No matter what, the next action is to go to the configure page and let it figure out the new state.
          // This ensures that they get the config wizard page that matches their state AFTER the change made here
          Redirect(routes.ConfigWizard.configure)
        }
        case Five_Create_Page      => {

          // No matter what, the next action is to go to the configure page and let it figure out the new state.
          // This ensures that they get the config wizard page that matches their state AFTER the change made here
          Redirect(routes.ConfigWizard.configure)
        }
        case Six_Success           => {

          // No matter what, the next action is to go to the configure page and let it figure out the new state.
          // This ensures that they get the config wizard page that matches their state AFTER the change made here
          Redirect(routes.ConfigWizard.configure)
        }
        case _                     =>  {
          // No matter what, the next action is to go to the configure page and let it figure out the new state.
          // This ensures that they get the config wizard page that matches their state AFTER the change made here
          Redirect(routes.ConfigWizard.configure)
        }
      }
    } else {
      Redirect(routes.Home.index)
    }
  }

  def reconfigure() = Action { implicit request =>
    if (CoreModule.ConfigWizard.isEnabled) {
      doInitialConfiguration(context.config)
      val (step,error,dbs) : (Step.Kind,Option[Throwable],DBConfig) = computeState(context)
      Ok(html.config.index(step,error))
    } else {
      Redirect(routes.Home.index)
    }
  }

  /** The type of the form passed between here and the databases configuration page */
  type DatabasesForm = Form[DatabasesInfo]

  /** Utility to turn the database configuration data into the DatabasesForm object */
  def makeDatabasesForm(cfgs: DBConfig) : DatabasesForm = {
    val dbInfos : Seq[DatabaseInfo] = { for ( (db: String, config: Option[Configuration]) <- cfgs ) yield {
      val cfg : Configuration = config.getOrElse(Configuration.empty)
      DatabaseInfo(
        db,
        SupportedDatabases.withName(cfg.getString("kind").getOrElse("H2")),
        cfg.getString("host").getOrElse(""),
        cfg.getInt("port").getOrElse(80),
        cfg.getString("user").getOrElse(""),
        cfg.getString("pass").getOrElse("")
      )
    }}.toSeq
    databasesForm.fill(DatabasesInfo(dbInfos))
  }

  /** Information for one database */
  case class DatabaseInfo(name: String, kind: SupportedDatabases.Kind, host: String, port: Int, user: String,
                          pass: String )

  /** Information for all the databases */
  case class DatabasesInfo(configs: Seq[DatabaseInfo])

  import scrupal.utils.Enumerations.enum
  /** The Play! Framework Form to perform the mapping to/from DatabasesInfo */
  val databasesForm : DatabasesForm = Form[DatabasesInfo] (
    mapping (
      "dbs" -> seq (
        mapping (
          "name" -> nonEmptyText,
          "kind" -> enum(SupportedDatabases),
          "host" -> nonEmptyText,
          "port" -> number(min=1, max=65535),
          "user" -> text(minLength=0, maxLength=255),
          "pass" -> text(minLength=0, maxLength=255)
        )(DatabaseInfo.apply)(DatabaseInfo.unapply)
      )
    )(DatabasesInfo.apply)(DatabasesInfo.unapply)
  )

}
