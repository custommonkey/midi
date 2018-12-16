import eu.timepit.refined.api.Refined
import eu.timepit.refined.auto._
import eu.timepit.refined.numeric.Positive

import scala.concurrent.duration._

class Tempo(bpm: Int Refined Positive) {

  private val xx = 1.minute / bpm.toLong

  object implicits {

    implicit class BeatOps[T](n: T)(implicit numeric: Numeric[T]) {
      def beats: FiniteDuration      = xx * numeric.toLong(n)
      def beat: FiniteDuration       = beats
      def crotchet: FiniteDuration   = beats
      def quaver: FiniteDuration     = beats / 2
      def semiquaver: FiniteDuration = beats / 4
      def minim: FiniteDuration      = beats * 2
    }

  }
}

object Tempo {
  type Bpm = Int Refined Positive
}
