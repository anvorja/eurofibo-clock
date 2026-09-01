package borja.eurofiboclock.domain.model

/** Result of evaluating the Fibonacci sequence at index `n`.
  *
  * `value` is a [[BigInt]] because the sequence overflows 64-bit integers well
  * before index 100.
  */
final case class FibonacciResult(n: Int, value: BigInt)
