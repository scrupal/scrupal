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

import scala.util.{ Failure, Success, Try }

/** Try With Resources
  *
  * This works just like Try[T] with the following exceptions:
  * - Instead of catching only NonFatal exceptions, catches all Throwables so resources are sure to be released
  * - Accepts a Seq[AutoCloseable] list of resources to close on exit from the Try
  *
  */
object TryWith {

  /** Try With Resources
    * This works just like Try[T] with the following exceptions:
    * - Instead of catching only NonFatal exceptions, catches all Throwables so resources are sure to be released
    * - Accepts a Seq[AutoCloseable] list of resources to close on exit from the Try
    *
    * @param resources The resources to be closed
    * @tparam T The
    * @return Success[T] when successful or Failure[Throwable] when an exceptions is thrown
    */
  def apply[T](resources : ⇒ Seq[AutoCloseable])(r : (Seq[AutoCloseable]) ⇒ T) : Try[T] = {
    try {
      Success(r(resources))
    } catch {
      case x : Throwable ⇒ Failure(x)
    } finally {
      for (r ← resources) { r.close() }
    }
  }

  def apply[R <: AutoCloseable, T](resource : R)(r : R ⇒ T) : Try[T] = {
    try {
      Success(r(resource))
    } catch {
      case x : Throwable ⇒ Failure(x)
    } finally {
      resource.close()
    }
  }
}
