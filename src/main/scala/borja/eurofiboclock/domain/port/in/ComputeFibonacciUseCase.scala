package borja.eurofiboclock.domain.port.in

import borja.eurofiboclock.domain.model.FibonacciResult

/** Driving port: "compute the Fibonacci number at index n".
  *
  * Implementations raise [[borja.eurofiboclock.domain.error.DomainError.InvalidFibonacciIndex]]
  * through `F` when `n` is out of the accepted range.
  */
trait ComputeFibonacciUseCase[F[_]]:
  def compute(n: Int): F[FibonacciResult]

object ComputeFibonacciUseCase:
  /** Guard rail so an inbound request cannot ask for an unbounded computation. */
  val MaxIndex: Int = 10_000
