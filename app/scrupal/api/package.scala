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

package scrupal

import play.api.libs.json._

/** The Module API to Scrupal.
  * This package provides all the abstract type definitions needed to write a module for Scrupal. Since Scrupal itself
  * is simply the "Core" module, this API provides essential everything needed to write Scrupal itself and any extension
  * to Scrupal through the introduction of a new module.
  *
  * Rule: scrupal.api should be fundamental an stand alone. That means it should not import anything from other
  * scrupal packages. Period.
  */
package object api {

  type Identifier = Symbol

  implicit val Symbol_Format : Format[Symbol] = {
    new Format[Symbol] {
      def reads(json: JsValue) : JsResult[Symbol] = { JsSuccess(Symbol(json.asInstanceOf[JsString].value)) }
      def writes(s: Symbol) : JsValue = JsString(s.name)
    }
  }

  // implicit val Identifier_Format : Format[Identifier] = Symbol_Format

  implicit val OptionSymbol_Format : Format[Option[Symbol]] = {
    new Format[Option[Symbol]] {
      def reads(json: JsValue) : JsResult[Option[Symbol]] = {
        val defined = JsPath \ "defined"
        defined.read[Boolean].reads(json).asOpt match {
          case Some(b) =>
            if (b) {
              val value = JsPath \ "value"
              value.read[String].reads(json).asOpt match {
                case Some(str) => JsSuccess(Some(Symbol(str)))
                case None => JsError(value,"Field not found")
              }
            } else {
              JsSuccess(None)
            }
          case None => JsError(defined, "Field not found")
        }
      }
      def writes(os: Option[Symbol]) : JsValue = {
        JsObject(
          Seq(
            "defined" -> JsBoolean(os.isDefined),
            "value" -> JsString(os.getOrElse('null).name)
          )
        )
      }
    }
  }

}
