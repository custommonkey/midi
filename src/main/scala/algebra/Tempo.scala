package algebra

import eu.timepit.refined.api.Refined
import eu.timepit.refined.auto._
import eu.timepit.refined.numeric.Positive

import scala.concurrent.duration._

case class Tempo(bpm: Int Refined Positive) {

  private val xx = 1.minute / bpm.toLong

}

object Tempo {
  type Bpm = Int Refined Positive
  implicit class BeatOps[T](n: T)(implicit numeric: Numeric[T]) {
    def beats(implicit tempo: Tempo): FiniteDuration      = tempo.xx * numeric.toLong(n)
    def beat(implicit tempo: Tempo): FiniteDuration       = beats
    def crotchet(implicit tempo: Tempo): FiniteDuration   = beats
    def quaver(implicit tempo: Tempo): FiniteDuration     = beats / 2
    def semiquaver(implicit tempo: Tempo): FiniteDuration = beats / 4
    def minim(implicit tempo: Tempo): FiniteDuration      = beats * 2
  }
}
