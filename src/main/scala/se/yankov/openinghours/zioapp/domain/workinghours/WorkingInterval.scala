package se.yankov.openinghours.zioapp
package domain
package workinghours

import domain.common.TimeUtils

import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Locale

final case class WorkingInterval(start: Int, endExclusive: Int):

  def split: List[WorkingInterval] =
    if endExclusive - start <= TimeUtils.daySeconds then this :: Nil
    else
      val splitPoint: Int = (start / TimeUtils.daySeconds + 1) * TimeUtils.daySeconds
      WorkingInterval(start, splitPoint) :: WorkingInterval(splitPoint, endExclusive).split

  def normalize: WorkingInterval =
    copy(start = start % TimeUtils.weekSeconds, endExclusive = endExclusive % TimeUtils.weekSeconds)

  override def toString(): String =
    s"${TimeUtils.formatDay(start)}: ${TimeUtils.formatTime(start)} - ${TimeUtils.formatTime(endExclusive)}"

object WorkingInterval:

  extension (intervals: List[WorkingInterval])
    def toWorkSchedule: String =
      val map: Map[DayOfWeek, String] =
        intervals.map(interval => DayOfWeek.values()(interval.start / TimeUtils.daySeconds) -> interval.toString).toMap
      DayOfWeek
        .values()
        .map(day => map.getOrElse(day, s"${day.getDisplayName(TextStyle.FULL, Locale.ENGLISH)}: Closed"))
        .mkString("\n")
