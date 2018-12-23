import algebra.Tempo
import algebra.errors.{AppError, DeviceNotFound, NoReceivers}
import algebra.types.{Channel, Thing, ToScore}
import cats.effect.ExitCode.{Error, Success}
import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits._
import eu.timepit.refined.auto._
import interpreters.AllInterpreters
import javax.sound.midi.MidiDevice.Info

import scala.concurrent.duration.FiniteDuration

trait PlayApp extends IOApp with AllInterpreters {

  implicit val tup: ToScore[(Int, FiniteDuration)] = (i: (Int, FiniteDuration)) ⇒ List(Thing(i))
  implicit val list: ToScore[List[(Int, FiniteDuration)]] = (l: List[(Int, FiniteDuration)]) ⇒
    l.map(Thing(_))

  implicit var tempo: Tempo     = Tempo(120)
  implicit var channel: Channel = 0

  def play: IO[Any]

  override def run(args: List[String]): IO[ExitCode] = {

    import reports._

    play
      .as(Success)
      .recoverWith {
        case err: AppError ⇒ {
          err match {
            case e: NoReceivers    ⇒ report(e)
            case e: DeviceNotFound ⇒ report(e)
          }
        } >> (api.midiDeviceInfo >>= report[List[Info]]) >> IO(Error)
      }
  }

}
