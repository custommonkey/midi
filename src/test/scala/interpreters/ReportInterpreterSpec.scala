package interpreters
import algebra.Arbitraries
import algebra.errors.{DeviceNotFound, NoReceivers}
import cats.Id
import javax.sound.midi.MidiDevice.Info
import org.scalatest.prop.PropertyChecks
import org.scalatest.{Matchers, WordSpec}
import Console.YELLOW
import Console.RESET

class ReportInterpreterSpec extends WordSpec with PropertyChecks with Arbitraries with Matchers {

  "ReportInterpreter" should {

    "noReceivers" in {
      forAll { error: NoReceivers ⇒
        val print = new StringPrintInterpreter[Id]
        val i     = new ReportInterpreter[Id](print)
        import i._

        report(error) should be(())

        print.string should be(s"NoReceivers ${error.info.toNonEmptyList.toList.mkString(", ")}")
      }
    }

    "listDevices" in {
      forAll { devices: List[Info] ⇒
        val print = new StringPrintInterpreter[Id]
        val i     = new ReportInterpreter[Id](print)
        import i._

        report(devices) should be(())

        print.string should be(
          s"devices {${devices.map(a ⇒ a.getName + " -- " + a.getDescription).mkString("\n")}}")
      }
    }

    "deviceNotFound" in {
      forAll { error: DeviceNotFound ⇒
        val print = new StringPrintInterpreter[Id]
        val i     = new ReportInterpreter[Id](print)
        import i._

        report(error) should be(())

        print.string should be(s"${YELLOW}Cannot open '${error.name}'$RESET")
      }
    }

  }
}
