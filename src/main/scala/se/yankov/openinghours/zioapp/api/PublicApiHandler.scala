package se.yankov.openinghours.zioapp
package api

import zio.*

import api.workinghours.*
import domain.workinghours.{ WorkingHoursDomainError, WorkingHoursService }

final case class PublicApiHandler(workingHoursService: WorkingHoursService):

  def health: UIO[String] = ZIO.succeed("ok")

  def formatWorkingHours(request: WorkingHoursRequest)
      : IO[WorkingHoursValidationError | WorkingHoursDomainError, String] =
    ZIO
      .fromEither(WorkingHoursParser.parse(request))
      .flatMap(workingHoursService.format(_).map(_.toWorkSchedule))

object PublicApiHandler:

  def formatWorkingHours(request: WorkingHoursRequest)
      : ZIO[PublicApiHandler, WorkingHoursValidationError | WorkingHoursDomainError, String] =
    ZIO.serviceWithZIO[PublicApiHandler](_.formatWorkingHours(request))

  val layer: RLayer[WorkingHoursService, PublicApiHandler] = ZLayer.derive[PublicApiHandler]
