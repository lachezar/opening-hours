package se.yankov.openinghours.zioapp
package domain
package workinghours

sealed trait WorkingState:
  val value: Int
  def copy(value: Int): WorkingState
  def weekAbsoluteTime(offset: Int): WorkingState = copy(value = value + offset)

object WorkingState:
  final case class Open(value: Int)  extends WorkingState
  final case class Close(value: Int) extends WorkingState
