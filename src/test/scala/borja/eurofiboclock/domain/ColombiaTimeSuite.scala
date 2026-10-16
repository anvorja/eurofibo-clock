package borja.eurofiboclock.domain

import munit.FunSuite

import java.time.{Instant, ZoneOffset}

import borja.eurofiboclock.domain.model.ColombiaTime

/** Unit tests for the [[ColombiaTime]] domain value object. */
final class ColombiaTimeSuite extends FunSuite:

  test("Zone is America/Bogota") {
    assertEquals(ColombiaTime.Zone.getId, "America/Bogota")
  }

  test("fromZoned splits an instant into date/time/offset for Bogota") {
    // 2026-08-31T12:00:00Z -> 2026-08-31T07:00:00-05:00
    val zdt = Instant.parse("2026-08-31T12:00:00Z").atZone(ColombiaTime.Zone)
    val t   = ColombiaTime.fromZoned(zdt)

    assertEquals(t.zoneId, ColombiaTime.Zone)
    assertEquals(t.date.toString, "2026-08-31")
    assertEquals(t.time.toString, "07:00")
    assertEquals(t.offset, ZoneOffset.ofHours(-5))
    assertEquals(t.isoDateTime, "2026-08-31T07:00-05:00")
    assertEquals(t.epochSecond, Instant.parse("2026-08-31T12:00:00Z").getEpochSecond)
  }

  test("epochSecond is independent of the zone projection") {
    val instant = Instant.parse("2026-01-15T23:45:10Z")
    val t       = ColombiaTime.fromZoned(instant.atZone(ColombiaTime.Zone))
    assertEquals(t.epochSecond, instant.getEpochSecond)
    assertEquals(t.date.toString, "2026-01-15")
    assertEquals(t.time.toString, "18:45:10")
  }
