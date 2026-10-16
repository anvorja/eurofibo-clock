package borja.eurofiboclock.infrastructure.adapter.in.rest.dto

import borja.eurofiboclock.domain.model.{ColombiaTime, EuropeanCapital, FibonacciResult}

/** Transport-facing representations. They exist so the domain model never has to
  * care about JSON shape, field names or string formatting.
  */

final case class ColombiaTimeResponse(
    zone: String,
    date: String,
    time: String,
    utcOffset: String,
    isoDateTime: String,
    epochSecond: Long
)

object ColombiaTimeResponse:
  def from(t: ColombiaTime): ColombiaTimeResponse =
    ColombiaTimeResponse(
      zone = t.zoneId.getId,
      date = t.date.toString,
      time = t.time.toString,
      utcOffset = t.offset.getId,
      isoDateTime = t.isoDateTime,
      epochSecond = t.epochSecond
    )

final case class FibonacciResponse(n: Int, value: String, digits: Int)

object FibonacciResponse:
  def from(r: FibonacciResult): FibonacciResponse =
    val rendered = r.value.toString
    FibonacciResponse(r.n, rendered, rendered.length)

final case class CapitalResponse(country: String, capital: String)

object CapitalResponse:
  def from(c: EuropeanCapital): CapitalResponse = CapitalResponse(c.country, c.capital)

final case class CapitalsResponse(count: Int, capitals: List[CapitalResponse])

object CapitalsResponse:
  def from(cs: List[EuropeanCapital]): CapitalsResponse =
    CapitalsResponse(cs.size, cs.map(CapitalResponse.from))

final case class ErrorResponse(error: String, message: String)
