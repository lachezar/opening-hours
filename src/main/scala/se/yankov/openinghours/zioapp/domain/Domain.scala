package se.yankov.openinghours.zioapp
package domain

import zio.RLayer

import domain.workinghours.WorkingHoursService
import implementation.ImplementationEnv

type DomainEnv = WorkingHoursService

val layer: RLayer[ImplementationEnv, DomainEnv] = WorkingHoursService.layer
