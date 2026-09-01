package borja.eurofiboclock.domain.model

/** Result of algorithm **A3 - Viajero Temporal Europeo**.
  *
  * Pipeline (spec), over the 10-entry extended catalogue:
  * {{{
  *   startIndex            = fibonacci(n) % catalogueSize
  *   temporalDisplacement  = hours * minutes + seconds
  *   effectiveDisplacement = temporalDisplacement % catalogueSize
  *   destinationIndex      = (startIndex + effectiveDisplacement) % catalogueSize
  * }}}
  * `path` is the circular walk from `startCapital` to `destinationCapital`.
  */
final case class EuropeanJourney(
    fibonacciInput: Int,
    fibonacciValue: BigInt,
    startIndex: Int,
    startCapital: EuropeanCapital,
    colombianTime: String,
    temporalDisplacement: Long,
    effectiveDisplacement: Int,
    destinationIndex: Int,
    destinationCapital: EuropeanCapital,
    path: List[EuropeanCapital]
)
