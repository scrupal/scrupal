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
import scrupal.core.api._
import spray.can.Http
import spray.http._
import spray.httpx.marshalling.{ToResponseMarshallingContext, MetaMarshallers, ToResponseMarshaller, BasicMarshallers}

import scala.concurrent.ExecutionContext
import scala.util.control.NonFatal

trait ScrupalMarshallers extends BasicMarshallers with MetaMarshallers {

  val html_ct = ContentType(MediaTypes.`text/html`,HttpCharsets.`UTF-8`)
  val text_ct = ContentType(MediaTypes.`text/plain`,HttpCharsets.`UTF-8`)

  def html_marshaller : ToResponseMarshaller[HtmlResult] = {
    ToResponseMarshaller.delegate[HtmlResult,String](html_ct) { h ⇒ h.payload.body }
  }

  def text_marshaller : ToResponseMarshaller[TextResult] = {
    ToResponseMarshaller.delegate[TextResult,String](text_ct) { h ⇒ h.payload }
  }

  implicit val mystery_marshaller: ToResponseMarshaller[Result[_]] = {
    ToResponseMarshaller.delegate[Result[_], String](text_ct, html_ct) { (r : Result[_], ct) ⇒
      r match {
        case h: HtmlResult ⇒ h.payload.body
        case t: TextResult ⇒ t.payload
        case x: ExceptionResult ⇒ x.payload.toString()
      }
    }
  }

  class BinaryMarshallingActor(value: EnumeratorResult, trmc: ToResponseMarshallingContext) extends Actor with
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

  implicit def binaryResponseMarshaller(
    implicit arf: ActorRefFactory, ec: ExecutionContext, timeout:  Timeout) : ToResponseMarshaller[EnumeratorResult] =
      ToResponseMarshaller[EnumeratorResult] { (value, toResponseMarshallingContext) => {

      val responseStreamerActor: ActorRef = arf.actorOf(Props(classOf[BinaryMarshallingActor], value, toResponseMarshallingContext))

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

}
