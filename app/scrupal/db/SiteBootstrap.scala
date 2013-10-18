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

import java.io._
import scrupal.api.ConfigKey
import java.sql.{SQLException, DriverManager}
import scrupal.controllers.Context
import scala.Some
import java.util.Properties
import scala.io.{BufferedSource, Source}


/** Bootstrapping mechanism for getting the initial set of sites and corresponding JDBC URLs for them.
  * When we first start up, there are no databases configured anywhere. That's fine and by design. In this mode,
  * Scrupal will only direct the user to configure the system. Once sites have been defined, however,
  * their JDBC URL needs to be stored somewhere that it can be loaded from on restart. That would be the db.config
  * file in the conf folder. The user should never have to write this file manually, this class does that instead.
  *
  * The format of the file is simple:
  * {{{
  *   SITENAME\tJDBC_URL
  * }}}
  * That is, the name of the site (which can include space characters), a tab character,
  * and the JDBC URL for the site. Note that multiple sites can live in the same database and that the database and
  * full user login credentials are put in the JDBC_URL part. This requires some security about who can read the file.
  * It should never be servable by Scrupal itself.
  *
  */
object  SiteBootstrap {

  type ParseResult = (String, Option[String])

  type Site2Jdbc = Map[String,ParseResult]

  def getOne(line: String) :  (String,ParseResult)= {
    if (line != null ) {
      val trimmed_line = line.trim
      if (trimmed_line.length > 0) {
        val strs = line.trim.split('\t')
        if (strs.length == 2) {
          val (site,url) = (strs(0), strs(1))
          val tsite = site.trim
          if (tsite.length > 0) {
            if (url.startsWith("jdbc:")) {
               try {
                val driver = DriverManager.getDriver(url)
                val conn = driver.connect(url, new Properties())
                // looks good, return it
                (tsite, (url,None))
              }
              catch {
                case xcptn: Throwable => (site, (url, Some(xcptn.getClass.getSimpleName + ": " + xcptn.getMessage)))
              }
            }
            else {
              (tsite, (url, Some("JDBC URL does not begin with 'jdbc:'")))
            }
          } else {
            ("", (url, Some("Site name has zero length")))
          }
        } else {
          ("", ("", Some("Wrong number of tab separated values on the line")))
        }
      } else {
        ("",("",Some ("Line is empty after trimming")))
      }
    } else {
      ("", ("", Some("Line is null")))
    }
  }

  def get(s: Source) : Site2Jdbc = {
    (s.getLines() filter { p: String => p.length > 0 } map { line: String => getOne(line) } filter {
        tup: (String,ParseResult) => tup._1.length > 0 && tup._2._1.length > 0 }).toMap
  }

  def get(is: InputStream) : Site2Jdbc = {
    get(Source.fromInputStream(is))
  }

  def get(fileContent: String) : Site2Jdbc = {
    get(Source.fromString(fileContent))
  }

  def get(file: File) : Site2Jdbc = {
    require(file.isFile)
    get(Source.fromFile(file, 1024))
  }

  def get ( context: Context) : Site2Jdbc = {
    get(new File(context.config.getString(ConfigKey.db_config).getOrElse("./conf/db.config")))
  }

  def get( context: Context, name: String) : Option[ParseResult] = {
    require(name.length > 0)
    get(context).get(name)
  }

  /*
  def put( context: Context, name : String, url: String ) : Boolean = {
    require(name.length > 0)
    require(url.startsWith("jdbc:"))
     for ( (name:String, v: ParseResult) <- get(context, name) if (v._1.isDefined && v._1) foreach { (x: String,
     y: ParseResult) => }
  }
  */
}

