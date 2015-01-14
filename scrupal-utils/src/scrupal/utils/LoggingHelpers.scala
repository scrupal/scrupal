/**********************************************************************************************************************
 * Copyright © 2015 Reactific Software LLC                                                                            *
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

package scrupal.utils

import ch.qos.logback.classic.{LoggerContext, Level, Logger}
import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.core.ConsoleAppender
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.CyclicBufferAppender
import ch.qos.logback.core.rolling.{SizeBasedTriggeringPolicy, FixedWindowRollingPolicy, RollingFileAppender}
import ch.qos.logback.classic.html.HTMLLayout

import java.io.File

import org.slf4j.LoggerFactory

import scala.util.matching.Regex
import scala.collection.JavaConverters._
import scala.util.{Failure, Success, Try}

import scalatags.Text.all._
import scalatags.Text.TypedTag


/** Log File Related Helpers
  *
  *  This object just provides a variety of utilities for manipulating LogBack programatically.
  */
object LoggingHelpers extends ScrupalComponent {

  def initializeLogging(forDebug: Boolean = true) = {
     if (forDebug)
       setToDebug("scrupal\\..*")
     else
       setToWarn("scrupal\\..*")
  }

  /** Easy access to the root logger */
  def rootLogger: Logger = { LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME).asInstanceOf[Logger] }

  /** Easy access to the logger context */
  def loggerContext: LoggerContext = { rootLogger.getLoggerContext }

  /** Set a component to ERROR logging level */
  def setToError(component: ScrupalComponent) = setLoggingLevel(component.getClass.getName, Level.ERROR)
  def setToError(pkg: String) = setLoggingLevel(pkg, Level.ERROR)

  /** Set a component to WARN logging level */
  def setToWarn(component: ScrupalComponent) = setLoggingLevel(component.getClass.getName, Level.WARN)
  def setToWarn(pkg: String) = setLoggingLevel(pkg, Level.WARN)

  /** Set a component to WARN logging level */
  def setToInfo(component: ScrupalComponent) = setLoggingLevel(component.getClass.getName, Level.INFO)
  def setToInfo(pkg: String) = setLoggingLevel(pkg, Level.INFO)

  /** Set a component to WARN logging level */
  def setToDebug(component: ScrupalComponent) = setLoggingLevel(component.getClass.getName, Level.DEBUG)
  def setToDebug(pkg: String) = setLoggingLevel(pkg, Level.DEBUG)

  /** Set a component to WARN logging level */
  def setToTrace(component: ScrupalComponent) = setLoggingLevel(component.getClass.getName, Level.TRACE)
  def setToTrace(pkg: String) = setLoggingLevel(pkg, Level.TRACE)


  /** Set Logging Level Generically.
    * This function sets the logging level for any pkg that matches a regular expression. This allows a variety of
    * loggers to be set without knowing their full names explicitly.
    * @param regex A Scala regular expression string for the names of the loggers to match
    * @param level The level you want any matching loggers to be set to.
    * @return A list of the names of the loggers whose levels were set
    */
  def setLoggingLevel(regex: String, level: Level) : Seq[String] = {
    val lc = loggerContext
    for (logger <- findLoggers(regex)) yield {
      val previousLevel: Level = logger.getLevel
      logger.setLevel(level)
      log.trace("Switched Logging Level For '" + logger.getName + "' from " + previousLevel + " to " + level)
      logger.getName
    }
  }

  /** Find loggers matching a pattern
    * @param pattern A Scala regular expression string for the names of the loggers to match
    * @return A sequence of the matching loggers
    */
  def findLoggers(pattern: String) : Seq[Logger] = {
    val regex = new Regex(pattern)
    val lc = loggerContext
    for (log: Logger <- lc.getLoggerList.asScala  if  regex.findFirstIn(log.getName).isDefined ) yield log
  }

  /** Determine if a logger has an appender or not
    * @param logger The logger to check
    * @return true iff the logger has an appender
    */
  def hasAppenders(logger: Logger) : Boolean = { logger.iteratorForAppenders().hasNext() }

  def getLoggingTableData : (Iterable[String], Iterable[Iterable[String]]) = {
    val lc = loggerContext
    val data = for (log: Logger <- lc.getLoggerList.asScala) yield {
      List(log.getName, log.getEffectiveLevel.toString, log.getLoggerContext.getName)
    }
    List("Name", "Level", "Context") -> data
  }

  def getLoggingConfig : List[(String,String)] = {
    val lc = loggerContext
    for (log: Logger <- lc.getLoggerList.asScala) yield {
      log.getName -> log.getEffectiveLevel.toString
    }
  }.toList

  def removeAppender(name: String) = {
    val existingAppender = rootLogger.getAppender(name)
    if (existingAppender != null) {
      existingAppender.stop()
      rootLogger.detachAppender(existingAppender)
    }
  }

  private val FILE_PATTERN = "%d %-7relative %-5level [%thread:%logger{30}] - %msg%n%xException"
  private val CONSOLE_PATTERN = "%date %-5level %logger{30} - %message%n%xException"

  private def makeEncoder(pattern: String, immediateFlush: Boolean, lc: LoggerContext) = {
    val ple = new PatternLayoutEncoder()
    ple.setPattern(pattern)
    ple.setOutputPatternAsHeader(false)
    ple.setImmediateFlush(immediateFlush)
    ple.setContext(lc)
    ple.start()
    ple
  }

  private def setRollingPolicy(fwrp: FixedWindowRollingPolicy, maxFiles: Int, fName: String) = {
    fwrp.setMaxIndex(maxFiles)
    fwrp.setMinIndex(1)
    fwrp.setFileNamePattern(fName + ".%i.zip")
  }

  private def makeRollingPolicy(lc: LoggerContext, maxFiles: Int, fName: String) : FixedWindowRollingPolicy = {
    val fwrp = new FixedWindowRollingPolicy
    setRollingPolicy(fwrp, maxFiles, fName)
    fwrp.setContext(lc)
    fwrp.start()
    fwrp
  }

  private def setTriggeringPolicy(sbtp: SizeBasedTriggeringPolicy[ILoggingEvent], maxSize: Int) = {
    sbtp.setMaxFileSize(maxSize + "MB")
  }

  private def makeTriggeringPolicy(lc: LoggerContext, maxSize: Int) : SizeBasedTriggeringPolicy[ILoggingEvent] = {
    val sbtp = new SizeBasedTriggeringPolicy[ILoggingEvent]
    setTriggeringPolicy(sbtp, maxSize)
    sbtp.setContext(lc)
    sbtp.start()
    sbtp
  }

  val FILE_APPENDER_NAME = "FILE"
  val PAGE_APPENDER_NAME = "PAGE"
  val STDOUT_APPENDER_NAME = "STDOUT"

  def setFileAppender(
    file: File,
    maxFiles: Int,
    maxFileSizeInMB: Int,
    immediateFlush: Boolean,
    name: String = FILE_APPENDER_NAME
  ) = Try {
    val lc = loggerContext
    val fName = file.getCanonicalPath
    rootLogger.getAppender(name) match {
      case rfa: RollingFileAppender[ILoggingEvent] => {
        rfa.getRollingPolicy match {
          case fwrp: FixedWindowRollingPolicy => setRollingPolicy(fwrp, maxFiles, fName)
          case _ => rfa.setRollingPolicy(makeRollingPolicy(lc, maxFiles, fName))
        }
        rfa.getTriggeringPolicy match {
          case sbtp: SizeBasedTriggeringPolicy[ILoggingEvent] => setTriggeringPolicy(sbtp, maxFileSizeInMB)
          case _ => rfa.setTriggeringPolicy(makeTriggeringPolicy(lc, maxFileSizeInMB))
        }
        rfa.getEncoder match {
          case ple: PatternLayoutEncoder => ple.setImmediateFlush(immediateFlush)
          case _ => rfa.setEncoder(makeEncoder(FILE_PATTERN, immediateFlush, lc))
        }
        rfa
      }
      case _ => {
        val rfa = new RollingFileAppender[ILoggingEvent]
        rfa.setContext(lc)
        rfa.setAppend(true)
        rfa.setName(name)
        rfa.setFile(fName)
        rfa.setEncoder(makeEncoder(FILE_PATTERN, immediateFlush, lc))
        rfa.setRollingPolicy(makeRollingPolicy(lc, maxFiles, fName))
        rfa.setTriggeringPolicy(makeTriggeringPolicy(lc, maxFileSizeInMB))
        rfa.start()
        rootLogger.addAppender(rfa)
        rfa
      }
    }
  } match {
    case Success(fa) => Some(fa)
    case Failure(xcptn) => log.error("Failed to set RollingFileAppender: ", xcptn); None
  }

  var pageAppender: Option[CyclicBufferAppender[ILoggingEvent]] = None

  def setPageAppender(maxSize: Int, name: String = PAGE_APPENDER_NAME) = Try {
    val lc = loggerContext
    rootLogger.getAppender(name) match {
      case cba: CyclicBufferAppender[ILoggingEvent] => {
        cba.setMaxSize(maxSize)
        cba
      }
      case _ => {
        val cba = new CyclicBufferAppender[ILoggingEvent]()
        cba.setMaxSize(maxSize)
        cba.setName(name)
        cba.setContext(lc)
        cba.start()
        rootLogger.addAppender(cba)
        cba
      }
    }
  } match {
    case Success(cb) => pageAppender = Some(cb)
    case Failure(xcptn) => log.warn("Failed to set PageAppender: ", xcptn); pageAppender = None;
  }

  def convertRecentEventsToHtml() : TypedTag[String] = Try {
    val lc = loggerContext
    pageAppender match {
      case None => div("No log content available.")
      case Some(pa) => {
        val layout = new HTMLLayout()
        val buffer = new StringBuilder(4096)
        layout.setContext(lc)
        layout.setPattern("%date%relative%level%logger%msg%ex")
        layout.setTitle("")
        layout.start()
        for (i <- 0 until pa.getLength) {
          buffer.append( layout.doLayout(pa.get(i)) )
        }
        div(buffer.toString())
      }
    }
  } match {
    case Success(result) => result
    case Failure(xcptn) =>
      log.warn("Error while converting log events to html: ", xcptn)
      div(`class`:="text-danger",
        s"Error while converting log events to html: ${xcptn.getClass.getCanonicalName}: ${xcptn.getMessage}"
      )
  }

  def setStdOutAppender(name: String = STDOUT_APPENDER_NAME) = Try {
    val lc = loggerContext
    rootLogger.getAppender(name) match {
      case ca : ConsoleAppender[ILoggingEvent] => {
        ca.setWithJansi(true)
        ca
      }
      case _ => {
        val ca = new ConsoleAppender[ILoggingEvent]
        ca.setContext(lc)
        ca.setEncoder(makeEncoder(CONSOLE_PATTERN, immediateFlush=true, lc))
        ca.setWithJansi(true)
        ca.start()
        rootLogger.addAppender(ca)
        ca
      }
    }
  }
}
