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

package scrupal.db

import org.specs2.mutable.Specification
import scrupal.fakes.WithFakeScrupal
import scala.slick.session.Session

/** One line sentence description here.
  * Further description here.
  */
class CoreSchemaSpec extends Specification {

  "CoreSchema" should {
    "Accumulate table names correctly" in new WithFakeScrupal {
      withDBSession { implicit session: Session =>
        val schema : CoreSchema = new CoreSchema(sketch)(session)
        schema.tableNames.contains(schema.Instances.tableName) must beTrue
        schema.tableNames.contains(schema.Types.tableName) must beTrue
        schema.tableNames.contains(schema.Entities.tableName) must beTrue
        schema.tableNames.contains(schema.Modules.tableName) must beTrue
        schema.tableNames.contains(schema.Sites.tableName) must beTrue
        schema.tableNames.contains(schema.Alerts.tableName) must beTrue
        schema.tableNames.contains(schema.Principals.tableName) must beTrue
        schema.tableNames.contains(schema.Handles.tableName) must beTrue
        schema.tableNames.contains(schema.Tokens.tableName) must beTrue
      }
    }

    "Generate DDL SQL For Each Core Table" in new WithFakeScrupal {
      withDBSession { implicit session: Session =>
        val schema : CoreSchema = new CoreSchema(sketch)(session)
        // TODO: Finish implementing
        success
      }
    }
  }
}
