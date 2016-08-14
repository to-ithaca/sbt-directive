package object cli {
  type Attempt[A] = Either[Throwable, A]
}
