package se.yankov.openinghours.zioapp

import zio.*
import zio.http.*
import zio.http.netty.NettyConfig
import zio.logging.backend.SLF4J

import api.*
import implementation.http.HttpConfig

object ZioApp extends ZIOAppDefault:

  private def publicApiProgram(port: Int): RIO[PublicApiHandler, Nothing] =
    (ZIO.serviceWithZIO[PublicApiHandler](handlers => Server.install(PublicApi.api)) *>
      ZIO.logDebug(s"Public API server started on port $port") *>
      ZIO.never)
      .provideSomeLayer(
        ZLayer.succeed(Server.Config.default.port(port)) ++
          ZLayer.succeed(NettyConfig.default.leakDetection(NettyConfig.LeakDetectionLevel.PARANOID)) >>>
          Server.customized
      )

  override val run: UIO[ExitCode] =
    ZIO
      .serviceWith[HttpConfig](_.port)
      .flatMap(publicApiProgram(_))
      .provide(
        (Runtime.removeDefaultLoggers >>> SLF4J.slf4j) ++
          AppConfig.layer >+>
          (implementation.layer >>> domain.layer >>> PublicApiHandler.layer)
      )
      .foldCauseZIO(
        error => ZIO.logError(s"Program failed: ${error.squash.getMessage}") *> ZIO.succeed(ExitCode.failure),
        _ => ZIO.succeed(ExitCode.success),
      )
