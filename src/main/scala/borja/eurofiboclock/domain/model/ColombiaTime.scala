package borja.eurofiboclock.domain.model

import java.time.{LocalDate, LocalTime, ZoneId, ZoneOffset, ZonedDateTime}

/** Snapshot of the wall-clock time in Colombia.
  *
  * Pure domain value object: it carries no framework or serialization concerns,
  * only the concepts the business cares about.
  */
final case class ColombiaTime(
    zoneId: ZoneId,
    date: LocalDate,
    time: LocalTime,
    offset: ZoneOffset,
    isoDateTime: String,
    epochSecond: Long
)

object ColombiaTime:

  /** The single zone this use case is defined against. */
  val Zone: ZoneId = ZoneId.of("America/Bogota")

  /** Builds the domain value from an absolute instant projected onto Colombia's zone. */
  def fromZoned(zdt: ZonedDateTime): ColombiaTime =
    ColombiaTime(
      zoneId = zdt.getZone,
      date = zdt.toLocalDate,
      time = zdt.toLocalTime,
      offset = zdt.getOffset,
      isoDateTime = zdt.toOffsetDateTime.toString,
      epochSecond = zdt.toEpochSecond
    )
