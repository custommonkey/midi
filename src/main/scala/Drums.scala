import algebra.Tempo.BeatOps
import algebra.types.{Channel, MidiInt}
import algebra.{Note, Rest}
import cats.effect.IO
import devices.UnoMidiInterface
import eu.timepit.refined.api.RefType.applyRef
import eu.timepit.refined.auto._
import algebra.Events.ScoreOps

object Drums extends PlayApp {
  val channels: List[Channel] = (0 to 15).map(applyRef[Channel].unsafeFrom(_)).toList

  channel = 10
  val Drum1: MidiInt = 36
  val Drum2: MidiInt = 37
  val Drum3: MidiInt = 38
  val Drum4: MidiInt = 44
  val Drum5: MidiInt = 45
  val Drum6: MidiInt = 46

  val bass  = Note(Drum1, 1.beat) + Note(Drum1, 1.beat)
  val snare = Rest(1.semiquaver) + Note(Drum2, 1.beat) + Note(Drum2, 1.beat)
  val s     = snare.events & bass.events

  override def play: IO[Any] =
    devices
      .open(UnoMidiInterface)
      .use { device ⇒
        device << s.durations
      }
}
