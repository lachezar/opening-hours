package se.yankov.openinghours.zioapp
package api
package workinghours

import zio.json.*

import domain.common.TimeUtils

type WorkTimes = List[WorkingHoursRequest.WorkingState]

final case class WorkingHoursRequest(
    monday: WorkTimes,
    tuesday: WorkTimes,
    wednesday: WorkTimes,
    thursday: WorkTimes,
    friday: WorkTimes,
    saturday: WorkTimes,
    sunday: WorkTimes,
  ) derives JsonCodec:
  def toWorkIntervals: WorkTimes =
    monday.map(_.weekAbsoluteTime(offset = TimeUtils.daySeconds * 0)) ++
      tuesday.map(_.weekAbsoluteTime(offset = TimeUtils.daySeconds * 1)) ++
      wednesday.map(_.weekAbsoluteTime(offset = TimeUtils.daySeconds * 2)) ++
      thursday.map(_.weekAbsoluteTime(offset = TimeUtils.daySeconds * 3)) ++
      friday.map(_.weekAbsoluteTime(offset = TimeUtils.daySeconds * 4)) ++
      saturday.map(_.weekAbsoluteTime(offset = TimeUtils.daySeconds * 5)) ++
      sunday.map(_.weekAbsoluteTime(offset = TimeUtils.daySeconds * 6))

object WorkingHoursRequest:
  @jsonDiscriminator("type")
  sealed trait WorkingState derives JsonCodec:
    val value: Int
    def copy(value: Int): WorkingState
    def weekAbsoluteTime(offset: Int): WorkingState = copy(value = value + offset)
    override def toString(): String                 =
      s"${getClass.getSimpleName} on ${TimeUtils.formatDay(value)} ${TimeUtils.formatTime(value)}"

  object WorkingState:
    @jsonHint("open")
    final case class Open(value: Int)  extends WorkingState derives JsonCodec
    @jsonHint("close")
    final case class Close(value: Int) extends WorkingState derives JsonCodec
