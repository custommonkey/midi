package interpreters

import algebra.Print
import algebra.algebra.{MidiApi, Msg, Receiver}
import cats.{ApplicativeError, Eval, Monad, Show}
import javax.sound.midi.MidiDevice.Info
import javax.sound.midi.{Instrument, MidiDevice, MidiSystem}
import cats.implicits._

class MidiApiIntepreter[F[_]: Monad](println: Print[F])(implicit F: ApplicativeError[F, Throwable])
    extends MidiApi[F] {

  class ReceiverInterpreter(r: javax.sound.midi.Receiver) extends Receiver[F] {
    override def send[T: Msg: Show](message: T, i: Long): F[Unit] =
      println(message) >> F.catchNonFatal(r.send(implicitly[Msg[T]].apply(message), i))
  }

  override def midiDevice(info: Info): F[MidiDevice] =
    F.catchNonFatal(MidiSystem.getMidiDevice(info))
  override def receiver(device: MidiDevice): F[Receiver[F]] =
    F.catchNonFatalEval(Eval.later(new ReceiverInterpreter(device.getReceiver)))
  override val midiDeviceInfo: F[List[Info]] = F.catchNonFatal(MidiSystem.getMidiDeviceInfo.toList)
  override val instruments: F[List[Instrument]] =
    F.catchNonFatal(MidiSystem.getSynthesizer.getAvailableInstruments.toList)

}
