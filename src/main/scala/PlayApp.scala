import algebra.{Note, NoteOrRest, Tempo, ToScore}
import algebra.errors.{AppError, DeviceNotFound, NoReceivers}
import algebra.types.Channel
import cats.effect.ExitCode.{Error, Success}
import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits._
import eu.timepit.refined.auto._
import interpreters.AllInterpreters
import javax.sound.midi.MidiDevice.Info

trait PlayApp extends IOApp with AllInterpreters {

  implicit val tup: ToScore[Note]                      = (i: Note) ⇒ List(i)
  implicit def list[T <: NoteOrRest]: ToScore[List[T]] = (l: List[T]) ⇒ l
  implicit var tempo: Tempo                            = Tempo(120)
  implicit var channel: Channel                        = 0

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
