package interpreters

import algebra.Messages.ProgramChange
import algebra.types.{Msg, Sleep}
import algebra.{MidiApi, Receiver}
import cats.implicits._
import javax.sound.midi.MidiDevice
import org.scalacheck.ScalacheckShapeless
import org.scalamock.scalatest.MockFactory
import org.scalatest.prop.PropertyChecks
import org.scalatest.{MustMatchers, WordSpec}

class DeviceInterpreterSpec
    extends WordSpec
    with MockFactory
    with MustMatchers
    with PropertyChecks
    with ScalacheckShapeless {

  type Error[T] = Either[Throwable, T]

  private val midiDevice = mock[MidiDevice]
  private val api        = mock[MidiApi[Error]]
  private val sleep      = mock[Sleep[Error]]
  private val receiver   = mock[Receiver[Error]]

  "Device" should {
    "do something" in {

      (api.receiver _)
        .expects(midiDevice)
        .returning(Right(receiver))

      val device = new DeviceInterpreter[Error](midiDevice, api, sleep)

      forAll { msg: ProgramChange ⇒
        (receiver(_: ProgramChange, _: Long)(_: Msg[ProgramChange]))
          .expects(msg, 0l, Msg[ProgramChange])
          .returning(Right(()))

        device(msg) must be(Right(()))
      }
    }
  }

}
