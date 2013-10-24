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

import org.specs2.mutable.Specification
import play.api.mvc.RequestHeader
import play.api.{Logger, Configuration}
import scrupal.db.{CoreSchema,Sketch}
import scala.slick.lifted.DDL
import scala.slick.driver._
import scala.slick.session.Session
import scrupal.fakes.WithFakeScrupal
import scala.slick.lifted.DDL
import scrupal.api.{Instance, EssentialSite}
import play.api.libs.json.Json
import scrupal.utils.ConfigHelper

/** This is the test suite for the Config.Step class
  *
  */
class ConfigStepSpec extends Specification {

  val nullRequest =  new RequestHeader() {
    def headers: play.api.mvc.Headers = ???
    def id: Long = ???
    def method: String = ???
    def path: String = ???
    def queryString: Map[String,Seq[String]] = ???
    def remoteAddress: String = ???
    def tags: Map[String,String] = ???
    def uri: String = ???
    def version: String = ???
  }

  def simpleContext(config: Map[String,Object]) : Context = Context(
    config = Configuration.empty ++ Configuration.from( config ))(nullRequest)

  "ConfigWizard.getDatabaseNames" should {
    "Recognize Step 0 when there's no configuration" in new WithFakeScrupal {
      val triple = ConfigWizard.getDatabaseNames(Configuration.from(Map()))
      triple._1 must beEqualTo(ConfigWizard.Step.Zero_Welcome)
      triple._2.isDefined must beTrue
      triple._3.size must beEqualTo(0)
    }

    "Recognize Step 0 on empty db config" in new WithFakeScrupal {
      val config = Configuration.from(
        Map(ConfigWizard.scrupal_database_config_file -> "test/resources/db/config/empty_conf.conf")
      )
      val triple = ConfigWizard.getDatabaseNames(config)
      triple._1 must beEqualTo(ConfigWizard.Step.Zero_Welcome)
      triple._2.isDefined must beTrue
      triple._3.size must beEqualTo(0)
    }
    "Recognize Step 0 on default db config" in new WithFakeScrupal {
      val config = Configuration.from(
        Map(ConfigWizard.scrupal_database_config_file -> "test/resources/db/config/default.conf")
      )
      val triple = ConfigWizard.getDatabaseNames(config)
      triple._1 must beEqualTo(ConfigWizard.Step.Zero_Welcome)
      triple._2.isDefined must beTrue
      triple._3.size must beEqualTo(0)
    }
    "Recognize Step 1 on invalid db config" in new WithFakeScrupal {
      val config = Configuration.from (
        Map(ConfigWizard.scrupal_database_config_file -> "test/resources/db/config/bad.conf")
      )
      val triple = ConfigWizard.getDatabaseNames(config)
      triple._1 must beEqualTo(ConfigWizard.Step.One_Specify_Databases)
      triple._2.isDefined must beTrue
      triple._3.size must beEqualTo(0)
    }

    "Recognize Step 1 on valid mem db config" in new WithFakeScrupal {
      val config = Configuration.from (
        Map(ConfigWizard.scrupal_database_config_file -> "test/resources/db/config/valid_mem.conf")
      )
      val triple = ConfigWizard.getDatabaseNames(config)
      triple._1 must beEqualTo(ConfigWizard.Step.One_Specify_Databases)
      triple._2.isDefined must beTrue
      triple._3.size must beEqualTo(0)
    }

    "Recognize Step 2 on valid db config" in new WithFakeScrupal {
      val config = Configuration.from ( Map(
        ConfigWizard.scrupal_database_config_file -> "test/resources/db/config/valid.conf"
        )
      )
      val triple = ConfigWizard.getDatabaseNames(config)
      triple._1 must beEqualTo(ConfigWizard.Step.Two_Connect_Databases)
      triple._2.isDefined must beFalse
      triple._3.size must beGreaterThan(0)
    }
  }

