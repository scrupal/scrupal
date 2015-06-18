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

import reactivemongo.api.commands.bson._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random

import reactivemongo.api.indexes.Index
import reactivemongo.api._
import reactivemongo.bson._
import reactivemongo.api.commands._

import play.api.libs.iteratee.Iteratee



/** An Abstraction For Easier Access to MongoDB Collections
  * Provides a variety of useful methods assuming that the collection holds instances of `Model` and those instances
  * are indexed by `Id`.
 * A DAO defines how to work with a specific collection.
 *
 * @tparam Model The representation of a document in the collection, typically a case class that extends Storable[Id]
 * @tparam Id The type of the _id field in the document which must guarantee uniqueness.
 */
abstract class DataAccessObject[Model <: Storable[Id],Id] extends DataAccessInterface[Model,Id]
{
  import BSONValueBuilder._

  type Reader = BSONDocumentReader[Model]
  type Writer = BSONDocumentWriter[Model]

  implicit val reader : Reader
  implicit val writer : Writer
  implicit val converter : Converter

  /**
   * Returns the number of documents in this collection matching the given selector.
   *
   * @param selector Selector document which may be empty.
   */
  def count(selector: BSONDocument, limit: Int = 0)(implicit ec: ExecutionContext): Future[Int] = {
    import BSONCountCommandImplicits._
    val command = BSONCountCommand.Count(doc=selector, limit)
    collection.runCommand(command).map(_.value)
  }

  /** Retrieves at most one model matching the given selector. */
  def findOne(selector: BSONDocument)
             (implicit ec: ExecutionContext): Future[Option[Model]] = {
    collection.find(selector).one[Model]
  }

  /** Find all matching instances of Model for the given selector and return them.
   * Note that you should only do this for smallish collection. For larger connections,
    * page through them with the [[findByPage]] method.
   *
   * @param selector Document for selecting the instances
   * @param sort Document for sorting the instances, if any.
   */
  def findAll(selector: BSONDocument, sort: BSONDocument = $empty)
             (implicit ec: ExecutionContext): Future[Seq[Model]] = {
    collection.find(selector).sort(sort).cursor[Model].collect[Seq]()
  }

  /** Find matching instances of Model for the given selector and return a sorted page of them
   * This allows you to constrain the size of the result set so you don't unload too many instances into memory.
   *
   * @param selector Document for selecting the instances.
   * @param page The page number to retrieve (0-based).
   * @param pageSize Maximum number of elements in each page.
    * @param sort Document for sorting the instances, if any.
   */
  def findByPage(selector: BSONDocument, page: Int, pageSize: Int, sort: BSONDocument = $empty)
          (implicit ec: ExecutionContext): Future[Seq[Model]] = {

    collection.find(selector).sort(sort)
      .options(QueryOpts(skipN = (page-1)*pageSize, batchSizeN = pageSize))
      .cursor[Model].collect[Seq](pageSize)
  }
  /**
   * Updates and returns a single model. It returns the old document by default.
   *
   * @param selector The selection criteria for the update.
   * @param update Performs an update of the selected model.
   * @param sort Determines which model the operation updates if the query selects multiple models.
   *             findAndUpdate() updates the first model in the sort order specified by this argument.
   * @param fetchNewObject When true, returns the updated model rather than the original.
   * @param upsert When true, findAndUpdate() creates a new model if no model matches the query.
   */
  def findAndUpdate(selector: BSONDocument, update: BSONDocument, sort: BSONDocument,fetchNewObject: Boolean = true,
                    upsert: Boolean = false)
                   (implicit ec: ExecutionContext): Future[Option[Model]]= {

    import BSONFindAndModifyImplicits._
    val command = BSONFindAndModifyCommand.FindAndModify (
      query = selector, modify = BSONFindAndModifyCommand.Update(update, fetchNewObject), upsert,
      if (sort == BSONDocument.empty) None else Some(sort)
    )

    collection.runCommand(command).map( _.result[Model] )
  }

