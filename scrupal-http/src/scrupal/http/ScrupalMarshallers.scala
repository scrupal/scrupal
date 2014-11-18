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

package scrupal.http

import akka.actor._
import akka.pattern.ask
import akka.util.Timeout
import play.api.libs.iteratee.{Cont, Done, Iteratee, Input, Enumerator}
import scrupal.core.Scrupal
import scrupal.core.api._
import spray.can.Http
import spray.http._
import spray.httpx.marshalling._

import scala.concurrent.{Future, ExecutionContext}
import scala.util.{Failure, Success}
import scala.util.control.NonFatal

trait ScrupalMarshallers extends BasicMarshallers with MetaMarshallers {

  def makeMarshallable(fr: Future[Result[_]])(implicit scrupal: Scrupal) : ToResponseMarshallable = {
    scrupal.withActorExec { (as, ec, to) ⇒
      val marshaller1 : ToResponseMarshaller[Result[_]] = mystery_marshaller(as, ec, to)
      val marshaller2 = futureMarshaller[Result[_]](marshaller1,ec)
      ToResponseMarshallable.isMarshallable(fr)(marshaller2)
    }
  }

  def makeMarshallable(r: Result[_])(implicit scrupal: Scrupal) : ToResponseMarshallable = {
    scrupal.withActorExec { (as, ec, to) ⇒
      val marshaller1 : ToResponseMarshaller[Result[_]] = mystery_marshaller(as, ec, to)
      ToResponseMarshallable.isMarshallable(r)(marshaller1)
    }
  }

  val html_ct = ContentType(MediaTypes.`text/html`,HttpCharsets.`UTF-8`)
  val text_ct = ContentType(MediaTypes.`text/plain`,HttpCharsets.`UTF-8`)

  implicit val html_marshaller : ToResponseMarshaller[HtmlResult] = {
    ToResponseMarshaller.delegate[HtmlResult,String](html_ct) { h ⇒ h.payload.body }
  }

  implicit val text_marshaller : ToResponseMarshaller[TextResult] =
    ToResponseMarshaller.delegate[TextResult,String](text_ct) { h ⇒ h.payload }

  implicit val error_marshaller : ToResponseMarshaller[ErrorResult] =
    ToResponseMarshaller.delegate[ErrorResult,String](text_ct) { e ⇒
      s"Error: ${e.disposition.id.name}: ${e.payload}"
    }

  implicit val exception_marshaller : ToResponseMarshaller[ExceptionResult] =
    ToResponseMarshaller.delegate[ExceptionResult,String](text_ct) { e ⇒
      s"Exception: ${e.payload}"
    }

  implicit def stream_marshaller(
    implicit arf: ActorRefFactory, ec: ExecutionContext, timeout:  Timeout) : ToResponseMarshaller[StreamResult] =
    ToResponseMarshaller.delegate[StreamResult,EnumeratorResult]() { s: StreamResult ⇒
      val e = Enumerator.fromStream(s.payload)
      EnumeratorResult(e, s.mediaType, s.disposition)
    } (enumerator_marshaller(arf,ec,timeout))

  implicit def mystery_marshaller(
    implicit arf: ActorRefFactory, ec: ExecutionContext, timeout:  Timeout) : ToResponseMarshaller[Result[_]] = {
    ToResponseMarshaller[Result[_]] {
      (value, trmc) => {
        value match {
          case h: HtmlResult ⇒ html_marshaller
          case t: TextResult ⇒ text_marshaller
          case x: ExceptionResult ⇒ exception_marshaller
          case s: StreamResult ⇒ stream_marshaller
          case e: EnumeratorResult ⇒ enumerator_marshaller
        }
      }
    }
  }

  implicit def futureMarshaller[T](implicit m: ToResponseMarshaller[T], ec: ExecutionContext) :
  ToResponseMarshaller[Future[T]] =
    ToResponseMarshaller[Future[T]] { (value, ctx) ⇒
      value.onComplete {
        case Success(v)     ⇒ m(v, ctx)
        case Failure(error) ⇒ ctx.handleError(error)
      }
    }

  implicit def enumerator_marshaller(
    implicit arf: ActorRefFactory, ec: ExecutionContext, timeout:  Timeout) : ToResponseMarshaller[EnumeratorResult] =
      ToResponseMarshaller[EnumeratorResult] { (value, trmc) => {

      val responseStreamerActor: ActorRef = arf.actorOf(Props(classOf[EnumeratorMarshallingActor], value, trmc))

      def byteArrayIteratee(responseStreamerActor: ActorRef): Iteratee[Array[Byte], Unit] = {
        def continuation(input: Input[Array[Byte]]): Iteratee[Array[Byte], Unit] = input match {
          case Input.Empty => Cont[Array[Byte], Unit](continuation)
          case Input.El(array) => Iteratee.flatten((responseStreamerActor ? array) map (_ => Cont[Array[Byte], Unit](continuation)))
          case Input.EOF => responseStreamerActor ! Input.EOF; Done((), Input.EOF)
        }

        Cont[Array[Byte], Unit](continuation)
      }

      value.payload |>>> byteArrayIteratee(responseStreamerActor) onFailure {
        case NonFatal(error) => responseStreamerActor ! error
      }
    }
  }

  class EnumeratorMarshallingActor(value: EnumeratorResult, trmc: ToResponseMarshallingContext) extends Actor with
                                                                                                    ActorLogging  {
    object ChunkSent

    def receive = {
      case data: Array[Byte] =>
        context.become(
          waitingForResponder(
            trmc.startChunkedMessage(
              HttpResponse(entity = HttpEntity(value.mediaType, data)),
              ack = Some(ChunkSent)
            ),
            sender()
          )
        )
      case Input.EOF => {
        trmc.marshalTo(HttpResponse(entity = HttpEntity(value.mediaType, HttpData.Empty)))
        context.stop(self)
      }
      case error: Throwable => {
        trmc.handleError(error)
        context.stop(self)
      }
    }

    def waitingForData(responder: ActorRef): Actor.Receive = {
      case data: Array[Byte] => {
        responder ! MessageChunk(data).withAck(ChunkSent)
        context.become(waitingForResponder(responder, sender))
      }
      case Input.EOF => {
        responder ! ChunkedMessageEnd
        context.stop(self)
      }
      case error: Throwable => {
        trmc.handleError(error)
        context.stop(self)
      }
    }

    def waitingForResponder(responder: ActorRef, requestor: ActorRef): Actor.Receive = {
      case ChunkSent => {
        requestor ! ChunkSent
        context.become(waitingForData(responder))
      }

      case event: Http.ConnectionClosed => {
        log.warning("Binary response streaming stopped due to {}", event)
        context.stop(self)
      }
    }
  }
}
