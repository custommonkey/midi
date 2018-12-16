import algebra.algebra.Device
import cats.effect.ExitCode.{Error, Success}
import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits._
import algebra.errors.{AppError, DeviceNotFound, NoReceivers}

trait PlayApp extends IOApp with Interpreters {

  def play(device: Device[IO]): IO[Unit]

  override def run(args: List[String]): IO[ExitCode] = {

    import reports._

    things
//      .open("OP-1 Midi Device")
      .open("Gervill")
      .use(play)
      .as(Success)
      .recoverWith {
        case err: AppError ⇒ {
          err match {
            case e: NoReceivers    ⇒ noReceivers(e)
            case e: DeviceNotFound ⇒ deviceNotFound(e)
          }
        } >>
          (api.midiDeviceInfo >>= listDevices) >> IO(Error)
      }
  }

}
