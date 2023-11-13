package se.yankov.openinghours.zioapp
package implementation

import zio.*

type ImplementationEnv = Unit

val layer: ULayer[ImplementationEnv] = ZLayer(ZIO.unit)
