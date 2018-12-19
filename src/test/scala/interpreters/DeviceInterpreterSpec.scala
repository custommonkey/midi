package interpreters

import algebra.Messages.ProgramChange
import algebra.types.Msg
import algebra.{MidiApi, Receiver}
import cats.Show
import cats.effect.Timer
import cats.implicits._
import javax.sound.midi.MidiDevice
import org.scalacheck.ScalacheckShapeless
import org.scalamock.scalatest.MockFactory
import org.scalatest.{MustMatchers, WordSpec}
import org.scalatest.prop.PropertyChecks

class DeviceInterpreterSpec
    extends WordSpec
    with MockFactory
    with MustMatchers
    with PropertyChecks
    with ScalacheckShapeless {

  type Error[T] = Either[Throwable, T]

  private val midiDevice = mock[MidiDevice]
  private val api        = mock[MidiApi[Error]]
  private val timer      = mock[Timer[Error]]
  private val receiver   = mock[Receiver[Error]]

  "Device" should {
    "do something" in {

      (api.receiver _)
        .expects(midiDevice)
        .returning(Right(receiver))

      val device = new DeviceInterpreter[Error](midiDevice, api, timer)

      forAll { msg: ProgramChange ⇒
        (receiver
          .send(_: ProgramChange, _: Long)(_: Msg[ProgramChange], _: Show[ProgramChange]))
          .expects(msg, 0l, Msg[ProgramChange], Show[ProgramChange])
          .returning(Right(()))

        device.send(msg) must be(Right(()))
      }
    }
  }

}
