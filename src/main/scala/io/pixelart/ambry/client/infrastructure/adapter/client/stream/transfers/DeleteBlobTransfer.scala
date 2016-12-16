package io.pixelart.ambry.client.infrastructure.adapter.client.stream.transfers

import akka.NotUsed
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.scaladsl.Flow
import io.pixelart.ambry.client.domain.model.{AmbryId, AmbryPostFileResponse, UploadBlobRequestData, AmbryUri}
import io.pixelart.ambry.client.infrastructure.adapter.client.stream.transfers.DeleteBlobTransfer.DeleteBlobRequestData
import io.pixelart.ambry.client.infrastructure.adapter.client.stream.transfers.UploadBloabTransfer.{ UploadBloabRequestData }
import io.pixelart.ambry.client.infrastructure.adapter.client.{ AmbryHttpClientResponseHandler, Execution }

object DeleteBlobTransfer {
  case class DeleteBlobRequestData(ambryUri: AmbryUri, ambryId: AmbryId)
}

trait DeleteBlobTransfer extends AmbryHttpClientResponseHandler {
  self: Execution =>

  import io.pixelart.ambry.client.infrastructure.translator.AmbryResponseUnmarshallers._

  def flowAuthenticate: Flow[DeleteBlobRequestData, Boolean, NotUsed] =
    Flow[DeleteBlobRequestData].mapAsync(1) { data =>
      val httpReq = httpRequests.deleteBlobHttpRequest(data.ambryUri, data.ambryId)

      val unmarshalFunc = (r: HttpResponse) => Unmarshal(r).to[Boolean]

      requestsExecutor.executeRequest(httpReq, unmarshalFunc)
    }
}
