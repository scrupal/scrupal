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

package models

import scrupal.api.Thing
import scrupal.models.{DomainName_t, Identifier_t, TcpPort_t}

/** The information about a Site that Scrupal vends.
  * Scrupal can be configured to respond to numerous web sites (multi-tenant) from a single instance. This Site
  * represents the salient information about a given Site that is being served to users. A Site consists of a set of
  * modules that are configured for its use (not necessarily all modules loaded are used by a given Site), the
  * configuration for that set of modules for that site, and some rudimentary information for routing requests.
  */
case class Site (
  name : Symbol,
  description : String,
  domain : String
)
  // extends Thing(name, description)
{

  /*
  val name
  Setting('name, Identifier_t, "The name of the site Scrupal will be serving"),
  Setting('domain, DomainName_t, "The domain name at which Scrupal will receive requests"),
  Setting('port, TcpPort_t, "The TCP Port number on which Scrupal should listen")
  */
}
