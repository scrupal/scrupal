/**********************************************************************************************************************
 * This file is part of Scrupal, a Scalable Reactive Content Management System.                                       *
 *                                                                                                                    *
 * Copyright © 2015 Reactific Software LLC                                                                            *
 *                                                                                                                    *
 * Licensed under the Apache License, Version 2.0 (the "License");  you may not use this file                         *
 * except in compliance with the License. You may obtain a copy of the License at                                     *
 *                                                                                                                    *
 *        http://www.apache.org/licenses/LICENSE-2.0                                                                  *
 *                                                                                                                    *
 * Unless required by applicable law or agreed to in writing, software distributed under the                          *
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,                          *
 * either express or implied. See the License for the specific language governing permissions                         *
 * and limitations under the License.                                                                                 *
 **********************************************************************************************************************/

package scrupal.store.reactivemongo

import reactivemongo.api.DefaultDB
import reactivemongo.bson._

import scala.concurrent.{Future, Await}

class VariantRegistrySpec extends DBContextSpecification("VariantRegistrySpec") {

  implicit val ec = scala.concurrent.ExecutionContext.Implicits.global

  trait Noom extends VariantStorable[BSONObjectID] {
    val identity = "I am Noom"
  }

  object variants extends VariantRegistry[Noom]("Noom")

  implicit val IdentifierBSONHandler = new BSONHandler[BSONString,Symbol] {
    override def write(t: Symbol): BSONString = BSONString(t.name)
    override def read(bson: BSONString): Symbol = Symbol(bson.value)
  }

  case class Woon(_id: BSONObjectID = BSONObjectID.generate, data: String, final val kind: Symbol = 'Woon)
    extends Noom { override val identity = "I am Woon" }
  case class Twoo(_id: BSONObjectID = BSONObjectID.generate, data: Double, final val kind: Symbol = 'Twoo)
    extends Noom { override val identity = "I am Twoo" }
  case class Tree(_id: BSONObjectID = BSONObjectID.generate, data: Long,   final val kind: Symbol = 'Tree)
    extends Noom { override val identity = "I am Tree" }

  object WoonVRW extends VariantReaderWriter[Noom, Woon] {
    implicit val WoonHandler = Macros.handler[Woon]
    def fromDoc(doc: BSONDocument) : Woon = WoonHandler.read(doc)
    def toDoc(obj: Noom) : BSONDocument = WoonHandler.write(obj.asInstanceOf[Woon])
  }
  variants.register('Woon, WoonVRW)

  object TwooVRW extends VariantReaderWriter[Noom, Twoo] {
    implicit val TwooHandler = Macros.handler[Twoo]
    def fromDoc(doc: BSONDocument) : Twoo = TwooHandler.read(doc)
    def toDoc(obj: Noom) : BSONDocument = TwooHandler.write(obj.asInstanceOf[Twoo])
  }
  variants.register('Twoo, TwooVRW)

  object TreeVRW extends VariantReaderWriter[Noom, Tree] {
    implicit val TreeHandler = Macros.handler[Tree]
    def fromDoc(doc: BSONDocument) : Tree = TreeHandler.read(doc)
    def toDoc(obj: Noom) : BSONDocument = TreeHandler.write(obj.asInstanceOf[Tree])
  }
  variants.register('Tree, TreeVRW)

  implicit val WoonReader = new VariantBSONDocumentReader[Noom] {
    def read(doc: BSONDocument) : Noom = variants.read(doc)
  }

  implicit val WoonWriter = new VariantBSONDocumentWriter[Noom] {
    def write(node: Noom) : BSONDocument = variants.write(node)
  }

  case class TestDao(db: DefaultDB, collectionName: String) extends VariantDataAccessObject[Noom,BSONObjectID] {
    val writer = new Writer(variants)
    val reader = new Reader(variants)
    val converter = (id: BSONObjectID) ⇒ id
  }

  "VariantRegistryDAO" should {
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


