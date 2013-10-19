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

import scrupal.api.{Sketch, Site, Entity}
import play.api.libs.json.{Json}
import play.api.mvc.{AnyContent, RequestHeader, Action}
import play.api.Logger
import scrupal.utils.ConfigHelper
import scala.util.{Success, Failure, Try}
import play.api.Play.current
import scala.slick.session.Session
import scrupal.db.ScrupalSchema

/** The Entity definition for the Configuration workflow/wizard.
  * This controller handles first-time configuration and subsequent reconfiguration of the essentials of Scrupal. It
  * makes very few assumptions about the running state of Scrupal and has to operate from initial conditions where
  * not even a database is configured.
  * Further description here.
  */
object Config extends Entity('Config, "Scrupal System Configuration Entity", 'EmptyBundle ) {

  type SiteMap = Map[Symbol,String]

  object Step extends Enumeration {
    type Kind = Value
    val One_Specify_Databases = Value
    val Two_Connect_Databases = Value
    val Three_Install_Schemas = Value
    val Four_Create_Site = Value
    val Five_Create_Entity = Value

    /** Determine if the schema is valid at a given URL */
    def schemaIsValid(site: String, url: String) : Boolean = {
      false
    }

    /** Determine which step we are at based on the Context provided */
    def apply(context: Context) : (Step.Kind,Option[Throwable]) = {
      Try {
        if (context.site.isDefined || Site.size > 0) {
          // Initial loading found sites so we can assume we've got DB & Schema, skip ahead to step 5 :)
          (Five_Create_Entity,None)
        } else {
          // Something is up with loading the Sites: Config, Database, Schema. Check each
          ConfigHelper(context.config).validateDBs match {
            case Success(siteNames: Set[String]) => {
              if (siteNames.isEmpty) {
                (One_Specify_Databases,None)
              } else {
                Logger.info("Got clean database config: " + siteNames )
                // Okay, we validated that we have a clean configuration. So, we can now look at each site and assess
                // if there are URL, connection or schema issues.
                val site_results: Set[(Step.Value,Option[Throwable])] = for ( site <- siteNames ) yield {
                  try
                  {
                    context.config.getConfig("db." + site) match {
                      case Some(config) => {
                        val sketch = Sketch(config)
                        sketch.withSession { implicit  session: Session =>
                          val schema = new ScrupalSchema(sketch)
                          val schema_was_validated = schema.validate
                          schema_was_validated match {
                            case Success(false) => (Three_Install_Schemas, None)
                            case Success(true) => (Four_Create_Site, None)
                            case Failure(x) => (Three_Install_Schemas,Some(x))
                          }
                        }
                      }
                      case None => (One_Specify_Databases, Some(context.config.reportError("db."+site,
                        "No configuration found for this database")))
                    }
                  }
                  catch { case x : Throwable => (Two_Connect_Databases,Some(x)) }
                }
                // We just collected together a list of the results for each site. now let's find the earliest
                // step amongst them.
                site_results.foldLeft[(Config.Step.Value,Option[Throwable])]((Four_Create_Site,None)) {
                  case (step1:(Step.Kind, Option[Throwable]), step2:(Step.Kind, Option[Throwable])) =>
                    if (step1._1 < step2._1) step1 else step2
                }
              }
            }
            case Failure(x)  => (One_Specify_Databases, Some(x))
          }
        }
      } match {
        case Success(s) => (s._1,None)
        case Failure(x) => (One_Specify_Databases, Some(x))
      }
    }
  }

/*
          val dbs = db_config.get()

          context.config.getConfig("db")
          val sites : Map[String,String] = Configuration.from(db_config.get())
        val sites : SiteBootstrap.Site2Jdbc = SiteBootstrap.get(context)
        if (sites.isEmpty)
          One_Specify_Databases
        else {
          val invalid = for (
            (site: String, (url: String, e: Option[String])) <- sites if e.isDefined
          ) yield e

          if (invalid.isEmpty) {
            val valid = for (
              (site: String, (url: String, e: Option[String])) <- sites if !e.isDefined
            ) yield (site, url)
            if (!valid.isEmpty) {
              if (valid.foldLeft(true)( (b,e) => b && schemaIsValid(e._1, e._2))) {
                Four_DB_Schema
              } else {
                // CoreModule.validateSchema
                Logger.debug("SiteBootstrap has returned: " + valid )
                Three_DBS_Connected
              }
            }
            else
              One_Specify_Databases
          } else
            Two_DBS_Validated
        }
      }
    }
  }
  */
  def get(id: String, what: String) : Action[AnyContent] = Action { implicit request : RequestHeader =>

    val step = Step(context)
    Ok(Json.obj( "state" -> step.toString) )
  }

}
