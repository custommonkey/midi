package interpreters

import algebra.Messages.ProgramChange
import algebra.algebra.{MidiApi, Receiver}
import cats.effect.{IO, Timer}
import javax.sound.midi.MidiDevice
import org.scalacheck.ScalacheckShapeless
import org.scalamock.scalatest.MockFactory
import org.scalatest.WordSpec
import org.scalatest.prop.PropertyChecks

class DeviceInterpreterSpec
    extends WordSpec
    with MockFactory
    with PropertyChecks
    with ScalacheckShapeless {

  val device   = mock[MidiDevice]
  val api      = mock[MidiApi[IO]]
  val timer    = mock[Timer[IO]]
  val receiver = mock[Receiver[IO]]

  "Device" should {
    "do something" in {

      api.receiver _ expects device returning IO(mock[Receiver[IO]])

      val i = new DeviceInterpreter[IO](device, api, timer)

      forAll { msg: ProgramChange ⇒
        (receiver.send[ProgramChange](_, _)).expects(msg, 0).returning(IO.unit)
        i.send(msg)
      }
    }
  }

}
