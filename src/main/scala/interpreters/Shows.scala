package interpreters

import cats.Show
import cats.implicits._
import javax.sound.midi.{Instrument, Patch}

object Shows {
  implicit val showPath: Show[Patch] = p ⇒ s"bank=${p.getBank}, program=${p.getProgram}"

  implicit val showInstrument: Show[Instrument] = instrument ⇒ {
    val name = instrument.getName.split("[ ()]").map(_.capitalize).mkString
    show"object $name extends ProgramChange(${instrument.getPatch.getProgram}) // ${instrument.getPatch.getBank}"
  }

}