  /** Find an object, remove it, and return it.
    *
    * @param selector
    * @param sort
    * @param ec
    * @return
    */

   def findAndRemove(selector: BSONDocument, sort: BSONDocument = BSONDocument.empty)
                    (implicit ec: ExecutionContext): Future[Option[Model]] = {

    import BSONFindAndModifyImplicits._
    val command = BSONFindAndModifyCommand.FindAndModify (
      query = selector,
      modify = BSONFindAndModifyCommand.Remove,
      sort = if (sort == BSONDocument.empty) None else Some(sort)
    )
    collection.runCommand(command).map(_.result[Model])
  }

  /** Find a random instance matching the selector provided.
    * This method obtains the size of the collection and then attempts to find a random document within it
    * @param selector Document for selecting the instances
    * @param ec execution context to run in
    * @return A future to an option of the instance of the Model
    */
  def findRandom(selector: BSONDocument)(implicit ec: ExecutionContext): Future[Option[Model]] = {
    for {
      count <- count(selector)
      index = if (count == 0) 0 else Random.nextInt(count)
      random <- collection.find(selector).options(QueryOpts(skipN = index, batchSizeN = 1)).one[Model]
    } yield random
  }

  /** Inserts the given model. */
  def insert(obj: Model, writeConcern: GetLastError = defaultWriteConcern)
            (implicit ec: ExecutionContext): Future[WriteResult] = {
    collection.insert(obj, writeConcern)
  }

  /**
   * Bulk inserts multiple models.
   *
   * @param objs A collection of model objects (any traversable collection type)
   * @param bulkSize The size of the bulk insert
   * @param bulkByteSize The size in bytes of the bulk insert
   * @return The number of successful insertions.
   */
  def bulkInsert(objs: TraversableOnce[Model],
                  bulkSize: Int /*= bulk.MaxDocs*/, bulkByteSize: Int /*= bulk.MaxBulkSize*/)
                (implicit ec: ExecutionContext): Future[Int] = {
    /*
    val enumerator = Enumerator.enumerate(objs)
    collection.bulkInsert(enumerator, bulkSize, bulkByteSize) map { result ⇒
      mappedDocuments.map(lifeCycle.postPersist)
      result
    }
    */
    Future { 0 }
    ???
  }

  /**
   * Updates the documents matching the given selector.
   *
   * @param selector Selector query.
   * @param update Update query.
   * @param writeConcern Write concern which defaults to defaultWriteConcern.
   * @param upsert Create the document if it does not exist.
   * @param multi Update multiple documents.
   * @tparam U Type of the update query.
   */
  def update[U <: BSONDocumentWriter[Model]]
    (selector: Model, update: U, writeConcern: GetLastError = defaultWriteConcern,
     upsert: Boolean = false, multi: Boolean = false)
    (implicit ec: ExecutionContext, u: BSONDocumentWriter[U]): Future[WriteResult] = {
      collection.update(writer.write(selector), u.write(update), writeConcern, upsert, multi)
  }

  /**
   * Updates the document with the given `id`.
   *
   * @param id ID of the document that will be updated.
   * @param update Update query.
   * @param writeConcern Write concern which defaults to defaultWriteConcern.
   * @tparam U Type of the update query.
   */
  def updateById[U <: BSONDocumentWriter[Model]]
    (id: Id, update: U,writeConcern: GetLastError = defaultWriteConcern)
    (implicit ec: ExecutionContext, updateWriter: BSONDocumentWriter[U]): Future[WriteResult] = {
      collection.update($id(id), updateWriter.write(update), writeConcern)
  }

