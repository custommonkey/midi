package interpreters

import cats.Show
import cats.implicits._
import javax.sound.midi.MidiDevice.Info
import javax.sound.midi.ShortMessage.CONTROL_CHANGE
import javax.sound.midi.{Instrument, MidiMessage, Patch}

object Shows {
  implicit val showPath: Show[Patch] = p => s"bank=${p.getBank}, program=${p.getProgram}"

  implicit val showInstrument: Show[Instrument] = instrument => {
    val name = instrument.getName.split("[ ()]").map(_.capitalize).mkString
    show"object $name extends ProgramChange(${instrument.getPatch.getProgram}) // ${instrument.getPatch.getBank}"
  }

  implicit val showInfo: Show[Info] = info =>
    show"${info.getName}, ${info.getDescription}, ${info.getVendor}, ${info.getVersion}"

  implicit val showMsg: Show[MidiMessage] = m => {
    val status = m.getStatus match {
      case CONTROL_CHANGE => "CC"
      case s              => s.toString
    }
    s"$status:${m.getMessage.mkString(":")}"
  }

}
