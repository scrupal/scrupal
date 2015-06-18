/**********************************************************************************************************************
 * Copyright © 2014 Reactific Software, Inc.                                                                          *
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

package scrupal.store.reactivemongo

import reactivemongo.api.DefaultDB
import reactivemongo.bson.Macros
import reactivemongo.bson._

import scala.concurrent.{Future, Await}

/**
 * Created by reid on 11/9/14.
 */
class VariantDataAccessObjectSpec extends DBContextSpecification("VariantDataAccessObjectSpec") {

  implicit val ec = scala.concurrent.ExecutionContext.Implicits.global

  trait Noom extends VariantStorable[BSONObjectID] { val identity = "I am Noom" }

  implicit val IdentifierBSONHandler = new BSONHandler[BSONString,Symbol] {
    override def write(t: Symbol): BSONString = BSONString(t.name)
    override def read(bson: BSONString): Symbol = Symbol(bson.value)
  }

  case class Woon(_id: BSONObjectID = BSONObjectID.generate, data: String, final val kind: Symbol = 'Woon)
    extends Noom { override val identity = "I am Woon" }
  case class Twoo(_id: BSONObjectID = BSONObjectID.generate, data: Double, final val kind: Symbol = 'Twoo)
    extends Noom { override val identity = "I am Twoo" }
  case class Tree(_id: BSONObjectID = BSONObjectID.generate, data: Long, final val kind: Symbol = 'Tree)
    extends Noom { override val identity = "I am Tree" }

  implicit val WoonHandler = Macros.handler[Woon]
  implicit val TwooHandler = Macros.handler[Twoo]
  implicit val TreeHandler = Macros.handler[Tree]

  implicit val WoonReader = new VariantBSONDocumentReader[Noom] {
    def read(doc: BSONDocument) : Noom = {
      doc.getAs[BSONString]("kind") match {
        case Some(str) ⇒
          str.value match {
            case "Woon"  ⇒ WoonHandler.read(doc)
            case "Twoo"  ⇒ TwooHandler.read(doc)
            case "Tree"  ⇒ TreeHandler.read(doc)
          }
        case None ⇒ throw new Exception(s"Field 'kind' is missing from Node: ${doc.toString()}")
      }
    }
  }

  implicit val WoonWriter = new VariantBSONDocumentWriter[Noom] {
    def write(node: Noom) : BSONDocument = {
      node.kind.name match {
        case "Woon" ⇒ WoonHandler.write(node.asInstanceOf[Woon])
        case "Twoo" ⇒ TwooHandler.write(node.asInstanceOf[Twoo])
        case "Tree" ⇒ TreeHandler.write(node.asInstanceOf[Tree])
      }
    }
  }

  case class TestDao(db: DefaultDB, collectionName: String) extends VariantDataAccessObject[Noom,BSONObjectID] {
    val writer = new Writer(WoonWriter)
    val reader = new Reader(WoonReader)
    val converter = (id: BSONObjectID) ⇒ id
  }

  "VariantDataAccessObject" should {
    "create new collection" in {
      withEmptyDB("test_newCollection") { db ⇒
        val dao = new TestDao(db, "testdata")
        val future = dao.ensureIndices.map { list ⇒ list.exists( p ⇒ p) }
        Await.result(future, timeout) must beTrue
      }
    }

    "drop collections" in {
      withEmptyDB("test_dropCollection") { db ⇒
        val dao = new TestDao(db, "testdata")
        val future = dao.ensureIndices.flatMap { list ⇒
          list.exists(p ⇒ p) must beTrue
          dao.drop.flatMap { _ ⇒ db.hasCollection("testdata") }
        }
        Await.result(future, timeout) must beFalse
      }
    }

    "insert and find items" in {
      withEmptyDB("test_insertFind") { db ⇒
        val dao = new TestDao(db, "testdata")
        val future = dao.ensureIndices.flatMap { list ⇒
          list.exists(p ⇒ p) must beTrue
          val n1 = Woon(data="42")
          val i1 = dao.insert(n1) flatMap { wr ⇒
            wr.hasErrors must beFalse
            dao.fetch(n1._id) map { optObj ⇒
              optObj.isDefined must beTrue
              optObj.get._id must beEqualTo(n1._id)
              optObj.get.isInstanceOf[Woon] must beTrue
              optObj.get.asInstanceOf[Woon].data must beEqualTo("42")
            }
          }
          val n2 = Twoo(data=42.0)
          val i2 = dao.insert(n2) flatMap { wr ⇒
            wr.hasErrors must beFalse
            dao.fetch(n2._id) map { optObj ⇒
              optObj.isDefined must beTrue
              optObj.get._id must beEqualTo(n2._id)
              optObj.get.isInstanceOf[Twoo] must beTrue
              optObj.get.asInstanceOf[Twoo].data must beEqualTo(42.0)
            }
          }
          val n3 = Tree(data=84)
          val i3 = dao.insert(n3) flatMap { wr ⇒
            wr.hasErrors must beFalse
            dao.fetch(n3._id) map { optObj ⇒
              optObj.isDefined must beTrue
              optObj.get._id must beEqualTo(n3._id)
              optObj.get.isInstanceOf[Tree] must beTrue
              optObj.get.asInstanceOf[Tree].data must beEqualTo(84)
            }
          }
          Future sequence List(i1,i2,i3)
        }
        val result = future map { list ⇒
          list.foldLeft(true) { (prev,next) ⇒ prev && next }
        }
        Await.result(result, timeout) must beTrue
      }
    }
  }
}

