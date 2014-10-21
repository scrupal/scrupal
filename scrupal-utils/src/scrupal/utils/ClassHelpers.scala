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

package scrupal.utils

/**
 * One line sentence description here.
 * Further description here.
 */
object ClassHelpers {

  implicit def string2Class[T<:AnyRef](name: String)(implicit classLoader: ClassLoader = ClassHelpers.getClass.getClassLoader): Class[T] = {
    Class.forName(name, true, classLoader).asInstanceOf[Class[T]]
  }

  implicit def class2instance[T<:AnyRef](clazz: Class[T]) : T = clazz.newInstance()

  implicit def string2instance[T<:AnyRef](name:String) : T = class2instance(string2Class(name))

}
