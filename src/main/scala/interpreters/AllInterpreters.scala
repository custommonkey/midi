package interpreters

import algebra.Algebra
import cats.effect.{IO, Timer}

trait AllInterpreters extends Algebra[IO] {
  protected implicit def timer: Timer[IO]
  override final val println  = new PrintInterpreter[IO]()
  override final val reports  = new ReportInterpreter[IO](println)
  override final val api      = new MidiApiIntepreter[IO](println)
  override final val devices  = new DevicesInterpreter[IO](api, println)
  override final val random   = new RandomInterpreter[IO]()
  override final val utils    = new UtilsInterpreter[IO](api, println, random)
}
