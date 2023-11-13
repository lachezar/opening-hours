package se.yankov.openinghours.zioapp
package api
package workinghours

import domain.common.TimeUtils
import domain.workinghours.WorkingInterval

object WorkingHoursParser:

  def validateOpeningHoursValues(request: WorkingHoursRequest)
      : Either[WorkingHoursValidationError, WorkingHoursRequest] =
    extension (wt: WorkTimes)
      def check(day: String): Either[WorkingHoursValidationError.InvalidOpeningHourValue, Unit] =
        wt.find(s => s.value < 0 || s.value > TimeUtils.daySeconds)
          .fold(Right(()))(s =>
            Left(
              WorkingHoursValidationError.InvalidOpeningHourValue(
                s"Error: $day ${s.getClass.getSimpleName} value of ${s.value} is invalid (must be integer between 0 and 86400)"
              )
            )
          )
    for {
      _ <- request.monday.check("Monday")
      _ <- request.tuesday.check("Tuesday")
      _ <- request.wednesday.check("Wednesday")
      _ <- request.thursday.check("Thursday")
      _ <- request.friday.check("Friday")
      _ <- request.saturday.check("Saturday")
      _ <- request.sunday.check("Sunday")
    } yield request

  def parseOpeningHoursSequence(seq: WorkTimes): Either[WorkingHoursValidationError, List[WorkingInterval]] =
    import WorkingHoursRequest.WorkingState.{ Open, Close }
    val sortedWorkTimes: WorkTimes = seq.sortBy(_.value)
    val doubleWorkTimes: WorkTimes =
      sortedWorkTimes ++ sortedWorkTimes.map(_.weekAbsoluteTime(offset = TimeUtils.weekSeconds))
    doubleWorkTimes
      .sliding(2)
      .find {
        case Open(_) :: Open(_) :: Nil   => true
        case Close(_) :: Close(_) :: Nil => true
        case _                           => false
      }
      .fold(Right(seq))(pair =>
        Left(WorkingHoursValidationError.InvalidOpeningHoursSequence(s"Error: ${pair.mkString(" followed by ")}"))
      )
      .map { _ =>
        doubleWorkTimes
          .dropWhile {
            case _: Open  => false
            case _: Close => true
          }
          .grouped(2)
          .collect {
            case a :: b :: Nil => WorkingInterval(start = a.value, endExclusive = b.value)
          }
          .toList
      }

  def parse(request: WorkingHoursRequest): Either[WorkingHoursValidationError, List[WorkingInterval]] =
    validateOpeningHoursValues(request).flatMap(request => parseOpeningHoursSequence(request.toWorkIntervals))
