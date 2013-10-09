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

package scrupal.api

import play.api.libs.json.{JsResult, JsValue}

/** A trait to define the transform method for objects that can transform a JsValue
  * Transformers implement the transform method to transform JSON values from one shape to another
  */
trait Transformer {
  /** Transform a JSON value into another JSON value
    * Abstract method definition for transformers to transform JSON values
    * @param value The JSON value to be transformed
    * @return JsSuccess(JsValue) when successful, JsError when not
    */
  def transform(value: JsValue) : JsResult[JsValue]
}
