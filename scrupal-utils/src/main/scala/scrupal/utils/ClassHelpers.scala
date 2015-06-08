/** ********************************************************************************************************************
  * This file is part of Scrupal, a Scalable Reactive Content Management System.                                       *
  *                                                                                                                 *
  * Copyright © 2015 Reactific Software LLC                                                                            *
  *                                                                                                                 *
  * Licensed under the Apache License, Version 2.0 (the "License");  you may not use this file                         *
  * except in compliance with the License. You may obtain a copy of the License at                                     *
  *                                                                                                                 *
  *     http://www.apache.org/licenses/LICENSE-2.0                                                                  *
  *                                                                                                                 *
  * Unless required by applicable law or agreed to in writing, software distributed under the                          *
  * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,                          *
  * either express or implied. See the License for the specific language governing permissions                         *
  * and limitations under the License.                                                                                 *
  * ********************************************************************************************************************
  */

package scrupal.utils

import scala.language.implicitConversions

/** One line sentence description here.
  * Further description here.
  */
object ClassHelpers {

  implicit def string2Class[T <: AnyRef](name : String)(implicit classLoader : ClassLoader = ClassHelpers.getClass.getClassLoader) : Class[T] = {
    Class.forName(name, true, classLoader).asInstanceOf[Class[T]]
  }

  implicit def class2instance[T <: AnyRef](clazz : Class[T]) : T = clazz.newInstance()

  implicit def string2instance[T <: AnyRef](name : String) : T = class2instance(string2Class(name))

}
