package se.yankov.openinghours.zioapp
package domain
package workinghours

import zio.*

import domain.common.TimeUtils

final class WorkingHoursService:

  def format(workingIntervals: List[WorkingInterval]): IO[WorkingHoursDomainError, List[WorkingInterval]] =
    workingIntervals
      .sliding(2)
      .find {
        case a :: b :: Nil => b.start < a.endExclusive
        case _             => false
      }
      .fold(ZIO.unit)(pair => ZIO.fail(WorkingHoursDomainError(s"Intersecting working time intervals ($pair)"))) *>
      ZIO.succeed(workingIntervals.flatMap(_.split).map(_.normalize).distinct.sortBy(_.start))

object WorkingHoursService:

  def format(workingIntervals: List[WorkingInterval])
      : ZIO[WorkingHoursService, WorkingHoursDomainError, List[WorkingInterval]] =
    ZIO.serviceWithZIO[WorkingHoursService](_.format(workingIntervals))

  val layer: ULayer[WorkingHoursService] = ZLayer.derive[WorkingHoursService]
