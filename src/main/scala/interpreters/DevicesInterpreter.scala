package interpreters

import algebra.Messages
import algebra.algebra.{Device, Devices, MidiApi}
import algebra.errors.NoReceivers
import cats.MonadError
import cats.effect.{Resource, Timer}
import cats.implicits._
import javax.sound.midi.MidiDevice
import javax.sound.midi.MidiDevice.Info

class DevicesInterpreter[F[_]](api: MidiApi[F])(implicit F: MonadError[F, Throwable],
                                                timer: Timer[F])
    extends Devices[F] {

  def findReceivers(list: List[Info]): F[MidiDevice] =
    list
      .traverse(api.midiDevice)
      .flatMap {
        _.find(_.getMaxReceivers != 0) match {
          case Some(value) ⇒ F.pure(value)
          case None        ⇒ F.raiseError(NoReceivers(list))
        }
      }

  private def findInfo(name: String): F[List[Info]] =
    api.midiDeviceInfo.map(_.filter(_.getName == name))

  override def open(name: String): Resource[F, Device[F]] = {
    Resource
      .make {
        (findInfo(name) >>= findReceivers)
          .map(new DeviceInterpreter(_, api, timer))
          .flatTap(_.open())
      } { d ⇒
        d.send(Messages.AllOff) >> d.close()
      }
      .flatMap(Resource.pure(_))
  }

}
