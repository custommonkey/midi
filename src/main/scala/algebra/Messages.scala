package algebra

import cats.Show
import eu.timepit.refined.W
import eu.timepit.refined.api.Refined
import eu.timepit.refined.numeric.Interval.Closed
import eu.timepit.refined.auto._
import javax.sound.midi.ShortMessage
import javax.sound.midi.ShortMessage._
import algebra.types.{Channel, MidiInt, Msg}

import scala.util.{Random => ScalaRandom}

object Messages {

  sealed class Status(private[Messages] val value: Int Refined Closed[W.`0`.T, W.`255`.T])
  sealed trait OnOff extends Status {
    def note: MidiInt
    def channel: Channel
  }

  case object TuneRequest                             extends Status(TUNE_REQUEST)
  case object Eox                                     extends Status(END_OF_EXCLUSIVE)
  case object TimingClock                             extends Status(TIMING_CLOCK)
  case object Undefined                               extends Status(0xF9)
  case object Start                                   extends Status(START)
  case object Continue                                extends Status(CONTINUE)
  case object Stop                                    extends Status(STOP)
  case object Undefined2                              extends Status(0xFD)
  case object ActiveSensing                           extends Status(ACTIVE_SENSING)
  case object SystemReset                             extends Status(SYSTEM_RESET)
  case object MTCQuarterFrame                         extends Status(0xF1)
  case object SongSelect                              extends Status(SONG_SELECT)
  case object SongPositionPointer                     extends Status(SONG_POSITION_POINTER)
  case class NoteOn(note: MidiInt, channel: Channel)  extends Status(NOTE_ON) with OnOff
  case class NoteOff(note: MidiInt, channel: Channel) extends Status(NOTE_OFF) with OnOff
  case class ProgramChange(data: Int)                 extends Status(PROGRAM_CHANGE)

  object ProgramChange {
    implicit val msg: Msg[ProgramChange]           = pc => mkMsg(pc, pc.data, 0)
    implicit def show[T <: ProgramChange]: Show[T] = pc => s"program change ${pc.data}"
    implicit val random: Random[ProgramChange]     = () => ProgramChange(ScalaRandom.nextInt(127))
  }

  object OnOff {
    implicit val msg: Msg[OnOff] = (n: OnOff) => mkMsg(n, n.channel, n.note, 100)
  }

  object NoteOff {
    implicit val show: Show[NoteOff] = n => s"off $n"
  }

  object NoteOn {
    implicit val show: Show[NoteOn] = n => s"on $n"
  }

  class ControlChange(private[Messages] val value: Int, private[Messages] val data: Int)
  case object AllOff        extends ControlChange(120, 0)
  case object BankSelectMSB extends ControlChange(0, 0)
  case object BankSelectLSB extends ControlChange(32, 0)

  object ControlChange {
    implicit def msg[T <: ControlChange]: Msg[T] =
      cc => new ShortMessage(CONTROL_CHANGE, cc.value, cc.data)
    implicit def show[T <: ControlChange]: Show[T] = cc => s"CC $cc"
  }

  private def mkMsg(status: Status, chl: Int, data1: Int, data2: Int) =
    new ShortMessage(status.value, chl, data1, data2)

  private def mkMsg(status: Status, data1: Int, data2: Int) =
    new ShortMessage(status.value, data1, data2)

}
