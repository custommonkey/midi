package interpreters

import algebra.Algebra
import algebra.types.Sleep
import cats.effect.IO

trait AllInterpreters extends Algebra[IO] {
  protected implicit def sleep: Sleep[IO]
  override final val println = new PrintInterpreter[IO]
  override final val reports = new ReportInterpreter[IO](println)
  override final val api     = new MidiApiInterpreter[IO](println)
  override final val devices = new DevicesInterpreter[IO](api)
  override final val random  = new RandomInterpreter[IO]
  override final val utils   = new UtilsInterpreter[IO](api, println, random)
}
