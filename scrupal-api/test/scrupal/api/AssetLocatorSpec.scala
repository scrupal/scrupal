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

package scrupal.api

import java.io.File

import scrupal.test.{FakeContext, ScrupalSpecification}
import spray.http.MediaTypes

class TestAssetLocator extends AssetLocator {
  def assets_path = Seq("test/resources")
}

class AssetLocatorSpec extends ScrupalSpecification("AssetLocatorSpec") {
sequential
  case class Assets(name: String) extends FakeContext[Assets](name) {

    val locator = new TestAssetLocator

  }

  "AssetLocator" should {
    "find extension correctly" in Assets("extensions") { a : Assets ⇒
      a.locator.extensionOf("a.ext") must beEqualTo("ext")
    }

    "locate a minified resource" in Assets("minified") {a : Assets ⇒
      val result = a.locator.minifiedResourceOf("fake.js")
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
      val result = a.locator.minifiedResourceOf("fake2.js")
      result.isDefined must beTrue
      val url = result.get
      url.getPath.endsWith("fake2.js") must beTrue
    }

    "obtain correct media type from file extension" in Assets("mediaType") { a: Assets ⇒
      val stream = a.locator.fetch("fake.js")
      Some(stream.contentType.mediaType) must beEqualTo(MediaTypes.forExtension("js"))
      stream match {
        case s: StreamResult ⇒
          s.payload.available() === 0
        case _ ⇒ failure("unexpected result type")
      }
      success
    }
  }

  "isFile" should {
    val locator = new TestAssetLocator
    "return false for null" in {
      locator.isFile(null) must beFalse
    }
    "return true for a file" in {
      val isFile = locator.resourceOf("fake.js") map { url ⇒ locator.isFile(url) }
      isFile.isDefined must beTrue
      isFile.get must beTrue
    }
    "return false for a class known to be in a jar" in {
      val isFile = locator.resourceOf("java/lang/Integer.class") map { url ⇒ locator.isFile(url) }
      isFile.isDefined must beTrue
      isFile.get must beFalse
    }
  }
  "isJar" should {
    val locator = new TestAssetLocator
    "return false for null" in {
      locator.isJar(null) must beFalse
    }
    "return false for a file" in {
      val isJar = locator.resourceOf("fake.js") map { url ⇒ locator.isJar(url) }
      isJar.isDefined must beTrue
      isJar.get must beFalse
    }
    "return true for a class known to be in a jar" in {
      val isJar = locator.resourceOf("java/lang/Integer.class") map { url ⇒ locator.isJar(url) }
      isJar.isDefined must beTrue
      isJar.get must beTrue
    }
  }

  "everythingUnder" should {
    val locator = new TestAssetLocator
    "give us a list of the files under resources"
  }

  "isNonRecursiveDirectory" should {
    val locator = new TestAssetLocator
    "return false for null" in {
      locator.isNonRecursiveDirectory(null.asInstanceOf[File]) must beFalse
    }
    "return false for ." in {
      val isNRD = locator.resourceOf(".") map { url ⇒ locator.isNonRecursiveDirectory(url) }
      isNRD.isDefined must beTrue
    }
    "return false for .." in {
      val isNRD = locator.resourceOf("..") map { url ⇒ locator.isNonRecursiveDirectory(url) }
      isNRD.isDefined must beFalse
    }
    "return false for root/up (symlink back to root)" in {
      val isNRD = locator.resourceOf("root/up") map { url ⇒ locator.isNonRecursiveDirectory(url) }
      isNRD.isDefined must beFalse
    }
    "return false for root/up/flat (symlink back to root)" in {
      val isNRD = locator.resourceOf("root/up/flat") map { url ⇒ locator.isNonRecursiveDirectory(url) }
      isNRD.isDefined must beFalse
    }
    "return false for a file in a directory" in {
      val isNRD = locator.resourceOf("root/foo.txt") map { url ⇒ locator.isNonRecursiveDirectory(url) }
      isNRD.isDefined must beFalse
    }
    "return true for 'root/.'" in {
      val url = locator.resourceOf("root/.")
      url.isDefined must beTrue
      val isNRD = locator.isNonRecursiveDirectory(url.get)
      isNRD.isDefined must beTrue
    }
  }
}
