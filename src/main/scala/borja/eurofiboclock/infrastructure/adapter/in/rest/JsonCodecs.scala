package borja.eurofiboclock.infrastructure.adapter.in.rest

import io.circe.Encoder

import borja.eurofiboclock.infrastructure.adapter.in.rest.dto.*

/** Hand-written circe encoders. Explicit on purpose: the JSON contract is part of
  * the adapter and should be visible, not conjured by generic derivation.
  */
object JsonCodecs:

  given Encoder[ColombiaTimeResponse] =
    Encoder.forProduct6("zone", "date", "time", "utcOffset", "isoDateTime", "epochSecond")(r =>
      (r.zone, r.date, r.time, r.utcOffset, r.isoDateTime, r.epochSecond)
    )

  given Encoder[FibonacciResponse] =
    Encoder.forProduct3("n", "value", "digits")(r => (r.n, r.value, r.digits))

  given Encoder[CapitalResponse] =
    Encoder.forProduct2("country", "capital")(r => (r.country, r.capital))

  given Encoder[CapitalsResponse] =
    Encoder.forProduct2("count", "capitals")(r => (r.count, r.capitals))

  given Encoder[ErrorResponse] =
    Encoder.forProduct2("error", "message")(r => (r.error, r.message))

  // --- EuroFibo Clock ---

  given Encoder[AlgorithmDescriptorResponse] =
    Encoder.forProduct4("id", "name", "businessQuestion", "catalogueSize")(r =>
      (r.id, r.name, r.businessQuestion, r.catalogueSize)
    )

  given Encoder[AlgorithmsResponse] =
    Encoder.forProduct2("count", "algorithms")(r => (r.count, r.algorithms))

  given Encoder[CapitalSelectionResponse] =
    Encoder.forProduct9(
      "algorithm",
      "fibonacciInput",
      "fibonacciValue",
      "fibonacciFactor",
      "colombianTime",
      "temporalFactor",
      "rawIndex",
      "capitalIndex",
      "selected"
    )(r =>
      (
        r.algorithm,
        r.fibonacciInput,
        r.fibonacciValue,
        r.fibonacciFactor,
        r.colombianTime,
        r.temporalFactor,
        r.rawIndex,
        r.capitalIndex,
        r.selected
      )
    )

  given Encoder[TemporalPassportResponse] =
    Encoder.forProduct9(
      "algorithm",
      "fibonacciInput",
      "fibonacciValue",
      "fibonacciCode",
      "colombianTime",
      "temporalScore",
      "capitalIndex",
      "destination",
      "passportCode"
    )(r =>
      (
        r.algorithm,
        r.fibonacciInput,
        r.fibonacciValue,
        r.fibonacciCode,
        r.colombianTime,
        r.temporalScore,
        r.capitalIndex,
        r.destination,
        r.passportCode
      )
    )

  given Encoder[EuropeanJourneyResponse] =
    Encoder.forProduct11(
      "algorithm",
      "fibonacciInput",
      "fibonacciValue",
      "startIndex",
      "startCapital",
      "colombianTime",
      "temporalDisplacement",
      "effectiveDisplacement",
      "destinationIndex",
      "destinationCapital",
      "path"
    )(r =>
      (
        r.algorithm,
        r.fibonacciInput,
        r.fibonacciValue,
        r.startIndex,
        r.startCapital,
        r.colombianTime,
        r.temporalDisplacement,
        r.effectiveDisplacement,
        r.destinationIndex,
        r.destinationCapital,
        r.path
      )
    )
