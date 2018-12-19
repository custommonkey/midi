package algebra

import cats.data.NonEmptyChain
import javax.sound.midi.MidiDevice.Info

object errors {

  sealed trait AppError extends Exception

  case class DeviceNotFound(name: String)           extends AppError
  case class NoReceivers(info: NonEmptyChain[Info]) extends AppError

}
