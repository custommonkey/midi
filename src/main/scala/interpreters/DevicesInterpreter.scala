package interpreters

import algebra.errors.{DeviceNotFound, NoReceivers}
import algebra.{Device, DeviceDef, Devices, Messages, MidiApi}
import cats.MonadError
import cats.data.NonEmptyChain
import cats.effect.{Resource, Timer}
import cats.implicits._
import javax.sound.midi.MidiDevice
import javax.sound.midi.MidiDevice.Info

class DevicesInterpreter[F[_]](api: MidiApi[F], println: PrintInterpreter[F])(
    implicit F: MonadError[F, Throwable],
    timer: Timer[F])
    extends Devices[F] {

  def findReceivers(list: NonEmptyChain[Info]): F[MidiDevice] = list.traverse(api.midiDevice) >>= {
    _.find(_.getMaxReceivers != 0) match {
      case Some(value) ⇒ F.pure(value)
      case None        ⇒ F.raiseError(NoReceivers(list))
    }
  }

  private def findInfo(name: String): F[NonEmptyChain[Info]] =
    api.midiDeviceInfo
      .map(_.filter(_.getName == name))
      .map(NonEmptyChain.fromSeq) >>= {
      case Some(value) ⇒ value.pure[F]
      case None        ⇒ F.raiseError(DeviceNotFound(name))
    }

  override def open(device: DeviceDef): Resource[F, Device[F]] =
    Resource
      .make {
        (findInfo(device.name) >>= findReceivers)
          .map(new DeviceInterpreter(_, api, timer, println))
          .flatTap(_.open)
      } { d ⇒
        d.send(Messages.AllOff) >> d.close
      } >>= (Resource.pure(_))

}
