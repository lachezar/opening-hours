package se.yankov.openinghours.zioapp
package api
package workinghours

sealed trait WorkingHoursValidationError:
  val message: String

object WorkingHoursValidationError:
  final case class InvalidOpeningHourValue(message: String = "Opening hour must be integer between 0 and 86400")
      extends WorkingHoursValidationError
  final case class InvalidOpeningHoursSequence(message: String = "Close after Close or Open after Open is not allowed")
      extends WorkingHoursValidationError
