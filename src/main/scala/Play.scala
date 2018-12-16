import algebra.Messages.ProgramChange
import algebra.algebra.Device
import cats.Show
import cats.effect.IO
import cats.effect.IO.sleep
import cats.implicits._
import eu.timepit.refined.auto._
import javax.sound.midi.{Instrument, Patch}

import scala.util.Random

object Play extends PlayApp {

  implicit val showPath :Show[Patch] = p ⇒ s"${p.getBank} + ${p.getProgram}"
  implicit val showInstrument: Show[Instrument] = i ⇒ {
    val xxx = i match {
      case _: com.sun.media.sound.DLSInstrument ⇒ "dls"
      case _: com.sun.media.sound.SF2Instrument ⇒ "sf2"
      case _: com.sun.media.sound.SimpleInstrument ⇒ "simple"
    }
    show"$xxx ${i.toString} ${i.getName} ${i.getPatch}"
  }

  override def play(device: Device[IO]): IO[Unit] = {

    val crap: IO[Unit] = api.instruments >>= (_.traverse(println(_)).void)

    import device._

    val tempo = new Tempo(160)
    import tempo.implicits._

    val notes  = List.fill(4)(Random.nextInt(44) + 30)
    val things = List.fill(8)(notes).flatten

    def boom(i: Int) =
      bleep(i, 1.semiquaver) >>
        sleep(1.semiquaver)

    val aa = sleep(1.beat) >> things.traverse(boom).void

    val bb = things.map(_ + 12).traverse(boom).void

    crap >>
      send(ProgramChange(Random.nextInt(127))) >>
      (for {
        fiber1 ← aa.start
        fiber2 ← bb.start
        _      ← fiber1.join
        _      ← fiber2.join
      } yield ())
  }.void
}
