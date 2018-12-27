import algebra.Tempo.BeatOps
import algebra.types.{Channel, MidiInt}
import algebra.{EventAlgebra, Note, Rest}
import cats.effect.IO
import devices.UnoMidiInterface
import eu.timepit.refined.api.RefType.applyRef
import eu.timepit.refined.auto._

object Drums extends PlayApp with EventAlgebra {
  val channels: List[Channel] = (0 to 15).map(applyRef[Channel].unsafeFrom(_)).toList

  channel = 10
  val Drum1: MidiInt = 36
  val Drum2: MidiInt = 37
  val Drum3: MidiInt = 38
  val Drum4: MidiInt = 44
  val Drum5: MidiInt = 45
  val Drum6: MidiInt = 46

  val bass  = score(Note(Drum1, 1.beat), Note(Drum1, 1.beat))
  val snare = score(Rest(1.semiquaver), Note(Drum2, 1.beat), Note(Drum2, 1.beat))
  val s     = snare + bass

  override def play: IO[Any] =
    devices
      .open(UnoMidiInterface)
      .use { device ⇒
        device << blo(s)
      }
}