  "ConfigWizard.checkSchema" should {
    "Recognize Step 2 on non-existent db" in new WithFakeScrupal {
      val config = Configuration.from (
        Map(ConfigWizard.scrupal_database_config_file -> "test/resources/db/config/no_exist.conf")
      )
      val triple = ConfigWizard.checkSchemas(config)
      triple._1 must beEqualTo(ConfigWizard.Step.Two_Connect_Databases)
      ({triple._2 map { xcptn: Throwable => xcptn.getMessage must contain("not found"); xcptn }}.isDefined) must beTrue
      triple._3.size must beEqualTo(0)
    }

    "Recognize Step 3 on empty database" in new WithFakeScrupal {
      val config = Configuration.from (
        Map(ConfigWizard.scrupal_database_config_file -> "test/resources/db/config/empty_db.conf")
      )
      val triple = ConfigWizard.checkSchemas(config)
      triple._1 must beEqualTo(ConfigWizard.Step.Three_Install_Schemas)
      ({triple._2 map { xcptn: Throwable => xcptn.getMessage must contain("empty"); xcptn }}.isDefined) must beTrue
      triple._3.size must beEqualTo(0)
    }

    "Recognize Step 3 on schema missing tables" in {
      val config = Configuration.from (
        Map(ConfigWizard.scrupal_database_config_file -> "test/resources/db/config/empty_db.conf")
      )
      // Get a DB Sketch with the same config values as specified above
      val db_config = ConfigHelper(config).getDbConfig
      val sketch = Sketch( db_config.getConfig("db.empty_db").get )
      sketch.withSession { implicit session: Session =>
        val schema = new CoreSchema(sketch)
        schema.createCoreTables
        val triple = ConfigWizard.checkSchemas(config)
        triple._1 must beEqualTo(ConfigWizard.Step.Three_Install_Schemas)
        ({triple._2 map { xcptn: Throwable => xcptn.getMessage must contain("validation"); xcptn }}.isDefined) must
          beTrue
        triple._3.size must beEqualTo(0)
      }
    }

    "Recognize Step 4 on valid schema" in {
      val config = Configuration.from (
        Map(ConfigWizard.scrupal_database_config_file -> "test/resources/db/config/empty_db.conf")
      )
      // Get a DB Sketch with the same config values as specified above
      val db_config = ConfigHelper(config).getDbConfig
      val sketch = Sketch( db_config.getConfig("db.empty_db").get )

      // Install the Scrupal Schema
      sketch.withSession { implicit session: Session =>
        val schema = new CoreSchema(sketch)
        schema.create(session)
        val triple = ConfigWizard.checkSchemas(config)
        triple._1 must beEqualTo(ConfigWizard.Step.Four_Create_Site)
        ({triple._2 map { xcptn: Throwable => xcptn.getMessage must contain("no sites"); xcptn }}.isDefined) must
          beTrue
        triple._3.size must beEqualTo(1)
      }
    }

    "Recognize Step 5 on valid site" in {
      val config = Configuration.from (
        Map(ConfigWizard.scrupal_database_config_file -> "test/resources/db/config/empty_db.conf")
      )
      // Get a DB Sketch with the same config values as specified above
      val db_config = ConfigHelper(config).getDbConfig
      val sketch = Sketch( db_config.getConfig("db.empty_db").get )

      // Install the Scrupal Schema
      sketch.withSession { implicit session: Session =>
        val schema = new CoreSchema(sketch)
        schema.create(session)
        schema.Sites.insert( EssentialSite('Test,"Testing",8080,"localhost",80,false,true))
        val triple = ConfigWizard.checkSchemas(config)
        triple._1 must beEqualTo(ConfigWizard.Step.Five_Create_Page)
        ({triple._2 map { xcptn: Throwable => xcptn.getMessage must contain("no entities"); xcptn }}.isDefined) must
          beTrue
        triple._3.size must beEqualTo(1)
      }
    }

    "Recognize Step 5 on valid entity" in {
      val config = Configuration.from (
        Map(ConfigWizard.scrupal_database_config_file -> "test/resources/db/config/empty_db.conf")
      )
      // Get a DB Sketch with the same config values as specified above
      val db_config = ConfigHelper(config).getDbConfig
      val sketch = Sketch( db_config.getConfig("db.empty_db").get )

      // Install the Scrupal Schema
      sketch.withSession { implicit session: Session =>
        val schema = new CoreSchema(sketch)
        schema.create(session)
        schema.Sites.insert( EssentialSite('Test,"Testing",8080,"localhost",80,false,true))
        schema.Instances.insert( Instance('AnInstance, "Testing", 'Page, Json.obj(
          "name" -> "TestInstance",
          "description" -> "Testing",
          "body" -> "# Heading\nThis is a test."
        )))
        val triple = ConfigWizard.checkSchemas(config)
        triple._1 must beEqualTo(ConfigWizard.Step.Six_Success)
        triple._2.isDefined must beFalse
        triple._3.size must beEqualTo(1)
      }
    }

  }
}
