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
import reactivemongo.bson.BSONValue
import scrupal.core.Scrupal
import scrupal.core.api._
import spray.can.Http
import spray.http._
import spray.httpx.marshalling._

import scala.concurrent.{Future, ExecutionContext}
import scala.util.{Try, Failure, Success}
import scala.util.control.NonFatal

trait ScrupalMarshallers extends BasicMarshallers with MetaMarshallers {

  def makeMarshallable(fr: Future[Result[_]])(implicit scrupal: Scrupal): ToResponseMarshallable = {
    scrupal.withActorExec { (as, ec, to) ⇒
      val marshaller1: ToResponseMarshaller[Result[_]] = mystery_marshaller(as, ec, to)
      val marshaller2 = futureMarshaller[Result[_]](marshaller1, ec)
      ToResponseMarshallable.isMarshallable(fr)(marshaller2)
    }
  }

  def makeMarshallable(r: Result[_])(implicit scrupal: Scrupal): ToResponseMarshallable = {
    scrupal.withActorExec { (as, ec, to) ⇒
      val marshaller1: ToResponseMarshaller[Result[_]] = mystery_marshaller(as, ec, to)
      ToResponseMarshallable.isMarshallable(r)(marshaller1)
    }
  }

  val html_ct = ContentType(MediaTypes.`text/html`, HttpCharsets.`UTF-8`)
  val text_ct = ContentType(MediaTypes.`text/plain`, HttpCharsets.`UTF-8`)

  implicit val html_marshaller: ToResponseMarshaller[HtmlResult] =
    ToResponseMarshaller.delegate[HtmlResult, String](html_ct) { h ⇒ h.payload.body }

  implicit val txt_marshaller: ToResponseMarshaller[TxtResult] =
    ToResponseMarshaller.delegate[TxtResult, String](text_ct) { h ⇒ h.payload.body }

  implicit val string_marshaller: ToResponseMarshaller[StringResult] =
    ToResponseMarshaller.delegate[StringResult, String](text_ct) { h ⇒ h.payload }

  implicit val error_marshaller: ToResponseMarshaller[ErrorResult] =
    ToResponseMarshaller.delegate[ErrorResult, String](text_ct) { e ⇒ e.formatted }

  implicit val exception_marshaller: ToResponseMarshaller[ExceptionResult] =
    ToResponseMarshaller.delegate[ExceptionResult, String](html_ct) { e ⇒ e.toHtmlResult.payload.body }

  implicit val octets_marshaller: ToResponseMarshaller[OctetsResult] =
    ToResponseMarshaller.delegate[OctetsResult, Array[Byte]](MediaTypes.`application/octet-stream`) { o ⇒ o.payload }

  implicit def stream_marshaller(ct: ContentType)(
    implicit arf: ActorRefFactory, ec: ExecutionContext, timeout: Timeout): ToResponseMarshaller[StreamResult] =
    ToResponseMarshaller.delegate[StreamResult, EnumeratorResult](ct) { s: StreamResult ⇒
      val e = Enumerator.fromStream(s.payload)
      EnumeratorResult(e, s.contentType, s.disposition)
    }(enumerator_marshaller(arf, ec, timeout))

  implicit def futureMarshaller[T](implicit m: ToResponseMarshaller[T], ec: ExecutionContext):
  ToResponseMarshaller[Future[T]] =
    ToResponseMarshaller[Future[T]] { (value, ctx) ⇒
      value.onComplete {
        case Success(v) ⇒ m(v, ctx)
        case Failure(error) ⇒ ctx.handleError(error)
      }
    }

  implicit def enumerator_marshaller(implicit arf: ActorRefFactory, ec: ExecutionContext, timeout: Timeout)
  : ToResponseMarshaller[EnumeratorResult] = {
    ToResponseMarshaller[EnumeratorResult] { (value, trmc) =>
      def consumePayload(streamActor: ActorRef): Iteratee[Array[Byte], Unit] = {
        def continue(input: Input[Array[Byte]]): Iteratee[Array[Byte], Unit] = input match {
          case Input.Empty => Cont[Array[Byte], Unit](continue)
          case Input.El(array) => Iteratee.flatten(
            (streamActor ? array) map (_ => Cont[Array[Byte], Unit](continue)))
          case Input.EOF => streamActor ! Input.EOF; Done((), Input.EOF)
        }
        Cont[Array[Byte], Unit](continue)
      }

      val streamingResponseActor: ActorRef = arf.actorOf(Props(classOf[StreamingResponseActor], value.contentType,trmc))

      value.payload |>>> consumePayload(streamingResponseActor) onFailure {
        case NonFatal(error) => streamingResponseActor ! error
      }
    }
  }

  type BSONStreamType = Try[(String,BSONValue)]

