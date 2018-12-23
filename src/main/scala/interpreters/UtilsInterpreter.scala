package interpreters

import algebra.Messages.ProgramChange
import algebra.{Device, MidiApi, Print, RandomApi, Utils}
import cats.Monad
import cats.implicits._
import interpreters.Shows._

class UtilsInterpreter[F[_]: Monad](api: MidiApi[F], println: Print[F], random: RandomApi[F])
    extends Utils[F] {

//TODO:  "implement as Device.randomProgram"
  override def randomProgram(device: Device[F]): F[Unit] =
    random[ProgramChange] >>= device.send[ProgramChange]

  override def showInstruments: F[Unit] =
    api.instruments >>= { _.traverse(println(_)).void }

  override def showDevices: F[Unit] =
    api.midiDeviceInfo >>= { _.traverse(println(_)).void }
}
