package io.pixelart.ambry.client.infrastructure.adapter.akkahttp

import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import io.pixelart.ambry.client.domain.model.{AmbryHttpAuthorisationException, AmbryHttpBadRequestException}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

trait AkkaHttpAmbryResponseHandler {

  protected def handleHttpResponse[T](httpResponse: HttpResponse, unmarshal: HttpResponse => Future[T])(implicit ec: ExecutionContext, mat: ActorMaterializer): Future[T] = {
    httpResponse match {
      case response @ HttpResponse(StatusCodes.OK, _, _, _) =>
        unmarshal(response).recover {
          case e => throw new AmbryHttpBadRequestException(e.getMessage)
        }
      case response @ HttpResponse(StatusCodes.Created) =>
      //todo
      case response @ HttpResponse(StatusCodes.Accepted) =>
      //todo
      case response @ HttpResponse(StatusCodes.Unauthorized, _, _, _) =>
        Unmarshal(response).to[AmbryHttpAuthorisationException].flatMap(Future.failed(_)).recoverWith {
          case e => throw new AmbryHttpBadRequestException(e.getMessage)
        }
      case response @ HttpResponse(StatusCodes.NotFound, h, msg, _) =>
        Unmarshal(response).to[AmbryHttpBadRequestException].flatMap(Future.failed(_))

      case response @ HttpResponse(StatusCodes.ProxyAuthenticationRequired, h, msg, _) =>
      //todo
      case response @ HttpResponse(StatusCodes.Gone, h, msg, _)                        =>
      //todo
      case response @ HttpResponse(StatusCodes.InternalServerError, h, msg, _)         =>
      //todo
    }
  }

  protected def handleHttpResponse[T](
    httpResponse: Try[HttpResponse],
    unmarshal: HttpResponse => Future[T]
  )(implicit ec: ExecutionContext, mat: ActorMaterializer): Future[T] = {
    httpResponse match {
      case Success(resp) => handleHttpResponse(resp, unmarshal)
      case Failure(why)  => Future.failed(why)
    }
  }
}