/**********************************************************************************************************************
 * This file is part of Scrupal a Web Application Framework.                                                          *
 *                                                                                                                    *
 * Copyright (c) 2014, Reid Spencer and viritude llc. All Rights Reserved.                                            *
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

package scrupal

import play.api.libs.json._
import reactivemongo.bson.BSONObjectID

/** Database Abstractions
  * This library contains various abstractions and interfaces to the MongoDB replica sets that Scrupal uses. Scrupal
  * uses three open source components to assist with this:
  * - ReactiveMongo: A non-blocking asynchronous driver for MongoDB
  * - Play2-ReactiveMongo: A Play2 extension to integrate ReactiveMongo with Play's JSON interface
  * - reactivemongo-extensions-json: A set of DAO extensions for JSON
  *
  * When implementing the database interface, we want a tradeoff between flexibility and ease of use. We also want to
  * be mindful of performance and security aspects of the database. Here are some principles learned from the Mongo DB
  * Developer Days in DC 10/14/14:
  * - Design documents around the natural entities the application uses. MongoDB is effective because related data is
  *   grouped together in a document and fetched/read/transmitted together.
  * - Optimize document content to minimize reads (i.e. read the 1 document not 100)
  * - Optimize document content to prefer update over insert (update are done in place and much faster)
  * - Don't leave security until deployment time. The schema may need to support it. Run dev in a secure mode.
  * - Implement indexes judiciously. They improve read speeds and slow down write speeds.
  * - The _id index (unique, immutable) can be any data, not just a BSONObjectID - use the natural unique primary key
  * - Choose sharding keys exceptionally well as they will make scalability effective .. or not.
  */
package object db {

  implicit val BSONObjectID_Formats = new Format[BSONObjectID] {
    def reads(jsv: JsValue) : JsResult[BSONObjectID] = {
      (JsPath \ "$oid").read[String].reads(jsv).asOpt match {
        case Some(str) => JsSuccess(BSONObjectID(str))
        case None => JsSuccess(BSONObjectID.generate)
      }
    }
    def writes(o : BSONObjectID) : JsValue = {
      JsObject(Seq("$oid" -> JsString(o.stringify)))
    }
  }
}
