/**********************************************************************************************************************
 * This file is part of Scrupal, a Scalable Reactive Web Application Framework for Content Management                 *
 *                                                                                                                    *
 * Copyright (c) 2015, Reactific Software LLC. All Rights Reserved.                                                   *
 *                                                                                                                    *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance     *
 * with the License. You may obtain a copy of the License at                                                          *
 *                                                                                                                    *
 *     http://www.apache.org/licenses/LICENSE-2.0                                                                     *
 *                                                                                                                    *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed   *
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for  *
 * the specific language governing permissions and limitations under the License.                                     *
 **********************************************************************************************************************/

package scrupal.api

import akka.http.scaladsl.model.{HttpMethod, HttpMethods}
import org.joda.time.DateTime
import scrupal.storage.api.Storable
import scrupal.utils._

/** A Scrupal Application
  * Applications are a fundamental unit of organization in Scrupal. A Site defines the set of applications that are to
  * run on that site. Each application gets a top level context and configures which modules are relevant for it
  * Created by reid on 11/6/14.
  */
abstract class Application(ident  : Identifier)(implicit scrpl : Scrupal) extends {
  val id : Identifier = ident
  implicit val scrupal: Scrupal = scrpl
} with EnablementProvider[Application]
  with Storable with Registrable[Application] with Authorable with Nameable with Describable with Modifiable {

  def registry : Registry[Application] = scrupal.Applications

  /** Applicable Modules
    * This modules are assigned (enabled) within this application
    */
  def modules = forEach[Module] { e ⇒ e.isInstanceOf[Module] && isEnabled(e, this) } { e ⇒ e.asInstanceOf[Module] }

  def entities = forEach[Entity] { e ⇒ e.isInstanceOf[Entity] && isEnabled(e, this) } { e ⇒ e.asInstanceOf[Entity] }

  def isChildScope(e : Enablement[_]) : Boolean = entities.contains(e)

}

case class BasicApplication(
  sym : Identifier,
  name : String,
  author : String,
  copyright : String,
  license: OSSLicense,
  description : String,
  modified : Option[DateTime] = None,
  created : Option[DateTime] = None
)(implicit scrupal : Scrupal) extends Application(sym)(scrupal) {
  final val method : HttpMethod = HttpMethods.GET
  final val kind = 'BasicApplication
}

case class ApplicationsRegistry() extends Registry[Application] {
  import scala.language.reflectiveCalls

  def registryName = "Applications"
  def registrantsName = "application"

  private[this] val _bypath = new AbstractRegistry[String, Application] {
    def reg(app : Application) = _register(app.label, app)
    def unreg(app : Application) = _unregister(app.label)
  }

  override def register(app : Application) : Unit = {
    _bypath.reg(app)
    super.register(app)
  }

  override def unregister(app : Application) : Unit = {
    _bypath.unreg(app)
    super.unregister(app)
  }

  def forPath(path : String) = _bypath.lookup(path)

  /*
  implicit lazy val ApplicationReader = new VariantBSONDocumentReader[Application] {
    def read(doc : BSONDocument) : Application = {
      doc.getAs[BSONString]("kind") match {
        case Some(str) ⇒
          str.value match {
            case "Basic" ⇒ BasicApplication.BasicApplicationHandler.read(doc)
            case _ ⇒ toss(s"Unknown kind of Application: '${str.value}")
          }
        case None ⇒ toss(s"Field 'kind' is missing from Node: ${doc.toString()}")
      }
    }
  }

  implicit val ApplicationWriter = new VariantBSONDocumentWriter[Application] {
    def write(app : Application) : BSONDocument = {
      app.kind match {
        case 'Basic ⇒ BasicApplication.BasicApplicationHandler.write(app.asInstanceOf[BasicApplication])
        case _ ⇒ toss(s"Unknown kind of Application: ${app.kind}")
      }
    }
  }

  /** Data Access Object For Applications
    * This DataAccessObject sublcass represents the "applications" collection in the database and permits management of
    * that collection as well as conversion to and from BSON format.
    * @param db A [[reactivemongo.api.DefaultDB]] instance in which to find the collection
    */

  case class ApplicationDAO(db : DefaultDB) extends VariantIdentifierDAO[Application] {
    final def collectionName : String = "applications"
    implicit val writer = new Writer(ApplicationWriter)
    implicit val reader = new Reader(ApplicationReader)

    override def indices : Traversable[Index] = super.indices ++ Seq(
      Index(key = Seq("path" -> IndexType.Ascending), name = Some("path")),
      Index(key = Seq("kind" -> IndexType.Ascending), name = Some("kind"))
    )
  }
  */
}
