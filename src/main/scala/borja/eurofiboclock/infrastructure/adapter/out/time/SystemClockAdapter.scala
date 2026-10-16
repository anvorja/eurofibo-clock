package borja.eurofiboclock.infrastructure.adapter.out.time

import cats.effect.kernel.Clock
import cats.syntax.functor.*

import java.time.Instant

import borja.eurofiboclock.domain.port.out.ClockPort

/** Driven adapter: satisfies [[ClockPort]] with the real system clock, obtained
  * through cats-effect's `Clock` so it stays inside `F` and referentially
  * transparent.
  */
final class SystemClockAdapter[F[_]: Clock: cats.Functor] extends ClockPort[F]:
  def now: F[Instant] = Clock[F].realTime.map(d => Instant.ofEpochMilli(d.toMillis))

object SystemClockAdapter:
  def apply[F[_]: Clock: cats.Functor]: SystemClockAdapter[F] = new SystemClockAdapter[F]
