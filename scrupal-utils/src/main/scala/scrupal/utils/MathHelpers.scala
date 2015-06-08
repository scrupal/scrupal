/** ********************************************************************************************************************
  * This file is part of Scrupal, a Scalable Reactive Content Management System.                                       *
  *                                                                                                                  *
  * Copyright © 2015 Reactific Software LLC                                                                            *
  *                                                                                                                  *
  * Licensed under the Apache License, Version 2.0 (the "License");  you may not use this file                         *
  * except in compliance with the License. You may obtain a copy of the License at                                     *
  *                                                                                                                  *
  *      http://www.apache.org/licenses/LICENSE-2.0                                                                  *
  *                                                                                                                  *
  * Unless required by applicable law or agreed to in writing, software distributed under the                          *
  * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,                          *
  * either express or implied. See the License for the specific language governing permissions                         *
  * and limitations under the License.                                                                                 *
  * ********************************************************************************************************************
  */

package scrupal.utils

/** One line sentence description here.
  * Further description here.
  */
object MathHelpers {
  def log2(num : Long) : Int = {
    if (num <= 0)
      throw new IllegalArgumentException("log2 requires a positive argument")
    else
      63 - java.lang.Long.numberOfLeadingZeros(num)
  }

  def log2(num : Int) : Int = {
    if (num <= 0)
      throw new IllegalArgumentException("log2 requires a positive argument")
    else
      31 - java.lang.Integer.numberOfLeadingZeros(num)
  }
}
