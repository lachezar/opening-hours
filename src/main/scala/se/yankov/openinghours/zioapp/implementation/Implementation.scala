package se.yankov.openinghours.zioapp
package implementation

import zio.*

// This is not doing anything, but I've left it here out of conceptual reasons
type ImplementationEnv = Unit

val layer: ULayer[ImplementationEnv] = ZLayer(ZIO.unit)
