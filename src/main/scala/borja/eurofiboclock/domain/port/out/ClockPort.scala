package borja.eurofiboclock.domain.port.out

import java.time.Instant

/** Driven port: the hexagon's only window onto "what time is it right now".
  *
  * Keeping this behind a port makes the time use case deterministic in tests
  * (inject a fixed instant) and keeps `java.time`'s system clock out of the core.
  */
trait ClockPort[F[_]]:
  def now: F[Instant]
