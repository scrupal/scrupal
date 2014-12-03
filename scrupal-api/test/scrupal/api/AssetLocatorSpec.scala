/***********************************************************************************************************************
  * Copyright © 2014 Reactific Software LLC.                                                                           *
  *                                                                                                                    *
  * This file is part of Scrupal, an Opinionated Web Application Framework.                                            *
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

import java.io.File

import com.typesafe.config.ConfigFactory
import scrupal.test.{FakeContext, ScrupalSpecification}
import scrupal.utils.{OSSLicense, Configuration}
import spray.http.MediaTypes

class TestAssetLocator(config: Configuration) extends ConfiguredAssetsLocator(config) {
  override def assets_path = super.assets_path ++ Seq("scrupal-api/test/resources")
}

class AssetLocatorSpec extends ScrupalSpecification("AssetLocatorSpec") {
sequential
  case class Assets(name: String) extends FakeContext[Assets](name) {

    val locator = new TestAssetLocator(scrupal._configuration)

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
    val assets = new Assets("isFile")
    val locator = assets.locator
    "return false for null" in  {
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
    val assets = new Assets("isJar")
    val locator = assets.locator
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

  "fetchDirectory" should {
    val assets = new Assets("fetchDirectory")
    val locator = assets.locator
    "grok the test directory" in {
      val dirOpt = locator.fetchDirectory("root")
      dirOpt.isDefined must beTrue
      val dir = dirOpt.get
      val cfg = Configuration(ConfigFactory.parseFile(new File("scrupal-api/test/resources/root/__dir.conf")))
      dir.author must beEqualTo(cfg.getString("author"))
      dir.title must beEqualTo(cfg.getString("title"))
      dir.copyright must beEqualTo(cfg.getString("copyright"))
      dir.license must beEqualTo(OSSLicense.lookup(Symbol(cfg.getString("license").getOrElse(""))))
      dir.description must beEqualTo(cfg.getString("description"))
      val direct = locator.resourceOf("root/foo.txt")
      direct.isDefined must beTrue
      val optB = dir.files.get("foo.txt").map { x ⇒ x._2.isDefined }
      optB.isDefined must beTrue
      optB.get must beTrue
     }

    "grok recursing directories" in {
      val dirOpt = locator.fetchDirectory("root", recurse=true)
      dirOpt.isDefined must beTrue
      val dir : AssetLocator.Directory = dirOpt.get
      val dirs = dir.dirs
      val empty = dirs.get("empty")
      empty.isDefined must beTrue
      empty.get.isDefined must beFalse
      val nothere = dirs.get("nothere")
      nothere.isDefined must beTrue
      nothere.get.isDefined must beFalse
      val subrootOptOpt = dirs.get("subroot")
      subrootOptOpt.isDefined must beTrue
      val subrootOpt = subrootOptOpt.get
      subrootOpt.isDefined must beTrue
      val subroot = subrootOpt.get
      subroot.title.isDefined must beFalse
      subroot.author.isDefined must beFalse
      subroot.files.isEmpty must beTrue
      subroot.dirs.isEmpty must beTrue
    }
  }

}
