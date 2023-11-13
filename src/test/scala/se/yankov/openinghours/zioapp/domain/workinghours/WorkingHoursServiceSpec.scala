package se.yankov.openinghours.zioapp
package domain
package workinghours

import zio.*
import zio.test.*

import domain.common.TimeUtils

object WorkingHoursServiceSpec extends ZIOSpecDefault:
  override def spec = suite("WorkingHoursServiceSpec")(
    test("format - overlapping intervals error") {
      val actual =
        WorkingHoursService
          .format(WorkingInterval(start = 0, endExclusive = 5) :: WorkingInterval(start = 4, endExclusive = 10) :: Nil)
          .exit
      assertZIO(actual)(Assertion.failsWithA[WorkingHoursDomainError])
    },
    test("format - ok") {
      val actual   =
        WorkingHoursService
          .format(
            WorkingInterval(start = 0, endExclusive = TimeUtils.daySeconds * 2) :: WorkingInterval(
              start = TimeUtils.daySeconds * 3 + 3600,
              endExclusive = TimeUtils.daySeconds * 4 + 1800,
            ) :: Nil
          )
      val expected =
        WorkingInterval(start = 0, endExclusive = TimeUtils.daySeconds) ::
          WorkingInterval(start = TimeUtils.daySeconds, endExclusive = TimeUtils.daySeconds * 2) ::
          WorkingInterval(start = TimeUtils.daySeconds * 3 + 3600, endExclusive = TimeUtils.daySeconds * 4 + 1800) ::
          Nil
      assertZIO(actual)(Assertion.equalTo(expected))
    },
  ).provideSomeLayer(WorkingHoursService.layer)
