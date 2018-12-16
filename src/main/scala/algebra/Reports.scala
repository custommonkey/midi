package algebra

import javax.sound.midi.MidiDevice.Info
import errors.DeviceNotFound
import errors.NoReceivers

object Reports {
  def apply[F[_]](implicit msgs: Reports[F]): Reports[F] = msgs
}

trait Reports[F[_]] {
  def noReceivers(err: NoReceivers): F[Unit]
  def deviceNotFound(err: DeviceNotFound): F[Unit]
  def listDevices(devices: List[Info]): F[Unit]
}
