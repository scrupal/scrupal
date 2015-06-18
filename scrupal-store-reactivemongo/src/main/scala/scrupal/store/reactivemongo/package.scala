/**********************************************************************************************************************
 * This file is part of Scrupal, a Scalable Reactive Content Management System.                                       *
 *                                                                                                                    *
 * Copyright © 2015 Reactific Software LLC                                                                            *
 *                                                                                                                    *
 * Licensed under the Apache License, Version 2.0 (the "License");  you may not use this file                         *
 * except in compliance with the License. You may obtain a copy of the License at                                     *
 *                                                                                                                    *
 *        http://www.apache.org/licenses/LICENSE-2.0                                                                  *
 *                                                                                                                    *
 * Unless required by applicable law or agreed to in writing, software distributed under the                          *
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,                          *
 * either express or implied. See the License for the specific language governing permissions                         *
 * and limitations under the License.                                                                                 *
 **********************************************************************************************************************/

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

package scrupal.store

import akka.actor.ActorSystem

import scala.concurrent.duration._

import reactivemongo.api.FailoverStrategy



/** Database Abstractions
  * This library contains various abstractions and interfaces to the MongoDB replica sets that Scrupal uses. Scrupal
  * uses ReactiveMongo, a non-blocking asynchronous driver for MongoDB. On top of ReactiveMongo, we provide other
  * utilities to make using Mongo easier.
  *
  * When implementing the database interface, we want a trade off between flexibility and ease of use. We also want to
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
package object reactivemongo {

  // NOTE: If needed: val system = ActorSystem("Scrupal-DB")

  type DB = reactivemongo.api.DefaultDB

  object DefaultFailoverStrategy extends FailoverStrategy(
    initialDelay=1.seconds, retries=3, delayFactor = { i ⇒ Math.log10(i) }
  )


}
