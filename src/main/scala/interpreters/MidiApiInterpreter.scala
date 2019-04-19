package interpreters

import algebra.{MidiApi, Print, Receiver}
import cats.{ApplicativeError, Eval}
import javax.sound.midi.MidiDevice.Info
import javax.sound.midi.{Instrument, MidiDevice, MidiSystem}

class MidiApiInterpreter[F[_]](print: Print[F])(implicit F: ApplicativeError[F, Throwable])
    extends MidiApi[F] {

  override def midiDevice(info: Info): F[MidiDevice] =
    F.catchNonFatal(MidiSystem.getMidiDevice(info))

  override def receiver(device: MidiDevice): F[Receiver[F]] =
    F.catchNonFatalEval(Eval.later(new ReceiverInterpreter(device.getReceiver, print)))

  override val midiDeviceInfo: F[List[Info]] = F.catchNonFatal(MidiSystem.getMidiDeviceInfo.toList)

  override val instruments: F[List[Instrument]] =
    F.catchNonFatal(MidiSystem.getSynthesizer.getAvailableInstruments.toList)

}
