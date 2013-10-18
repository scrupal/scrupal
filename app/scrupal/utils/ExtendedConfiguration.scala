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

import play.api.{Logger, Configuration}

/**
 * Provide some extentions to the Play Configuration class via the pimp-my-library pattern
 * Further description here.
 */
class ExtendedConfiguration(config : Configuration) {

  import ClassHelpers._

  /** Convert any class name into an instance of that class, assuming it has an empty args constructor
    *
    * @param name The class name
    * @param m A manifest for the class
    * @tparam C The kind of class expected, a base class
    * @return An instance of C that is of class `name` or None if it couldn't be instantiated.
    */
  def getInstance[C<:AnyRef](name: String)(implicit m: Manifest[C]) : Option[C] = {
    try
    {
      Option( string2instance[C](name))
    }
    catch {
      case x: IllegalAccessException =>  { Logger.error("Cannot access class " + name + " while instantiating: ", x); None }
      case x: InstantiationException =>  { Logger.error("Cannot instantiate uninstantiable class " + name + ": ", x); None }
      case x: ExceptionInInitializerError =>  { Logger.error("Instance initialization of " + name + " failed: ", x); None }
      case x: SecurityException => { Logger.error("Security exception while instantiating " + name + ": ", x);  None }
      case x: LinkageError => { Logger.error("Linkage error while instantiating " + name + ": ", x); None }
      case x: ClassNotFoundException =>  { Logger.error("Cannot find class " + name + " to instantiate: ", x); None }
      case x: Throwable  => { throw x }
    }
  }
}

object ExtendedConfiguration
{
  implicit def extendYoConfiguration(config: Configuration) = new ExtendedConfiguration(config)
}
