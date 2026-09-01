package borja.eurofiboclock.domain.model

/** Fibonacci as a pure domain function.
  *
  * Implemented as tail recursion with accumulators (iterative behaviour), exactly
  * as the EuroFibo Clock spec mandates for A1, A2, A3 and any future algorithm:
  *
  * {{{
  *   fibonacci(n)        -> fibonacciTail(n, 0, 1)
  *   fibonacciTail(n,a,b) -> if n == 0 then a else fibonacciTail(n - 1, b, a + b)
  * }}}
  *
  * So `of(0) == 0`, `of(1) == 1`, `of(5) == 5`, `of(10) == 55`.
  */
object Fibonacci:

  def of(n: Int): BigInt =
    require(n >= 0, s"Fibonacci is undefined for negative indices, got $n")

    @annotation.tailrec
    def tail(remaining: Int, a: BigInt, b: BigInt): BigInt =
      if remaining == 0 then a
      else tail(remaining - 1, b, a + b)

    tail(n, BigInt(0), BigInt(1))
