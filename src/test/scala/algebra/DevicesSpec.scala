package algebra
import algebra.Messages.ProgramChange
import algebra.types.{Channel, Msg}
import cats.Show
import cats.effect._
import devices.Gervill
import org.scalacheck.ScalacheckShapeless
import org.scalatest.prop.PropertyChecks
import org.scalatest.{MustMatchers, WordSpec}

import scala.concurrent.duration.FiniteDuration

class DevicesSpec extends WordSpec with MustMatchers with ScalacheckShapeless with PropertyChecks {

  "devices" should {
    "do things" in {

      val devices = new Devices[IO] {
        val device = new Device[IO] {
          override def send[T: Msg: Show](msg: T): IO[Unit]                              = IO.unit
          override def <<(events: Events[FiniteDuration])(implicit c: Channel): IO[Unit] = ???
        }
        override def open(name: DeviceDef): Resource[IO, Device[IO]] = Resource.pure(device)
      }

      forAll { pc: ProgramChange ⇒
        devices
          .open(Gervill)
          .use { device ⇒
            device.send(pc)
          }
          .unsafeRunSync()

      }
    }
  }

}
