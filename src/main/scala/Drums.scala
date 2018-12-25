import algebra.{Note, Rest}
import algebra.Tempo.BeatOps
import algebra.types.{Channel, Nint}
import cats.effect.IO
import cats.implicits._
import devices.UnoMidiInterface
import eu.timepit.refined.api.RefType.applyRef
import eu.timepit.refined.auto._

object Drums extends PlayApp {
  val channels: List[Channel] = (0 to 15).map(applyRef[Channel].unsafeFrom(_)).toList

  channel = 10
  val Drum1: Nint = 36
  val Drum2: Nint = 37
  val Drum3: Nint = 38
  val Drum4: Nint = 44
  val Drum5: Nint = 45
  val Drum6: Nint = 46

  val bass  = List(Note(Drum1, 1.beat), Note(Drum1, 1.beat))
  val snare = List(Rest(1.semiquaver), Note(Drum2, 1.beat), Note(Drum2, 1.beat))

  override def play: IO[Any] =
    devices
      .open(UnoMidiInterface)
      .use { device ⇒
        (device << bass) *> (device << snare)
      }
}
