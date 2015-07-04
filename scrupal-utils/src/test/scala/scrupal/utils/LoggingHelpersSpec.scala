/**********************************************************************************************************************
 * This file is part of Scrupal, a Scalable Reactive Web Application Framework for Content Management                 *
 *                                                                                                                    *
 * Copyright (c) 2015, Reactific Software LLC. All Rights Reserved.                                                   *
 *                                                                                                                    *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance     *
 * with the License. You may obtain a copy of the License at                                                          *
 *                                                                                                                    *
 *     http://www.apache.org/licenses/LICENSE-2.0                                                                     *
 *                                                                                                                    *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed   *
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for  *
 * the specific language governing permissions and limitations under the License.                                     *
 **********************************************************************************************************************/

package scrupal.utils

import ch.qos.logback.classic.{ LoggerContext, Level, Logger }

import org.specs2.mutable.Specification

class LoggingHelpersSpec extends Specification with ScrupalComponent {

  lazy val selfName = "scrupal.utils.LoggingHelpersSpec"
  sequential // these tests are stateful

  "LoggingHelpersSpec" should {
    "Set scrupal to debug on debug init" in {
      LoggingHelpers.initializeLogging(forDebug=true)
      LoggingHelpers.getLoggingLevel(selfName) must beEqualTo(Level.DEBUG)

    }
    "Set scrupal to warning on non-debug init" in {
      LoggingHelpers.initializeLogging(forDebug=false)
      LoggingHelpers.getLoggingLevel(selfName) must beEqualTo(Level.WARN)
    }
    "rootLogger returns a Logger" in {
      LoggingHelpers.rootLogger.isInstanceOf[Logger] must beTrue
    }
    "loggerContext return s Context" in {
      LoggingHelpers.loggerContext.isInstanceOf[LoggerContext] must beTrue
    }

    "setToError must set this logger to error" in {
      LoggingHelpers.setToError(this)
      LoggingHelpers.getLoggingLevel(selfName) must beEqualTo(Level.ERROR)
    }
    "setToWarn must set this logger to warn" in {
      LoggingHelpers.setToWarn(this)
      LoggingHelpers.getLoggingLevel(selfName) must beEqualTo(Level.WARN)
    }
    "setToInfo must set this logger to info" in {
      LoggingHelpers.setToInfo(this)
      LoggingHelpers.getLoggingLevel(selfName) must beEqualTo(Level.INFO)
    }
    "setToDebug must set this logger to info" in {
      LoggingHelpers.setToDebug(this)
      LoggingHelpers.getLoggingLevel(selfName) must beEqualTo(Level.DEBUG)
    }
    "setToTrace must set this logger to info" in {
      LoggingHelpers.setToTrace(this)
      LoggingHelpers.getLoggingLevel(selfName) must beEqualTo(Level.TRACE)
    }
    "setLoggingLevel should set level correctly" in {
      LoggingHelpers.setLoggingLevel(selfName, Level.WARN)
      LoggingHelpers.getLoggingLevel(selfName) must beEqualTo(Level.WARN)
      LoggingHelpers.setLoggingLevel(selfName, Level.DEBUG)
      LoggingHelpers.getLoggingLevel(selfName) must beEqualTo(Level.DEBUG)
      LoggingHelpers.setLoggingLevel(selfName, Level.ERROR)
      LoggingHelpers.getLoggingLevel(selfName) must beEqualTo(Level.ERROR)
      LoggingHelpers.setLoggingLevel(selfName, Level.INFO)
      LoggingHelpers.getLoggingLevel(selfName) must beEqualTo(Level.INFO)
      LoggingHelpers.setLoggingLevel(selfName, Level.TRACE)
      LoggingHelpers.getLoggingLevel(selfName) must beEqualTo(Level.TRACE)
    }
    "findLoggers(pattern) should find the scrupal.utils loggers" in {
      val loggers = LoggingHelpers.findLoggers("scrupal.utils.*")
      loggers.isEmpty must beFalse
      val found = loggers.find { logger ⇒ logger.getName == this.log.name }
      found.nonEmpty must beTrue
    }

    "hasAppenders should find an appender for this test case" in {
      LoggingHelpers.hasAppenders(this.log.logger.asInstanceOf[Logger]) must beFalse // FIXME: wrong result?
      pending("clarity on logging appenders")
    }
    "getLoggingTableData returns table data" in {
      val table = LoggingHelpers.getLoggingTableData
      table._1.nonEmpty must beTrue
      table._2.nonEmpty must beTrue
    }

    "getLoggingConfig returns string pairs" in {
      val list : List[(String,String)] = LoggingHelpers.getLoggingConfig
      list.nonEmpty must beTrue
    }
  }
}
