val zioVersion            = "2.0.19"
val tapirZioVersion       = "1.9.0"
val tapirSwaggerVersion   = "1.9.0"
val zioHttpVersion        = "3.0.0-RC3"
val zioJsonVersion        = "0.6.2"
val zioConfigVersion      = "3.0.7"
val zioLoggingVersion     = "2.1.15"
val logbackClassicVersion = "1.4.11"

lazy val root = (project in file("."))
  .settings(
    inThisBuild(
      List(
        organization := "se.yankov",
        scalaVersion := "3.3.1",
      )
    ),
    name                    := "opening-hours",
    libraryDependencies ++= Seq(
      "dev.zio"                     %% "zio"                     % zioVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-zio"               % tapirZioVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-zio-http-server"   % tapirZioVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-json-zio"          % tapirZioVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-bundle" % tapirSwaggerVersion,
      "dev.zio"                     %% "zio-http"                % zioHttpVersion,
      "dev.zio"                     %% "zio-config"              % zioConfigVersion,
      "dev.zio"                     %% "zio-config-typesafe"     % zioConfigVersion,
      "dev.zio"                     %% "zio-config-magnolia"     % zioConfigVersion,
      "dev.zio"                     %% "zio-json"                % zioJsonVersion,

      // logging
      "dev.zio"       %% "zio-logging"       % zioLoggingVersion,
      "dev.zio"       %% "zio-logging-slf4j" % zioLoggingVersion,
      "ch.qos.logback" % "logback-classic"   % logbackClassicVersion,

      // test
      "dev.zio" %% "zio-test-sbt" % zioVersion % Test,
    ),
    testFrameworks          := Seq(new TestFramework("zio.test.sbt.ZTestFramework")),
    // try using the `tpolecatScalacOptions` configuration key for any additional compiler flags
    Compile / doc / sources := Seq.empty,
  )
  .enablePlugins(JavaAppPackaging, UniversalPlugin)

addCommandAlias("fmt", "all scalafmtSbt scalafmtAll")

wartremoverErrors ++= Warts.unsafe diff Seq(Wart.Any, Wart.DefaultArguments)
