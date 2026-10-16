package borja.eurofiboclock

import cats.effect.IO
import munit.CatsEffectSuite

import java.time.Instant

import borja.eurofiboclock.application.service.ColombiaTimeService
import borja.eurofiboclock.domain.port.out.ClockPort

final class ColombiaTimeServiceSuite extends CatsEffectSuite:

  private def fixedClock(instant: Instant): ClockPort[IO] =
    new ClockPort[IO]:
      def now: IO[Instant] = IO.pure(instant)

  test("projects the instant onto America/Bogota (UTC-5, no DST)") {
    // 2026-08-31T12:00:00Z  ->  2026-08-31T07:00:00-05:00 in Bogota
    val service = new ColombiaTimeService[IO](fixedClock(Instant.parse("2026-08-31T12:00:00Z")))
    service.currentTime.map { t =>
      assertEquals(t.zoneId.getId, "America/Bogota")
      assertEquals(t.offset.getId, "-05:00")
      assertEquals(t.date.toString, "2026-08-31")
      assertEquals(t.time.toString, "07:00")
      assertEquals(t.epochSecond, Instant.parse("2026-08-31T12:00:00Z").getEpochSecond)
    }
  }
