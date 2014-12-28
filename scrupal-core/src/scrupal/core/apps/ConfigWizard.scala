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

package scrupal.core.apps

import reactivemongo.bson.{BSONLong, BSONInteger, BSONString}
import scrupal.core._
import scrupal.core.api.Forms._
import scrupal.core.types._
import scrupal.utils.ScrupalComponent


/** The Entity definition for the Configuration workflow/wizard.
  * This controller handles first-time configuration and subsequent reconfiguration of the essentials of Scrupal. It
  * makes very few assumptions about the running state of Scrupal and has to operate from initial conditions where
  * not even a database is configured.
  * Further description here.
  */
object ConfigWizard extends ScrupalComponent {

  val cw = CoreModule.ConfigWizard

  type SiteMap = Map[Symbol,String]

  object Step extends Enumeration {
    type Kind = Value
    val Zero_Welcome = Value(0)
    val One_Specify_Databases = Value(1)
    val Two_Connect_Databases = Value(2)
    val Three_Install_Schemas = Value(3)
    val Four_Create_Site = Value(4)
    val Five_Create_Page = Value(5)
    val Six_Success = Value(6)

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

  /* FIXME: Commented out for now until we can get forms, actions, handlers, controllers, etcl all working in scrupal-http
  type DBConfig = ConfigHelper#DBConfig
  val emptyDBConfig = Map.empty[String,Option[Configuration]]

  import Step._

  /** Get the names of the configured database, or error information
    * Reads the database.conf file and validates the configuration information contained therein. If it checks out,
    * returns the list of database names in the third part of the triple. Otherwise,
    * the first to parts of the triple provide the current state and error message to go with it.
    * @return A triple providing information for the next part of configuration
    */
  def getDatabaseNames(config: Configuration) : (Step.Kind, Option[Throwable], DBConfig) = {
    ConfigHelper(config).validateDBConfiguration match {
      case Failure(x)  => {
        log.debug("Configuration failed DB validation: ", x)
        x.getMessage match {
          case s if s.contains("default") || s.contains("completely empty") || s.contains("not contain") =>
            (Zero_Welcome, Some(x), emptyDBConfig)
          case _ => (One_Specify_Databases, Some(x), emptyDBConfig)
        }
      }
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
            case Some(cfg) => {
              val context = DBContext.fromSpecificConfig(Symbol(db_name), cfg)
              context.withDatabase { implicit db =>
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
              val context = DBContext.fromSpecificConfig(Symbol(db), config)
              val schema = new CoreSchema(context)
              schema.validate match {
                case Success(false) =>
                  (Step.Three_Install_Schemas, Some(new Exception("Schema validation failed.")), dbConfigs)
                case Success(true) => {
                  val numSites = schema.sites.countSync()
                  if (numSites > 0) {
                    val numInstances = schema.instances.countSync()
                    if ( numInstances > 0) {
                      // Finally, at this point, we know everything is working.
                      (Step.Six_Success, None, dbConfigs)
                    } else {
                      (Step.Five_Create_Page, Some(new Exception("You have a site defined but there are no " +
                        "entity instances created yet so nothing will be served.")), dbConfigs)
                    }
                  } else {
                    (Step.Four_Create_Site, Some(new Exception("The database is configured correctly but no " +
                      "sites have been defined yet.")), dbConfigs)
                  }
                }
                case Failure(x) => (Step.Three_Install_Schemas,Some(x), dbConfigs)
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

  def createSchemas(fullConfig: Configuration) : Try[Boolean] = Try[Boolean] {
    val (state, err, dbConfigs) = checkSchemas(fullConfig)
    require(state == Step.Three_Install_Schemas)
    if (dbConfigs.nonEmpty) {
      val (db:String, dbConfig: Option[Configuration]) = dbConfigs.head
      dbConfig match {
        case Some(config) => {
          implicit val context = DBContext.fromSpecificConfig(Symbol(db), config)
          // FIXME: Process the results of Schema.create
          CoreModule.schemas(context).foreach { schema:Schema => schema.create }
          true
        }
        case None => throw new Exception("Cannot create schemas, Database configuration is invalid.")
      }
    } else throw new Exception("Cannot create schemas, database configuration is empty.")
  }

  def createSite(fullConfig: Configuration, siteData: SiteInfo) = Try[Boolean] {
    val (state, err, dbConfigs) = checkSchemas(fullConfig)
    require(state == Step.Four_Create_Site)
    if (dbConfigs.nonEmpty) {
      val (db:String, dbConfig: Option[Configuration]) = dbConfigs.head
      dbConfig match {
        case Some(config) => {
          val context = DBContext.fromSpecificConfig(Symbol(db), config)
          val schema = new CoreSchema(context)
          val sd = Site(Symbol(siteData.name), Symbol(siteData.name), siteData.description, siteData.host, None, siteData.requiresHttps, true, None, None)
          schema.sites.insert( sd )
          true
        }
        case None => throw new Exception("Cannot create site, database configuration is invalid.")
      }
    } else {
      throw new Exception("Cannot create site, database configuration is empty.")
    }
  }

  def createPage(fullConfig: Configuration, pageInfo: PageInfo) = Try {
    val (state, err, dbConfigs) = checkSchemas(fullConfig)
    require(state == Step.Five_Create_Page)
    if (!dbConfigs.isEmpty) {
      val (db:String, dbConfig: Option[Configuration]) = dbConfigs.head
      dbConfig match {
        case Some(config) => {
          val context = DBContext.fromSpecificConfig(Symbol(db), config)
          val schema = new CoreSchema(context)
          val id = Symbol(pageInfo.name)
          val instance = Instance(id, id.name, pageInfo.description, 'Page, BSONDocument( "body" -> BSONString(pageInfo.body) ) )
          val inserted = schema.instances.insert( instance )
          val site = schema.sites.fetchAllSync().head
          val update = BSONDocument( "$set" -> BSONDocument( "siteIndex" -> BSONString(instance._id.name) ))
          schema.sites.update(site, update)
          log.debug("Inserted instance with id=" + inserted)
          true
        }
        case None => throw new Exception("Cannot create page, database configuration is invalid.")
      }
    } else throw new Exception("Cannot create page, database configuration is empty.")
  }

  /** Determine which step we are at based on the Context provided */
  def computeState(implicit context: Context) : (Step.Kind,Option[Throwable],DBConfig) = {
    checkSchemas(context.config)
  }

  // The configuration key that says where to get the database configuration data.
  lazy val scrupal_database_config_file = "scrupal.database.config.file"

  private def doShortCutConfiguration(config: Configuration) = {
    val default_db_conf = Map(
      "db.scrupal.uri" ->  ("mongodb://localhost:27017/scrupal_shortcut_" + System.currentTimeMillis())
    )

    val new_config = ConfigHelper(config).setDbConfig(default_db_conf)
    val context = DBContext.fromConfiguration()
    val schema = new CoreSchema(context)
    schema.validate match {
      case Success(true) => // nothing to do
      case Success(false) => Module.installSchemas(context)
      case Failure(x) => log.error("Failed to validate schema because: ", x); throw x
    }

    val instance = Instance('YourPage, "YourPage", "Auto-generated page created by Short-cut Configuration", 'Page,
      BSONDocument(
        "body" ->
          """# Welcome to Scrupal.
            |This page was created for you because you chose the Short-cut configuration. Feel free to modify it at
            |any time. If you need to learn more about Scrupal, please read the [Documentation](/doc) that comes
            |with it. We hope you enjoy using Scrupal as much as we enjoyed developing it for you.
            |
            |+ [User Documentation](/doc)
            |+ [Developer Documentation](/scaladoc)
            |+ [API Documentation](/apidoc)
            |+ [Scrupal Project](http://scrupal.org/)
            |+ [Scrupal On GitHub](https://github.com/scrupal/scrupal)
            |
          """.stripMargin
      )
    )
    val instance_id = schema.instances.insert(instance)
    val site = Site( Symbol("YourSite"), Symbol("YourSite"), "Auto-generated site created by Short-cut Configuration",
                     "localhost", Some(instance._id), false, true, None, None)
    schema.sites.insert(site)
    Scrupal.reload( config )
  }

  private def doInitialConfiguration(config: Configuration) = Try[Boolean] {
    val default_db_conf = Map(
      "db.scrupal.uri" ->  ""
    )
    ConfigHelper(config).setDbConfig(default_db_conf)
    Scrupal.reload( config )
    true
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
  def configure() = BasicAction { implicit context : AnyBasicContext => WithFeature(cw) {
      val (step,error,dbs) : (Step.Kind,Option[Throwable],DBConfig) = computeState(context)
      step match {
        case Zero_Welcome          => Ok(html.config.index(step,error))
        case One_Specify_Databases => Ok(html.config.database(makeDatabasesForm(dbs), step, error))
        case Two_Connect_Databases => Ok(html.config.connect(step,error,extractDatabaseUrl(dbs)))
        case Three_Install_Schemas => Ok(html.config.schema(step,error))
        case Four_Create_Site      => Ok(html.config.site(makeSiteForm,step,error))
        case Five_Create_Page      => Ok(html.config.page(makePageForm,step,error))
        case Six_Success           => Global.reload(context.config); Ok(html.config.success(step,error))
        case _                     => Ok(html.config.index(step,error)) // just in case
      }
    }
  }

  private def getFormAction(name:String)(f: (String) => Result )(implicit request: Request[AnyContent] ) : Result = {
    val formData : Map[String,Seq[String]] = request.body.asFormUrlEncoded.getOrElse(Map())
    if (formData.contains(name)) {
      val hows = formData.get(name).get
      if ( hows.size == 1 ) {
        f(hows(0))
      } else {
        Redirect( routes.ConfigWizard.configure )
      }
    } else Redirect( routes.ConfigWizard.configure )
  }

  /** Take Conifguration Action
    * This is the POST method handler for the configuration wizard. It is invoked when the user provides some input
    * to the configuration process. This is where the work gets done.
    * @return An Action
    */
  def configAction() = BasicAction { implicit context: AnyBasicContext => WithFeature(cw) {
      // First, figure out where we are, step wise, by computing the state.
      val (step,error,dbs) : (Step.Kind,Option[Throwable],DBConfig) = computeState(context)
      step match {
        case Zero_Welcome          => {
          getFormAction("how") {
            how: String => how match {
              case "shortcut"  => doShortCutConfiguration( context.config )
              case "configure" => doInitialConfiguration( context.config )
              case _  => {}
            }
            Redirect( routes.ConfigWizard.configure )
          }
        }

        // They are posting database configuration data.
        case One_Specify_Databases => {
          databaseForm.bindFromRequest.fold(
            formWithErrors => { // binding failure, send bad request
              BadRequest(html.config.database(formWithErrors, step, error))
            },
            dbData => { //binding success, build a new configuration file
              log.debug("DB Info Request: " + context.body.asFormUrlEncoded)
              log.debug("Converted to DBDATA: " + dbData)
              val dbConfig = makeConfiguration(dbData)
              ConfigHelper(context.config).setDbConfig(Configuration(dbConfig))
              Redirect(routes.ConfigWizard.configure)
            }
          )
        }

        // They are testing database configuration
        case Two_Connect_Databases => {
          getFormAction("how") {
            how: String => how match {
              case "configure" => {
                doInitialConfiguration( context.config )
                Redirect(routes.ConfigWizard.configure)
              }
              case _ => Redirect(routes.ConfigWizard.configure)
            }
          }
        }
        case Three_Install_Schemas => {
          getFormAction("action") {
            case "proceed" => {
              createSchemas(context.config)
              Redirect(routes.ConfigWizard.configure)
            }
            case _ => Redirect(routes.ConfigWizard.configure)
          }
        }
        case Four_Create_Site      => {
          siteForm.bindFromRequest.fold(
            formWithErrors => { BadRequest(html.config.site(formWithErrors, step, error)) },
            siteData => {
              createSite(context.config, siteData)
              Redirect(routes.ConfigWizard.configure)
            }
          )
        }
        case Five_Create_Page      => {
          pageForm.bindFromRequest.fold(
            formWithErrors => { BadRequest(html.config.page(formWithErrors, step, error)) },
            pageData => {
              createPage(context.config, pageData)
            }
          )

          Redirect(routes.ConfigWizard.configure)
        }
        case Six_Success           => {

          // NOTE: Set up the NEXT request to not configure any more. We tell Global to reload the sites. This
          // NOTE: makes the list of sites available and consequently the configuration is complete.
          Global.reload(context.config)

          // No matter what, the next action is to go to the configure page and let it figure out the new state.
          // This ensures that they get the config wizard page that matches their state AFTER the change made here
          Redirect(routes.Home.index)
        }
        case _                     =>  {
          // No matter what, the next action is to go to the configure page and let it figure out the new state.
          // This ensures that they get the config wizard page that matches their state AFTER the change made here
          Redirect(routes.ConfigWizard.configure)
        }
      }
    }
  }

  def reconfigure() = BasicAction { implicit context : AnyBasicContext => WithFeature(cw) {
      val ctxt = context
      // Just wipe out the initial configuration to get to step 0
      ConfigHelper(ctxt.config).getDbConfigFile map { file: File =>
        if (file.exists)
          file.delete()
        Global.unload(ctxt.config)
      }
      val (step,error,dbs) : (Step.Kind,Option[Throwable],DBConfig) = computeState(ctxt)
      Ok(html.config.index(step,error))
    }
  }

  /** The type of the form passed between here and the databases configuration page */
  type DatabaseForm = Form[DatabaseInfo]

  /** Utility to turn the database configuration data into the DatabasesForm object */
  def makeDatabasesForm(cfgs: DBConfig) : DatabaseForm = {
    val dbInfos : Seq[DatabaseInfo] = { for ( (db: String, config: Option[Configuration]) <- cfgs ) yield {
      val cfg : Configuration = config.getOrElse(Configuration.empty)
      DatabaseInfo(
        cfg.getString("host").getOrElse("localhost"),
        cfg.getInt("port").getOrElse(27017),
        db,
        cfg.getString("user").getOrElse(""),
        cfg.getString("pass").getOrElse("")
      )
    }}.toSeq
    val dbInfo = if (dbInfos.isEmpty)
      DatabaseInfo("localhost", 27017, "scrupal", "", "")
    else
      dbInfos.head
    databaseForm.fill(dbInfo)
  }

  def extractDatabaseUrl(cfgs: DBConfig) : String = {
    if (!cfgs.isEmpty) {
      cfgs.head._2 map { config: Configuration =>
        config.getString("uri").getOrElse("")
      }
    }.getOrElse("")
    else "mongodb:localhost:27017/scrupal"
  }

  def makeConfiguration(db: DatabaseInfo) : Config = {
    import scala.collection.JavaConversions._
    val theMap = Map( {
        val required = Seq(
          "db." + db.name + ".uri" -> db.uri,
          "db." + db.name + ".host" -> db.host,
          "db." + db.name + ".port" -> db.port.toString
        )
        val opt1 = if (db.user.isEmpty) Seq.empty else Seq( "db." + db.name + ".user" -> db.user)
        val opt2 = if (db.pass.isEmpty) Seq.empty else Seq( "db." + db.name + ".pass" -> db.pass)
        required ++ opt1 ++ opt2
      }:_*
    )
    log.debug("Constructed map from DatabaseInfos: " + theMap )
    ConfigFactory.parseMap( theMap )
  }
*/
  /** Information for one database */
  case class DatabaseInfo(host:String, port: Int, name: String, user: String, pass: String ) {
    def uri = { "mongodb://" + host + ":" + port + "/" + name }
  }

  val databaseSection = FieldSet("Site", "Site", "Description", "Title", Seq(
    StringField("Host", "Host", "The hostname where your MongoDB server is running",
              DomainName_t, BSONString("localhost")),
    IntegerField("Port", "Port", "The port number at which your MongoDB server is running",
                  TcpPort_t, BSONLong(27172)),
    StringField("Name", "Name", "The name of the database you want to connect to",
              Identifier_t, BSONString("scrupal")),
    StringField("User", "User", "The user name for the MongoDB server authentication", Identifier_t),
    PasswordField("Password", "Password", "The password for the MongoDB server authentication", Password_t)
  ))

  case class SiteInfo(name:String="", description: String="", host:String="", requiresHttps: Boolean=false)

  val siteSection = FieldSet("Site", "Site", "Description", "Title", Seq(
    StringField("Name", "Name", "The name of the site you want to create", Identifier_t),
    StringField("Description", "Description", "A description of your site", NonEmptyString_t),
    StringField("Host", "Host", "The host name or IP address from which your site will be served", DomainName_t),
    BooleanField("HttpsRequired", "HttpsRequired", "Check whether HTTPS is required or not", Boolean_t)
  ))

  // def makeSiteForm = siteSection.fill(SiteInfo())

  case class PageInfo(name: String="", description: String="", body: String="")
  val pageSection = FieldSet("Page", "Page", "Description", "Title", Seq(
    StringField("Name", "Name", "The name of the page you want to create", Identifier_t),
    StringField("Description", "Description",  "A description or summary of your page", NonEmptyString_t),
    TextAreaField("Body", "Body", "The body of your page in markdown format", Markdown_t)
  ))

  // def makePageForm = pageSection.fill(PageInfo())

}
