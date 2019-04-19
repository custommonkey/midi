package interpreters

import algebra.errors.{DeviceNotFound, NoReceivers}
import algebra.{Print, Reports}
import cats.Show
import cats.implicits._
import javax.sound.midi.MidiDevice.Info

import scala.Console.{RESET, YELLOW}

class ReportInterpreter[F[_]](println: Print[F]) extends Reports[F] {

  private def info(i: Info) = i.getName + " -- " + i.getDescription

  override def report[T](e: T)(implicit show: Show[T]): F[Unit] =
    println(show.show(e))

  implicit val noReceivers: Show[NoReceivers] = err =>
    s"NoReceivers ${err.info.toList.mkString(", ")}"

  implicit val deviceNotFound: Show[DeviceNotFound] = err =>
    s"${YELLOW}Cannot open '${err.name}'$RESET"

  implicit val listDevices: Show[List[Info]] = l => "devices {" + l.map(info).mkString("\n") + "}"
}
