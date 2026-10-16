package borja.eurofiboclock.infrastructure.adapter.in.rest.dto

import borja.eurofiboclock.domain.algorithm.AlgorithmDescriptor
import borja.eurofiboclock.domain.model.{CapitalSelection, EuropeanJourney, TemporalPassport}

/** Transport-facing representations for the EuroFibo Clock algorithms.
  *
  * `BigInt` domain values are rendered as strings so the JSON stays exact for
  * large Fibonacci indices, consistent with `FibonacciResponse.value`.
  */

final case class AlgorithmDescriptorResponse(
    id: String,
    name: String,
    businessQuestion: String,
    catalogueSize: Int
)

object AlgorithmDescriptorResponse:
  def from(d: AlgorithmDescriptor): AlgorithmDescriptorResponse =
    AlgorithmDescriptorResponse(d.id.slug, d.name, d.businessQuestion, d.catalogueSize)

final case class AlgorithmsResponse(count: Int, algorithms: List[AlgorithmDescriptorResponse])

object AlgorithmsResponse:
  def from(ds: List[AlgorithmDescriptor]): AlgorithmsResponse =
    AlgorithmsResponse(ds.size, ds.map(AlgorithmDescriptorResponse.from))

/** A1 - Fibonacci Colombian Capital Selector. */
final case class CapitalSelectionResponse(
    algorithm: String,
    fibonacciInput: Int,
    fibonacciValue: String,
    fibonacciFactor: String,
    colombianTime: String,
    temporalFactor: Int,
    rawIndex: String,
    capitalIndex: Int,
    selected: CapitalResponse
)

object CapitalSelectionResponse:
  def from(s: CapitalSelection): CapitalSelectionResponse =
    CapitalSelectionResponse(
      algorithm = "a1",
      fibonacciInput = s.fibonacciInput,
      fibonacciValue = s.fibonacciValue.toString,
      fibonacciFactor = s.fibonacciFactor.toString,
      colombianTime = s.colombianTime,
      temporalFactor = s.temporalFactor,
      rawIndex = s.rawIndex.toString,
      capitalIndex = s.capitalIndex,
      selected = CapitalResponse.from(s.selected)
    )

/** A2 - Fibonacci Temporal Passport. */
final case class TemporalPassportResponse(
    algorithm: String,
    fibonacciInput: Int,
    fibonacciValue: String,
    fibonacciCode: String,
    colombianTime: String,
    temporalScore: String,
    capitalIndex: Int,
    destination: CapitalResponse,
    passportCode: String
)

object TemporalPassportResponse:
  def from(p: TemporalPassport): TemporalPassportResponse =
    TemporalPassportResponse(
      algorithm = "a2",
      fibonacciInput = p.fibonacciInput,
      fibonacciValue = p.fibonacciValue.toString,
      fibonacciCode = p.fibonacciCode.toString,
      colombianTime = p.colombianTime,
      temporalScore = p.temporalScore.toString,
      capitalIndex = p.capitalIndex,
      destination = CapitalResponse.from(p.destination),
      passportCode = p.passportCode
    )

/** A3 - Viajero Temporal Europeo. */
final case class EuropeanJourneyResponse(
    algorithm: String,
    fibonacciInput: Int,
    fibonacciValue: String,
    startIndex: Int,
    startCapital: CapitalResponse,
    colombianTime: String,
    temporalDisplacement: Long,
    effectiveDisplacement: Int,
    destinationIndex: Int,
    destinationCapital: CapitalResponse,
    path: List[CapitalResponse]
)

object EuropeanJourneyResponse:
  def from(j: EuropeanJourney): EuropeanJourneyResponse =
    EuropeanJourneyResponse(
      algorithm = "a3",
      fibonacciInput = j.fibonacciInput,
      fibonacciValue = j.fibonacciValue.toString,
      startIndex = j.startIndex,
      startCapital = CapitalResponse.from(j.startCapital),
      colombianTime = j.colombianTime,
      temporalDisplacement = j.temporalDisplacement,
      effectiveDisplacement = j.effectiveDisplacement,
      destinationIndex = j.destinationIndex,
      destinationCapital = CapitalResponse.from(j.destinationCapital),
      path = j.path.map(CapitalResponse.from)
    )
