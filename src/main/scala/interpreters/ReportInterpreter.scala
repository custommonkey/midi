package interpreters

import algebra.{Print, Reports}
import algebra.types.Report
import algebra.errors.{DeviceNotFound, NoReceivers}
import cats.implicits._
import javax.sound.midi.MidiDevice.Info

import scala.Console.{RESET, YELLOW}

class ReportInterpreter[F[_]](println: Print[F]) extends Reports[F] {

  override def report[E: Report](e: E): F[Unit] =
    println(implicitly[Report[E]].apply(e))

  implicit val noReceivers: Report[NoReceivers] = err ⇒
    s"NoReceivers ${err.info.toList.mkString(", ")}"

  implicit val deviceNotFound: Report[DeviceNotFound] = err ⇒
    s"${YELLOW}Cannot open '${err.name}'$RESET"

  private def info(i: Info) = i.getName + " -- " + i.getDescription

  implicit val listDevices: Report[List[Info]] = l ⇒ "devices {" + l.map(info).mkString("\n") + "}"
}
