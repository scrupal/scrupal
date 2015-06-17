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

package scrupal.storage.api

import scrupal.utils.ScrupalComponent

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

sealed trait WriteResult {
  def isSuccess : Boolean;
  def isFailure : Boolean = !isSuccess
  def tossOnError : Unit = {
    if (isFailure)
      WriteResult.toss(s"Write operation failed: $this")
  }
}

case class WriteSuccess() extends WriteResult { val isSuccess = true }
case class WriteFailure(failure : Throwable) extends WriteResult { val isSuccess = false }
case class WriteError(error : String) extends WriteResult { val isSuccess = false }
case class WriteResults(results: Iterable[WriteResult]) extends WriteResult {
  def isSuccess = {
    results.isEmpty || results.forall(_.isSuccess)
  }
}

object WriteResult extends ScrupalComponent {
  def failure(x : Throwable) : WriteResult = { WriteFailure(x) }
  def error(x : String) : WriteResult = { WriteError(x) }
  def success() : WriteResult = WriteSuccess()
  def coalesce(results: Iterable[Future[WriteResult]]) : Future[WriteResult] = {
    val invert = Future sequence results
    invert map { seq ⇒
      WriteResults(seq filter { _.isSuccess })
    }
  }
}
