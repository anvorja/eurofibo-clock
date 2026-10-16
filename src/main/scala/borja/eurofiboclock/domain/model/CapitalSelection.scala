package borja.eurofiboclock.domain.model

/** Result of algorithm **A1 - Fibonacci Colombian Capital Selector**.
  *
  * Pipeline (spec):
  * {{{
  *   fibonacciValue  = fibonacci(n)
  *   fibonacciFactor = n + fibonacciValue
  *   temporalFactor  = hours + minutes + seconds
  *   rawIndex        = temporalFactor / fibonacciFactor      // integer division
  *   capitalIndex    = rawIndex % catalogueSize              // circular
  *   selected        = catalogue(capitalIndex)
  * }}}
  */
final case class CapitalSelection(
    fibonacciInput: Int,
    fibonacciValue: BigInt,
    fibonacciFactor: BigInt,
    colombianTime: String,
    temporalFactor: Int,
    rawIndex: BigInt,
    capitalIndex: Int,
    selected: EuropeanCapital
)