  /**
   * Removes model(s) matching the given selector.
   *
   * In order to remove multiple documents `firstMatchOnly` has to be `false`.
   *
   * @param selector Selector document.
   * @param writeConcern Write concern defaults to `defaultWriteConcern`.
   * @param firstMatchOnly Remove only the first matching document.
   */
  def remove(selector: BSONDocument, firstMatchOnly: Boolean = false, writeConcern: GetLastError = defaultWriteConcern)
            (implicit ec: ExecutionContext): Future[WriteResult] = {
    collection.remove(selector, writeConcern, firstMatchOnly)
  }

  /** Removes the document with the given ID. */
  def removeById(id: Id, writeConcern: GetLastError = defaultWriteConcern)
                (implicit ec: ExecutionContext): Future[WriteResult] = {
    collection.remove($id(id), writeConcern)
  }


  /** Removes all documents in this collection. */
  def removeAll(writeConcern: GetLastError = defaultWriteConcern)
               (implicit ec: ExecutionContext): Future[WriteResult] = {
    collection.remove(query = BSONDocument.empty, writeConcern = writeConcern, firstMatchOnly = false)
  }

  def drop(implicit ec: ExecutionContext): Future[Unit] = {
    collection.drop()
  }

  /**
   * Folds the documents matching the given selector by applying the function `f`.
   *
   * @param selector Selector document.
   * @param sort Sorting document.
   * @param state Initial state for the fold operation.
   * @param f Folding function.
   * @tparam A Type of fold result.
   */
  def fold[A](selector: BSONDocument, sort: BSONDocument, state: A)(f: (A, Model) ⇒ A)
             (implicit ec: ExecutionContext): Future[A] = {
    collection.find(selector).sort(sort).cursor[Model]
      .enumerate()
      .apply(Iteratee.fold(state)(f))
      .flatMap(i ⇒ i.run)

  }

  /**
   * Iterates over the documents matching the given selector and applies the function `f`.
   *
   * @param selector Selector document.
   * @param sort Sorting document.
   * @param f function to be applied.
   */
  def foreach(selector: BSONDocument, sort: BSONDocument)(f: (Model) ⇒ Unit)
             (implicit ec: ExecutionContext): Future[Unit] = {
    collection.find(selector).sort(sort).cursor[Model]
      .enumerate()
      .apply(Iteratee.foreach(f))
      .flatMap(i ⇒ i.run)
  }

  /**
   * Lists indexes that are currently ensured in this collection.
   *
   * This list may not be equal to `autoIndexes` in case of index creation failure.
   */
  def listIndices()(implicit ec: ExecutionContext): Future[List[Index]] = {
    collection.indexesManager.list()
  }

  /**
   * The list of indexes to be ensured on DAO load.
   *
   * Because of Scala initialization order there are exactly 2 ways
   * of defining auto indexes.
   *
   * First way is to use an '''early definition''':
   *
   * {{{
   * object PersonDao extends {
   *   override val autoIndexes = Seq(
   *     Index(Seq("name" -> IndexType.Ascending), unique = true, background = true),
   *     Index(Seq("age" -> IndexType.Ascending), background = true))
   * } with BsonDao[Person, BSONObjectID](MongoContext.db, "persons")
   * }}}
   *
   * Second way is to '''override def'''. Be careful __not to change declaration to `val` instead of `def`__.
   *
   * {{{
   * object PersonDao extends BsonDao[Person, BSONObjectID](MongoContext.db, "persons") {
   *
   *   override def autoIndexes = Seq(
   *     Index(Seq("name" -> IndexType.Ascending), unique = true, background = true),
   *     Index(Seq("age" -> IndexType.Ascending), background = true))
   * }
   * }}}
   */

  /** Ensures indexes defined by `autoIndexes`. */
  def ensureIndices(implicit ec: ExecutionContext): Future[Traversable[Boolean]] = {
    Future sequence {
      indices map { index ⇒
        collection.indexesManager.ensure(index)
      }
    }
  }
}

abstract class IdentifierDAO[Model <: Storable[Symbol]] extends DataAccessObject[Model,Symbol] {
  implicit val converter = (id: Symbol) ⇒ BSONString(id.name)
}
