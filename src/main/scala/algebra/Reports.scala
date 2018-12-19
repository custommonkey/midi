object Reports {
  def apply[F[_]](implicit msgs: algebra.Reports[F]): algebra.Reports[F] = msgs
}
