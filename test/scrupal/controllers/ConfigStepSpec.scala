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
import scrupal.api.{Sketch, ConfigKey}
import play.api.{Logger, Configuration}
import scala.util.Success
import scrupal.db.ScrupalSchema
import scala.slick.session.Session

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

  def simpleContext(config: Map[String,Object])(implicit request: RequestHeader ) : Context = Context(
    config = Configuration.empty ++ play.api.Configuration.from( config )
  )


  "Config.Step" should {
    "Identify Step 1 - empty config" in {
      implicit val request : RequestHeader = nullRequest
      implicit val context = simpleContext(Map())
      val step = Config.Step(context)
      step must beEqualTo((Config.Step.One_Specify_Databases,None))
    }

    "Identify Step 1 - wrong config" in {
      implicit val request : RequestHeader = nullRequest
      implicit val context = simpleContext(
        Map( "db" -> Map( "default" -> Map( "foo" -> "bar")))
      )
      val step = Config.Step(context)
      step must beEqualTo((Config.Step.One_Specify_Databases,None))

    }

    "Identify Step 2 - bad config" in {
      implicit val request : RequestHeader = nullRequest
      implicit val context = simpleContext(
        Map( "db" -> Map( "default" -> Map( "url" -> "not-a-url")))
      )
      val step = Config.Step(context)
      step must beEqualTo((Config.Step.Two_Connect_Databases,None))
    }

    "Identify Step 2 - no such db" in {
      implicit val request : RequestHeader = nullRequest
      implicit val context = simpleContext(
        Map( "db" -> Map( "default" -> Map( "url" -> "jdbc:h2:not_an_existing_db;IFEXISTS=TRUE")))
      )
      val step = Config.Step(context)
      step must beEqualTo((Config.Step.Two_Connect_Databases,None))
    }

    "Identify Step 3 - no schema" in {
      implicit val request : RequestHeader = nullRequest
      implicit val context = simpleContext( Map( "db" -> Map( "default" -> Map( "url" -> "jdbc:h2:mem:empty_test"))
      ) )

      val step = Config.Step(context)
      step must beEqualTo((Config.Step.Three_Install_Schemas,None)) or beEqualTo((Config.Step.Two_Connect_Databases,
        None))
    }

    "Identify Step 4 - missing site instances " in {
      implicit val request : RequestHeader = nullRequest
      implicit val context = simpleContext(
        Map( "db" -> Map( "default" -> Map( "url" -> "jdbc:h2:mem:no_sites;DB_CLOSE_DELAY=2")))
      )

      // Get a DB Sketch with the same config values as specified above
      val sketch = Sketch( context.config.getConfig("db.default").get )

      // Install the Scrupal Schema
      sketch.withSession { implicit session: Session =>
        val schema = new ScrupalSchema(sketch)
        schema.create(session)
      }

      // Now let's see if it validates that all the tables are there.
      val step = Config.Step(context)

      step must beEqualTo((Config.Step.Four_Create_Site,None))
    }



  }
}
