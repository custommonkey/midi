import algebra.algebra.Devices

object Devices {
  def apply[F[_]](implicit devices: Devices[F]): Devices[F] = devices
}
