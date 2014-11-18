/** *********************************************************************************************************************
  * This file is part of Scrupal, an Opinionated Web Application Framework.                                            *
  * *
  * © Copyright 2014 Reactific Systems, Inc. All Rights Reserved.                                                   *
  * *
  * Scrupal is free software: you can redistribute it and/or modify it under the terms                                 *
  * of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License,   *
  * or (at your option) any later version.                                                                             *
  * *
  * Scrupal is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied      *
  * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more      *
  * details.                                                                                                           *
  * *
  * You should have received a copy of the GNU General Public License along with Scrupal. If not, see either:          *
  * http://www.gnu.org/licenses or http://opensource.org/licenses/GPL-3.0.                                             *
  * *********************************************************************************************************************/

package scrupal.core

import org.specs2.mutable.Specification
import scrupal.core.api.StreamResult
import scrupal.test.FakeContext
import spray.http.MediaTypes

class TestAssetLocator extends AssetLocator {
  def asset_dirs = Seq("test/resources")
}

case class Assets(name: String) extends FakeContext[Assets](name) {

  val locator = new TestAssetLocator

}

class AssetLocatorSpec extends Specification {

  "AssetLocator" should {
    "find extension correctly" in Assets("extensions") { a : Assets ⇒
      a.locator.extensionOf("a.ext") must beEqualTo("ext")
    }

    "locate a minified resource" in Assets("minified") {a : Assets ⇒
      val result = a.locator.minifiedResource("fake.js")
      result.isDefined must beTrue
      val url = result.get
      url.getPath.endsWith("/fake.min.js") must beTrue
    }

    "local an unminified resource when minified one is present" in Assets("unminified") { a: Assets ⇒
      val result = a.locator.resourceOf("fake.js")
      result.isDefined must beTrue
      val url = result.get
      url.getPath.endsWith("fake.js") must beTrue
    }

    "find unminified resource when minified one is not present" in Assets("unminified2") { a: Assets ⇒
      val result = a.locator.minifiedResource("fake2.js")
      result.isDefined must beTrue
      val url = result.get
      url.getPath.endsWith("fake2.js") must beTrue
    }

    "obtain correct media type from file extension" in Assets("mediaType") { a: Assets ⇒
      val stream = a.locator.fetch("fake.js")
      Some(stream.mediaType) must beEqualTo(MediaTypes.forExtension("js"))
      stream match {
        case s: StreamResult ⇒
          s.payload.available() === 0
        case _ ⇒ failure("unexpected result type")
      }
      success
    }
  }


}
