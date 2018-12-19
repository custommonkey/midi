import algebra.Device
import cats.effect.ExitCode.{Error, Success}
import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits._
import algebra.errors.{AppError, DeviceNotFound, NoReceivers}
import devices.Gervill
import interpreters.AllInterpreters
import javax.sound.midi.MidiDevice.Info

trait PlayApp extends IOApp with AllInterpreters {

  def play(device: Device[IO]): IO[Any]

  override def run(args: List[String]): IO[ExitCode] = {

    import reports._

    devices
//      .open("OP-1 Midi Device")
      .open(Gervill)
      .use(play)
      .as(Success)
      .recoverWith {
        case err: AppError ⇒ {
          err match {
            case e: NoReceivers    ⇒ report(e)
            case e: DeviceNotFound ⇒ report(e)
          }
        } >>
          (api.midiDeviceInfo >>= report[List[Info]]) >> IO(Error)
      }
  }

}
