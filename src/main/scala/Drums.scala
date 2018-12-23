import algebra.Tempo.BeatOps
import algebra.types.{Channel, Thing}
import cats.effect.IO
import cats.implicits._
import devices.UnoMidiInterface
import eu.timepit.refined.api.RefType.applyRef
import eu.timepit.refined.auto._

object Drums extends PlayApp {
  val channels: List[Channel] = (0 to 15).map(applyRef[Channel].unsafeFrom(_)).toList

  channel = 10
  val Drum1 = 36
  val Drum2 = 37
  val Drum3 = 38
  val Drum4 = 44
  val Drum5 = 45
  val Drum6 = 46

  val bass  = List(Thing(Drum1                      → 1.beat), Thing(Drum1 → 1.beat))
  val snare = List(Thing(1.semiquaver), Thing(Drum2 → 1.beat), Thing(Drum2 → 1.beat))

  override def play: IO[Any] =
    devices
      .open(UnoMidiInterface)
      .use { device ⇒
        (device << bass) *> (device << snare)
      }
}
