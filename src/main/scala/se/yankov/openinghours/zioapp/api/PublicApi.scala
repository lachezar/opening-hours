package se.yankov.openinghours.zioapp
package api

import zio.*
import zio.http.{ Header => ZioHttpHeader, MediaType => ZioHttpMediaType, Response => ZioHttpResponse, * }

import api.workinghours.*
import domain.workinghours.WorkingHoursDomainError

import sttp.model.{ Header, MediaType, StatusCode }
import sttp.tapir.{ endpoint, Codec, Endpoint, PublicEndpoint, Schema }
import sttp.tapir.CodecFormat.TextPlain
import sttp.tapir.generic.auto.*
import sttp.tapir.json.zio.jsonBody
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.server.ziohttp.ZioHttpInterpreter
import sttp.tapir.swagger.{ SwaggerUI, SwaggerUIOptions }
import sttp.tapir.swagger.bundle.SwaggerInterpreter
import sttp.tapir.ztapir.*

object PublicApi {

  given Codec.PlainCodec[WorkingHoursDomainError]     = Codec.string.mapEither(_ => Left("unsupported"))(_.message)
  given Codec.PlainCodec[WorkingHoursValidationError] = Codec.string.mapEither(_ => Left("unsupported"))(_.message)

  val formatWorkingHoursEndpoint
      : PublicEndpoint[WorkingHoursRequest, WorkingHoursValidationError | WorkingHoursDomainError, String, Any] =
    endpoint
      .post
      .in("v1" / "opening-hours" / "format")
      .in(jsonBody[WorkingHoursRequest].example(Examples.workingHoursRequest))
      .in(header(Header.contentType(MediaType.ApplicationJson)))
      .errorOut(
        oneOf(
          oneOfVariant(StatusCode.BadRequest, plainBody[WorkingHoursDomainError]),
          oneOfVariant(
            StatusCode.BadRequest,
            plainBody[WorkingHoursValidationError].example(WorkingHoursValidationError.InvalidOpeningHourValue()),
          ),
        )
      )
      .out(stringBody.example(Examples.workingHoursResponse))

  val formatWorkingHoursServerEndpoint: ZServerEndpoint[PublicApiHandler, Any] =
    formatWorkingHoursEndpoint.zServerLogic(PublicApiHandler.formatWorkingHours(_))

  val apiEndpoints: List[Endpoint[Unit, _, _, _, _]]                         = formatWorkingHoursEndpoint :: Nil
  val apiEndpointsWithHandlers: List[ZServerEndpoint[PublicApiHandler, Any]] = formatWorkingHoursServerEndpoint :: Nil

  // TODO: sealed traits in OpenAPI 3.1.0 are still not shown correct (just "object")
  val swaggerEndpoints: List[ZServerEndpoint[Any, Any]] =
    import sttp.apispec.openapi.circe.yaml.*
    import sttp.apispec.openapi.*
    import sttp.tapir.docs.openapi.OpenAPIDocsInterpreter
    val docs: OpenAPI = OpenAPIDocsInterpreter()
      .toOpenAPI(formatWorkingHoursEndpoint :: Nil, Info("Opening Hours", "1.0"))
      .openapi("3.0.3") // "3.0.3" version explicitly specified
    SwaggerUI(docs.toYaml3_0_3, SwaggerUIOptions.default)

  val api: HttpApp[PublicApiHandler] =
    Routes(
      Method.GET / "health" -> handler(ZIO.serviceWithZIO[PublicApiHandler](_.health).map(ZioHttpResponse.text(_)))
    ).toHttpApp ++
      ZioHttpInterpreter().toHttp(swaggerEndpoints) ++
      (ZioHttpInterpreter().toHttp(apiEndpointsWithHandlers) @@ requireContentType)

  def requireContentType: HandlerAspect[Any, Unit] =
    HandlerAspect
      .Allow(())
      .apply { req =>
        req.headers.get(ZioHttpHeader.ContentType).exists(_.mediaType == ZioHttpMediaType.application.json)
      }

  object Examples {

    def workingHoursRequest: WorkingHoursRequest =
      import WorkingHoursRequest.WorkingState.{ Open, Close }
      WorkingHoursRequest(
        monday = Nil,
        tuesday = Open(36000) :: Close(64800) :: Nil,
        wednesday = Nil,
        thursday = Open(37800) :: Close(64800) :: Nil,
        friday = Open(36000) :: Nil,
        saturday = Close(3600) :: Open(36000) :: Nil,
        sunday = Close(3600) :: Open(43200) :: Close(75600) :: Nil,
      )

    val workingHoursResponse: String = """
        |Monday: Closed
        |Tuesday: 10 AM - 6 PM
        |Wednesday: Closed
        |Thursday: 10:30 AM - 6 PM
        |Friday: 10 AM - 1 AM
        |Saturday: 10 AM - 1 AM
        |Sunday: 12 PM - 9 PM
        """.stripMargin.trim
  }
}
