package se.yankov.openinghours.zioapp
package domain
package common

import java.time.{ DayOfWeek, LocalTime }
import java.time.format.{ DateTimeFormatter, TextStyle }
import java.util.Locale

object TimeUtils {
  val daySeconds: Int  = 24 * 60 * 60
  val weekSeconds: Int = daySeconds * 7

  def formatTime(seconds: Int): String =
    LocalTime
      .ofSecondOfDay(seconds % daySeconds)
      .format(DateTimeFormatter.ofPattern(if seconds % 3600 == 0 then "h a" else "h:mm a"))

  def formatDay(seconds: Int): String =
    DayOfWeek.values()((seconds % weekSeconds) / daySeconds).getDisplayName(TextStyle.FULL, Locale.ENGLISH)

}
