package se.yankov.openinghours.zioapp

import zio.*
import zio.config.magnolia.descriptor
import zio.config.syntax.*
import zio.config.toKebabCase
import zio.config.typesafe.TypesafeConfig

import implementation.http.HttpConfig

import scala.io.Source

type ConfigEnv = HttpConfig

final case class AppConfig(http: HttpConfig)

object AppConfig:
  val layer: TaskLayer[ConfigEnv] =
    ZLayer(ZIO.attempt(Source.fromResource("application.conf").mkString))
      .flatMap(content =>
        val configLayer = TypesafeConfig
          .fromHoconString(content.get, descriptor[AppConfig].mapKey(toKebabCase))
          .mapError(e => new RuntimeException(e.prettyPrint()))
        configLayer.narrow(_.http)
      )
