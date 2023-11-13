package se.yankov.openinghours.zioapp
package api

import zio.*
import zio.http.*
import zio.test.*

import domain.workinghours.WorkingHoursService

object PublicApiSpec extends ZIOSpecDefault:

  override def spec = suite("public http")(
    suite("format working hours")(
      test("forbid requests without content-type application/json") {
        val body   =
          """{"monday":[],"tuesday":[],"wednesday":[],"thursday":[],"friday":[{"type":"close","value":9936000}],"saturday":[{"type":"open","value":36000}],"sunday":[]}"""
        val actual =
          PublicApi
            .api
            .runZIO(
              Request
                .post(URL(Root / "v1" / "opening-hours" / "format"), Body.fromString(body))
            )
        assertZIO(actual.map(_.status))(Assertion.equalTo(Status.Forbidden))
      },
      test("validation error - invalid value") {
        val body   =
          """{"monday":[],"tuesday":[],"wednesday":[],"thursday":[],"friday":[{"type":"close","value":9936000}],"saturday":[{"type":"open","value":36000}],"sunday":[]}"""
        val actual =
          PublicApi
            .api
            .runZIO(
              Request
                .post(URL(Root / "v1" / "opening-hours" / "format"), Body.fromString(body))
                .addHeader(Header.ContentType(MediaType.application.json))
            )
        assertZIO(actual.flatMap(_.body.asString))(
          Assertion.equalTo(
            "Error: Friday Close value of 9936000 is invalid (must be integer between 0 and 86400)"
          )
        ) &&
        assertZIO(actual.map(_.status))(Assertion.equalTo(Status.BadRequest))
      },
      test("validation error - invalid sequence") {
        val body   =
          """{"monday":[],"tuesday":[],"wednesday":[],"thursday":[],"friday":[{"type":"close","value":36000}],"saturday":[{"type":"close","value":36000}],"sunday":[]}"""
        val actual =
          PublicApi
            .api
            .runZIO(
              Request
                .post(URL(Root / "v1" / "opening-hours" / "format"), Body.fromString(body))
                .addHeader(Header.ContentType(MediaType.application.json))
            )
        assertZIO(actual.flatMap(_.body.asString))(
          Assertion.equalTo("Error: Close on Friday 10 AM followed by Close on Saturday 10 AM")
        ) &&
        assertZIO(actual.map(_.status))(Assertion.equalTo(Status.BadRequest))
      },
      test("working time schema - ok") {
        val body   =
          """{"monday":[],"tuesday":[{"type":"open","value":36000},{"type":"close","value":64800}],"wednesday":[],"thursday":[{"type":"open","value":37800},{"type":"close","value":64800}],"friday":[{"type":"open","value":36000}],"saturday":[{"type":"close","value":3600},{"type":"open","value":36000}],"sunday":[{"type":"close","value":3600},{"type":"open","value":43200},{"type":"close","value":75600}]}"""
        val actual =
          PublicApi
            .api
            .runZIO(
              Request
                .post(URL(Root / "v1" / "opening-hours" / "format"), Body.fromString(body))
                .addHeader(Header.ContentType(MediaType.application.json))
            )
        assertZIO(actual.flatMap(_.body.asString))(
          Assertion.equalTo("""
            |Monday: Closed
            |Tuesday: 10 AM - 6 PM
            |Wednesday: Closed
            |Thursday: 10:30 AM - 6 PM
            |Friday: 10 AM - 1 AM
            |Saturday: 10 AM - 1 AM
            |Sunday: 12 PM - 9 PM
          """.stripMargin.trim)
        ) &&
        assertZIO(actual.map(_.status))(Assertion.equalTo(Status.Ok))
      },
      test("working time schema with splitting of long intervals - ok") {
        val body   =
          """{"monday":[],"tuesday":[],"wednesday":[],"thursday":[],"friday":[{"type":"close","value":36000}],"saturday":[{"type":"open","value":36000}],"sunday":[]}"""
        val actual =
          PublicApi
            .api
            .runZIO(
              Request
                .post(URL(Root / "v1" / "opening-hours" / "format"), Body.fromString(body))
                .addHeader(Header.ContentType(MediaType.application.json))
            )
        assertZIO(actual.flatMap(_.body.asString))(
          Assertion.equalTo("""
            |Monday: 12 AM - 12 AM
            |Tuesday: 12 AM - 12 AM
            |Wednesday: 12 AM - 12 AM
            |Thursday: 12 AM - 12 AM
            |Friday: 12 AM - 10 AM
            |Saturday: 10 AM - 12 AM
            |Sunday: 12 AM - 12 AM
          """.stripMargin.trim)
        ) &&
        assertZIO(actual.map(_.status))(Assertion.equalTo(Status.Ok))
      },
      test("working time schema with multiple openings per day") {
        val body   =
          """{"monday":[],"tuesday":[],"wednesday":[],"thursday":[],"friday":[{"type":"open","value":64800}],"saturday":[{"type":"close","value":3600},{"type":"open","value":32400},{"type":"close","value":39600},{"type":"open","value":57600},{"type":"close","value":82800}],"sunday":[]}"""
        val actual =
          PublicApi
            .api
            .runZIO(
              Request
                .post(URL(Root / "v1" / "opening-hours" / "format"), Body.fromString(body))
                .addHeader(Header.ContentType(MediaType.application.json))
            )
        assertZIO(actual.flatMap(_.body.asString))(
          Assertion.equalTo("""
            |Monday: Closed
            |Tuesday: Closed
            |Wednesday: Closed
            |Thursday: Closed
            |Friday: 6 PM - 1 AM
            |Saturday: 9 AM - 11 AM, 4 PM - 11 PM
            |Sunday: Closed
          """.stripMargin.trim)
        ) &&
        assertZIO(actual.map(_.status))(Assertion.equalTo(Status.Ok))
      },
      test("working time schema with multiple openings per day and splitting of large work interval") {
        val body   =
          """{"monday":[{"type":"open","value":0},{"type":"close","value":43200},{"type":"open","value":64800}],"tuesday":[{"type":"close","value":68400}],"wednesday":[],"thursday":[],"friday":[],"saturday":[],"sunday":[]}"""
        val actual =
          PublicApi
            .api
            .runZIO(
              Request
                .post(URL(Root / "v1" / "opening-hours" / "format"), Body.fromString(body))
                .addHeader(Header.ContentType(MediaType.application.json))
            )
        assertZIO(actual.flatMap(_.body.asString))(
          Assertion.equalTo("""
            |Monday: 12 AM - 12 PM, 6 PM - 12 AM
            |Tuesday: 12 AM - 7 PM
            |Wednesday: Closed
            |Thursday: Closed
            |Friday: Closed
            |Saturday: Closed
            |Sunday: Closed""".stripMargin.trim)
        ) &&
        assertZIO(actual.map(_.status))(Assertion.equalTo(Status.Ok))
      },
    )
  ).provide(PublicApiHandler.layer, WorkingHoursService.layer)
