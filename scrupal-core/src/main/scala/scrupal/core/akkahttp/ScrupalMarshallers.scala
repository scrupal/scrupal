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

package scrupal.core.akkahttp

import akka.actor._
import akka.http.scaladsl.marshalling._
import akka.http.scaladsl.marshalling.ToResponseMarshaller
import akka.http.scaladsl.model._
import akka.pattern.ask
import akka.util.Timeout
import play.api.libs.iteratee.{Cont, Done, Enumerator, Input, Iteratee}
import scrupal.api._

import scala.concurrent.{ExecutionContext, Future}
import scala.language.implicitConversions
import scala.util.control.NonFatal
import scala.util.{Failure, Success}

trait ScrupalMarshallers extends GenericMarshallers {

  /* FIXME: Reinstate ScrupalMarshallers
  def makeMarshallable(fr : Future[Response])(implicit scrupal : Scrupal) : ToResponseMarshallable = {
    scrupal.withActorExec { (as, ec, to) ⇒
      val marshaller1 : ToResponseMarshaller[Response] = mystery_marshaller(as, ec, to)
      val marshaller2 = futureMarshaller[Response](marshaller1, ec)
      ToResponseMarshallable.isMarshallable(fr)(marshaller2)
    }
  }

  def makeMarshallable(r : Response[_])(implicit scrupal : Scrupal) : ToResponseMarshallable = {
    scrupal.withActorExec { (as, ec, to) ⇒
      val marshaller1 : ToResponseMarshaller[Response] = mystery_marshaller(as, ec, to)
      ToResponseMarshallable.isMarshallable(r)(marshaller1)
    }
  }

  val html_ct = ContentType(MediaTypes.`text/html`, HttpCharsets.`UTF-8`)
  val text_ct = ContentType(MediaTypes.`text/plain`, HttpCharsets.`UTF-8`)

  implicit val html_marshaller : ToResponseMarshaller[HtmlResponse] =
    ToResponseMarshallable.marshaller.delegate[HtmlResponse, String](html_ct) { h ⇒ h.payload.toString() }

  implicit val string_marshaller : ToResponseMarshaller[StringResponse] =
    ToResponseMarshaller.delegate[StringResponse, String](text_ct) { h ⇒ h.payload }

  implicit def octets_marshaller(ct : ContentType)(
    implicit arf : ActorRefFactory, ec : ExecutionContext, timeout : Timeout) : ToResponseMarshaller[OctetsResponse] =
    ToResponseMarshaller.delegate[OctetsResponse, EnumeratorResponse](ct) { or : OctetsResponse ⇒ or() } (enumerator_marshaller(arf, ec, timeout))

  implicit def stream_marshaller(ct : ContentType)(
    implicit arf : ActorRefFactory, ec : ExecutionContext, timeout : Timeout) : ToResponseMarshaller[StreamResponse] =
    ToResponseMarshaller.delegate[StreamResponse, EnumeratorResponse](ct) { s : StreamResponse ⇒ s() } (enumerator_marshaller(arf, ec, timeout))

  implicit def futureMarshaller[T](implicit m : ToResponseMarshaller[T], ec : ExecutionContext) : ToResponseMarshaller[Future[T]] =
    ToResponseMarshaller[Future[T]] { (value, ctx) ⇒
      value.onComplete {
        case Success(v)     ⇒ m(v, ctx)
        case Failure(error) ⇒ ctx.handleError(error)
      }
    }

  implicit def enumerator_marshaller(implicit arf : ActorRefFactory, ec : ExecutionContext, timeout : Timeout) : ToResponseMarshaller[EnumeratorResponse] = {
    ToResponseMarshaller[EnumeratorResponse] { (value, trmc) ⇒
        def consumePayload(streamActor : ActorRef) : Iteratee[Array[Byte], Unit] = {
            def continue(input : Input[Array[Byte]]) : Iteratee[Array[Byte], Unit] = input match {
              case Input.Empty ⇒ Cont[Array[Byte], Unit](continue)
              case Input.El(array) ⇒ Iteratee.flatten(
                (streamActor ? array) map (_ ⇒ Cont[Array[Byte], Unit](continue)))
              case Input.EOF ⇒ streamActor ! Input.EOF; Done((), Input.EOF)
            }
          Cont[Array[Byte], Unit](continue)
        }

      val streamingResponseActor : ActorRef = arf.actorOf(Props(classOf[StreamingResponseActor], value.contentType, trmc))

      value.payload |>>> consumePayload(streamingResponseActor) onFailure {
        case NonFatal(error) ⇒ streamingResponseActor ! error
      }
    }
  }

  type BSONStreamType = (String, BSONValue)

  implicit def bson_marshaller(implicit arf : ActorRefFactory, ec : ExecutionContext, timeout : Timeout) : ToResponseMarshaller[BSONResult] = ToResponseMarshaller[BSONResult] { (value, trmc) ⇒
      def consumePayload(streamActor : ActorRef) : Iteratee[BSONStreamType, Unit] = {
          def continue(input : Input[BSONStreamType]) : Iteratee[BSONStreamType, Unit] = input match {
            case Input.Empty ⇒ Cont[BSONStreamType, Unit](continue)
            case Input.EOF   ⇒
              streamActor ! Input.EOF; Done((), Input.EOF)
            case Input.El(data) ⇒ {
              Iteratee.flatten { (streamActor ? data) map { _ ⇒ Cont[BSONStreamType, Unit](continue) } }
            }
          }
        Cont[BSONStreamType, Unit](continue)
      }

    val streamingResponseActor : ActorRef = arf.actorOf(Props(classOf[StreamingResponseActor], value.contentType, trmc))
    val enumerator = Enumerator.enumerate(value.payload.stream).map {
      case Success(pair)  ⇒ pair
      case Failure(xcptn) ⇒ ("", BSONNull)
    }
    enumerator |>>> consumePayload(streamingResponseActor) onFailure {
      case NonFatal(error) ⇒ streamingResponseActor ! error
    }
  }

  def disposition2StatusCode(disposition : Disposition) : StatusCode = {
    disposition match {
      case Successful ⇒ StatusCodes.OK
      case Received ⇒ StatusCodes.Accepted
      case Pending ⇒ StatusCodes.OK
      case Promise ⇒ StatusCodes.OK
      case Unspecified ⇒ StatusCodes.InternalServerError
      case TimedOut ⇒ StatusCodes.GatewayTimeout
      case Unintelligible ⇒ StatusCodes.BadRequest
      case Unimplemented ⇒ StatusCodes.NotImplemented
      case Unsupported ⇒ StatusCodes.NotImplemented
      case Unauthorized ⇒ StatusCodes.Unauthorized
      case Unavailable ⇒ StatusCodes.ServiceUnavailable
      case Unacceptable ⇒ StatusCodes.NotAcceptable
      case NotFound ⇒ StatusCodes.NotFound
      case Ambiguous ⇒ StatusCodes.Conflict
      case Conflict ⇒ StatusCodes.Conflict
      case TooComplex ⇒ StatusCodes.Forbidden
      case Exhausted ⇒ StatusCodes.ServiceUnavailable
      case Exception ⇒ StatusCodes.InternalServerError
      case _ ⇒ StatusCodes.InternalServerError
    }
  }

  implicit val error_marshaller : ToResponseMarshaller[ErrorResponse] =
    ToResponseMarshaller[ErrorResponse] { (value, context) ⇒
      val status_code = disposition2StatusCode(value.disposition)
      context.marshalTo(HttpResponse(status_code, HttpEntity(value.contentType, value.formatted)))
    }

  implicit val form_error_marshaller : ToResponseMarshaller[FormErrorResponse] =
    ToResponseMarshaller[FormErrorResponse] { (value, context) ⇒
      val status_code = disposition2StatusCode(value.disposition)
      context.marshalTo(HttpResponse(status_code, HttpEntity(text_ct, value.formatted)))
    }

  implicit val exception_marshaller : ToResponseMarshaller[ExceptionResponse] =
    ToResponseMarshaller[ExceptionResponse] { (value, ctxt) ⇒
      ctxt.handleError(value.payload)
    }

  implicit def mystery_marshaller(
    implicit arf : ActorRefFactory, ec : ExecutionContext, timeout : Timeout) : ToResponseMarshaller[Response] = {
    ToResponseMarshaller[Response] {
      (value, trmc) ⇒
        {
          value match {
            case h : HtmlResponse ⇒ html_marshaller(h, trmc)
            case s : StringResponse ⇒ string_marshaller(s, trmc)
            case e : ErrorResponse ⇒ error_marshaller(e, trmc)
            case f : FormErrorResponse ⇒ form_error_marshaller(f, trmc)
            case x : ExceptionResponse ⇒ exception_marshaller(x, trmc)
            case o : OctetsResponse ⇒
              val m = octets_marshaller(o.contentType)
              m(o, trmc)
            case s : StreamResponse ⇒
              val m = stream_marshaller(s.contentType)
              m(s, trmc)
            case e : EnumeratorResponse ⇒
              val m = enumerator_marshaller
              m(e, trmc)
            case b : BSONResult ⇒
              val m = bson_marshaller
              m(b, trmc)
            case _ ⇒
              toss(s"Failed to find marshaller for: $value")
          }
        }
    }
  }
}

class StreamingResponseActor(ct : ContentType, trmc : ToResponseMarshallingContext)
  extends Actor with ActorLogging {

  object ChunkSent

  def bsonStreamToData(key : String, value : BSONValue) : Array[Byte] = {
    // TODO: Implement this conversion
    // See reactivemongo.core.iteratees and reactivemongo.bson.buffers
    val data = new Array[Byte](1)
    data.update(0, value.code)
    data
  }

  def receive = {
    case (key : String, value : BSONValue) ⇒
      context.become(
        waitingForResponder(
          trmc.startChunkedMessage(
            HttpResponse(entity = HttpEntity(ct, bsonStreamToData(key, value))),
            ack = Some(ChunkSent)
          ),
          sender()
        )
      )
    case data : Array[Byte] ⇒
      context.become(
        waitingForResponder(
          trmc.startChunkedMessage(
            HttpResponse(entity = HttpEntity(ct, data)),
            ack = Some(ChunkSent)
          ),
          sender()
        )
      )
    case Input.EOF ⇒
      trmc.marshalTo(HttpResponse(entity = HttpEntity(ct, HttpData.Empty)))
      context.stop(self)
    case error : Throwable ⇒
      trmc.handleError(error)
      context.stop(self)
  }

  def waitingForData(responder : ActorRef) : Actor.Receive = {
    case (key : String, value : BSONValue) ⇒
      responder ! MessageChunk(bsonStreamToData(key, value)).withAck(ChunkSent)
      context.become(waitingForResponder(responder, sender()))

    case data : Array[Byte] ⇒
      responder ! MessageChunk(data).withAck(ChunkSent)
      context.become(waitingForResponder(responder, sender()))

    case Input.EOF ⇒
      responder ! ChunkedMessageEnd
      context.stop(self)

    case error : Throwable ⇒
      trmc.handleError(error)
      context.stop(self)
  }

  def waitingForResponder(responder : ActorRef, requester : ActorRef) : Actor.Receive = {
    case ChunkSent ⇒ {
      requester ! ChunkSent
      context.become(waitingForData(responder))
    }

    case event : Http.ConnectionClosed ⇒ {
      log.warning(s"Response streaming stopped because: $event")
      context.stop(self)
    }
  }
  */
}
