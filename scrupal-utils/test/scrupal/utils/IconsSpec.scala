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

import org.specs2.mutable.Specification

/**
 * One line sentence description here.
 * Further description here.
 */
class IconsSpec extends Specification
{
	"icons" should {
		"return correct html for Icons.heart" in {
			Icons.html(Icons.heart).toString must beEqualTo("<i class=\"icon-heart\"></i>")
		}
		"return correct html for Icons.long_arrow_left" in {
			Icons.html(Icons.long_arrow_left).toString must beEqualTo("<i class=\"icon-long-arrow-left\"></i>")
		}
	}
}