  implicit def bson_marshaller(implicit arf: ActorRefFactory, ec: ExecutionContext, timeout: Timeout):
  ToResponseMarshaller[BSONResult] = ToResponseMarshaller[BSONResult] { (value, trmc) ⇒
    def consumePayload(streamActor: ActorRef): Iteratee[BSONStreamType, Unit] = {
      def continue(input: Input[BSONStreamType]) : Iteratee[BSONStreamType, Unit] = input match {
        case Input.Empty ⇒ Cont[BSONStreamType, Unit](continue)
        case Input.EOF ⇒ streamActor ! Input.EOF; Done((), Input.EOF)
        case Input.El(data) ⇒ {
          Iteratee.flatten { (streamActor ? data ) map { _ ⇒ Cont[BSONStreamType,Unit](continue) } }
        }
      }
      Cont[BSONStreamType,Unit](continue)
    }

    val streamingResponseActor: ActorRef = arf.actorOf(Props(classOf[StreamingResponseActor], value.contentType, trmc))
    val enumerator = Enumerator.enumerate(value.payload.stream)
    enumerator |>>> consumePayload(streamingResponseActor) onFailure {
      case NonFatal(error) => streamingResponseActor ! error
    }
  }

  implicit def mystery_marshaller(
    implicit arf: ActorRefFactory, ec: ExecutionContext, timeout: Timeout): ToResponseMarshaller[Result[_]] = {
    ToResponseMarshaller[Result[_]] {
      (value, trmc) => {
        value match {
          case h: HtmlResult ⇒ html_marshaller(h, trmc)
          case s: StringResult ⇒ string_marshaller(s, trmc)
          case t: TxtResult ⇒ txt_marshaller(t, trmc)
          case e: ErrorResult ⇒ error_marshaller(e, trmc)
          case x: ExceptionResult ⇒ exception_marshaller(x, trmc)
          case o: OctetsResult ⇒ octets_marshaller(o, trmc)
          case s: StreamResult ⇒
            val m = stream_marshaller(s.contentType)
            m(s, trmc)
          case e: EnumeratorResult ⇒
            val m = enumerator_marshaller
            m(e, trmc)
          case b: BSONResult ⇒
            val m = bson_marshaller
            m(b, trmc)
        }
      }
    }
  }


}

class StreamingResponseActor(ct: ContentType, trmc: ToResponseMarshallingContext) extends Actor with
                                                                                                  ActorLogging  {
  object ChunkSent

  def bsonStreamToData(s: ScrupalMarshallers#BSONStreamType) : Array[Byte] = {
      s match {
        case Success(pair) ⇒
          // TODO: Implement this conversion
          // See reactivemongo.core.iteratees and reactivemongo.bson.buffers
          val data = new Array[Byte](1)
          data.update(0, pair._2.code)
          data
        case Failure(xcptn) ⇒ Array[Byte]() // Failed elements are not included so just push empty
      }
  }

  def receive = {
    case try_pair: ScrupalMarshallers#BSONStreamType ⇒
      context.become(
        waitingForResponder(
          trmc.startChunkedMessage(
            HttpResponse(entity=HttpEntity(ct, bsonStreamToData(try_pair))),
            ack=Some(ChunkSent)
          ),
          sender()
        )
      )
    case data: Array[Byte] =>
      context.become(
        waitingForResponder(
          trmc.startChunkedMessage(
            HttpResponse(entity = HttpEntity(ct, data)),
            ack = Some(ChunkSent)
          ),
          sender()
        )
      )
    case Input.EOF =>
      trmc.marshalTo(HttpResponse(entity = HttpEntity(ct, HttpData.Empty)))
      context.stop(self)
    case error: Throwable =>
      trmc.handleError(error)
      context.stop(self)
  }

  def waitingForData(responder: ActorRef): Actor.Receive = {
    case try_pair: ScrupalMarshallers#BSONStreamType ⇒
      responder ! MessageChunk(bsonStreamToData(try_pair)).withAck(ChunkSent)
      context.become(waitingForResponder(responder, sender()))

    case data: Array[Byte] =>
      responder ! MessageChunk(data).withAck(ChunkSent)
      context.become(waitingForResponder(responder, sender()))

    case Input.EOF =>
      responder ! ChunkedMessageEnd
      context.stop(self)

    case error: Throwable =>
      trmc.handleError(error)
      context.stop(self)
  }

  def waitingForResponder(responder: ActorRef, requestor: ActorRef): Actor.Receive = {
    case ChunkSent => {
      requestor ! ChunkSent
      context.become(waitingForData(responder))
    }

    case event: Http.ConnectionClosed => {
      log.warning(s"Response streaming stopped because: $event")
      context.stop(self)
    }
  }
}
