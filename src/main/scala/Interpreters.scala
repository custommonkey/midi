import cats.effect.{IO, Timer}
import interpreters.{DevicesInterpreter, MidiApiIntepreter, PrintInterpreter, ReportInterpreter}

trait Interpreters {
  protected implicit def timer: Timer[IO]
  val println   = new PrintInterpreter[IO]()
  val reports = new ReportInterpreter[IO](println)
  val api     = new MidiApiIntepreter[IO](println)
  val things  = new DevicesInterpreter[IO](api)
}
