package scrupal.core.echo

import akka.actor.{Props, ActorLogging, Actor}
import reactivemongo.bson.BSONDocument
import scrupal.core.BundleType
import scrupal.core.api._
import scrupal.utils.OSSLicense

/** The Echo Entity
  * This is really the heart of the EchoApp. All the requests that get echoed go through here.
  */

object EchoEntity extends Entity {

  def id: Symbol = 'Echo

  def kind: Symbol = 'Echo

  def path: String = ""

  def instanceType: BundleType = BundleType.Empty

  def author: String = "Reid Spencer"

  def copyright: String = "© 2014, 2015 Reid Spencer. All Rights Reserved."

  def license: OSSLicense = OSSLicense.GPLv3

  def description: String = "An entity that stores nothing and merely echos its requests"

  override protected val worker =
    system.actorOf(Props(classOf[EchoWorker], this), "EchoWorker")

  class EchoWorker extends Actor with ActorLogging {
    def receive : Receive = {
      // TODO: Implement Entity.receive to process messages
      case a: Action[_, _] =>
      case Create(id: String, instance: BSONDocument, ctxt: Context) =>
      case Retrieve(id: String, ctxt: Context) =>
      case Update(id: String, fields: BSONDocument, ctxt: Context) =>
      case Delete(id: String, ctxt: Context) =>
      case Query(fields: BSONDocument, ctxt: Context) =>
      case Option(id: String, option: String, ctxt: Context) =>
      case Get(id: String, what: String, data: BSONDocument, ctxt: Context) =>
      case Put(id: String, what: String, data: BSONDocument, ctxt: Context) =>
      case AddFacet(id: String, name: String, facet: Facet, ctxt: Context) =>
    }
  }


}
