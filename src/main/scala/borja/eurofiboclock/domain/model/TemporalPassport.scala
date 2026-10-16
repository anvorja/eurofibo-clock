package borja.eurofiboclock.domain.model

/** Result of algorithm **A2 - Fibonacci Temporal Passport**.
  *
  * Pipeline (spec):
  * {{{
  *   fibonacciValue = fibonacci(n)
  *   fibonacciCode  = n + fibonacciValue
  *   temporalScore  = hours * fibonacciCode + minutes + seconds   // European Temporal Score
  *   capitalIndex   = temporalScore % catalogueSize
  *   destination    = catalogue(capitalIndex)
  *   passportCode   = "FIB-<n>-COL-<HHMMSS>-<ETS>-<CAPITAL>"
  * }}}
  */
final case class TemporalPassport(
    fibonacciInput: Int,
    fibonacciValue: BigInt,
    fibonacciCode: BigInt,
    colombianTime: String,
    temporalScore: BigInt,
    capitalIndex: Int,
    destination: EuropeanCapital,
    passportCode: String
)
