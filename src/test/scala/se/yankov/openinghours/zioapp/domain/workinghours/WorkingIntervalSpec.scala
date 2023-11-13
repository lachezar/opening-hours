package se.yankov.openinghours.zioapp
package domain
package workinghours

import zio.*
import zio.test.*

import domain.common.TimeUtils

object WorkingIntervalSpec extends ZIOSpecDefault:
  override def spec = suite("WorkingInterval")(
    test("split interval") {

      assert(WorkingInterval(start = 0, endExclusive = TimeUtils.daySeconds).split)(
        Assertion.equalTo(WorkingInterval(start = 0, endExclusive = TimeUtils.daySeconds) :: Nil)
      ) &&
      assert(WorkingInterval(start = 9 * 3600, endExclusive = TimeUtils.daySeconds + 6 * 3600).split)(
        Assertion.equalTo(WorkingInterval(start = 9 * 3600, endExclusive = TimeUtils.daySeconds + 6 * 3600) :: Nil)
      ) &&
      assert(WorkingInterval(start = 0, endExclusive = TimeUtils.daySeconds + 1).split)(
        Assertion.equalTo(
          WorkingInterval(start = 0, endExclusive = TimeUtils.daySeconds) :: WorkingInterval(
            start = TimeUtils.daySeconds,
            endExclusive = TimeUtils.daySeconds + 1,
          ) :: Nil
        )
      ) &&
      assert(WorkingInterval(start = 0, endExclusive = TimeUtils.weekSeconds).split)(
        Assertion.equalTo(
          WorkingInterval(start = TimeUtils.daySeconds * 0, endExclusive = TimeUtils.daySeconds * 1) ::
            WorkingInterval(start = TimeUtils.daySeconds * 1, endExclusive = TimeUtils.daySeconds * 2) ::
            WorkingInterval(start = TimeUtils.daySeconds * 2, endExclusive = TimeUtils.daySeconds * 3) ::
            WorkingInterval(start = TimeUtils.daySeconds * 3, endExclusive = TimeUtils.daySeconds * 4) ::
            WorkingInterval(start = TimeUtils.daySeconds * 4, endExclusive = TimeUtils.daySeconds * 5) ::
            WorkingInterval(start = TimeUtils.daySeconds * 5, endExclusive = TimeUtils.daySeconds * 6) ::
            WorkingInterval(start = TimeUtils.daySeconds * 6, endExclusive = TimeUtils.daySeconds * 7) :: Nil
        )
      )
    },
    test("work schedule") {
      assert(List.empty[WorkingInterval].toWorkSchedule)(
        Assertion.equalTo("""
        |Monday: Closed
        |Tuesday: Closed
        |Wednesday: Closed
        |Thursday: Closed
        |Friday: Closed
        |Saturday: Closed
        |Sunday: Closed""".stripMargin.trim)
      ) &&
      assert((WorkingInterval(start = 9 * 3600, endExclusive = TimeUtils.daySeconds + 2 * 3600) :: Nil).toWorkSchedule)(
        Assertion.equalTo("""
        |Monday: 9 AM - 2 AM
        |Tuesday: Closed
        |Wednesday: Closed
        |Thursday: Closed
        |Friday: Closed
        |Saturday: Closed
        |Sunday: Closed""".stripMargin.trim)
      )
    },
  )
