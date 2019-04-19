package algebra

import algebra.types.MidiInt
import cats.Functor

case class Event[T](e: EvType, n: MidiInt, time: T)

object Event {
  implicit val functorEvent: Functor[Event] = new Functor[Event] {
    override def map[A, B](fa: Event[A])(f: A ⇒ B): Event[B] = fa match {
      case Event(evType, midiInt, t) ⇒ Event(evType, midiInt, f(t))
    }
  }
}
