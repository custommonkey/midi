package interpreters
import algebra.{Print, Reports}
import algebra.errors.{DeviceNotFound, NoReceivers}
import cats.ApplicativeError
import cats.implicits._
import javax.sound.midi.MidiDevice.Info

import scala.Console.{RESET, YELLOW}

class ReportInterpreter[F[_]](println: Print[F])(implicit a: ApplicativeError[F, Throwable])
    extends Reports[F] {

  override def noReceivers(err: NoReceivers): F[Unit] =
    println(s"NoReceivers ${err.info.mkString(", ")}")

  override def deviceNotFound(err: DeviceNotFound): F[Unit] =
    println(s"${YELLOW}Cannot open '${err.name}'$RESET")

  private def info(i: Info) =
    println(i.getName) *>
      println(" -- " + i.getDescription)

  override def listDevices(l: List[Info]): F[Unit] = l.traverse(info).void
}
