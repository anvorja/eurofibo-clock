package borja.eurofiboclock.domain

import munit.FunSuite

import java.time.Instant

import borja.eurofiboclock.domain.model.WallClock

/** Unit tests for [[WallClock]] (formatting + projection onto America/Bogota). */
final class WallClockSuite extends FunSuite:

  test("label is zero-padded HH:MM:SS") {
    assertEquals(WallClock(13, 40, 34).label, "13:40:34")
    assertEquals(WallClock(1, 2, 3).label, "01:02:03")
    assertEquals(WallClock(0, 0, 0).label, "00:00:00")
    assertEquals(WallClock(23, 59, 59).label, "23:59:59")
  }

  test("compact is zero-padded HHMMSS (used by the A2 passport code)") {
    assertEquals(WallClock(13, 40, 34).compact, "134034")
    assertEquals(WallClock(2, 3, 4).compact, "020304")
    assertEquals(WallClock(0, 0, 0).compact, "000000")
  }

  test("fromInstant projects onto America/Bogota (UTC-5, no DST)") {
    // 2026-09-01T18:40:34Z -> 13:40:34 in Bogota
    assertEquals(
      WallClock.fromInstant(Instant.parse("2026-09-01T18:40:34Z")),
      WallClock(13, 40, 34)
    )
  }

  test("fromInstant rolls the local time back across midnight") {
    // 2026-09-01T02:30:00Z -> 2026-08-31T21:30:00 in Bogota
    assertEquals(
      WallClock.fromInstant(Instant.parse("2026-09-01T02:30:00Z")),
      WallClock(21, 30, 0)
    )
  }

  test("fromInstant at the Bogota day boundary") {
    // 2026-09-01T05:00:00Z -> 00:00:00 in Bogota
    assertEquals(
      WallClock.fromInstant(Instant.parse("2026-09-01T05:00:00Z")),
      WallClock(0, 0, 0)
    )
  }
