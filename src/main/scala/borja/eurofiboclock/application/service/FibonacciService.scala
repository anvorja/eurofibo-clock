package borja.eurofiboclock.application.service

import cats.MonadThrow
import cats.syntax.applicative.*
import cats.syntax.applicativeError.*

import borja.eurofiboclock.domain.error.DomainError
import borja.eurofiboclock.domain.model.{Fibonacci, FibonacciResult}
import borja.eurofiboclock.domain.port.in.ComputeFibonacciUseCase

/** Application service implementing [[ComputeFibonacciUseCase]].
  *
  * Validation (the business rule "n must be in range") happens here; the
  * arithmetic is the pure, tail-recursive [[Fibonacci]] domain function shared
  * with the EuroFibo Clock algorithms.
  */
final class FibonacciService[F[_]: MonadThrow] extends ComputeFibonacciUseCase[F]:

  def compute(n: Int): F[FibonacciResult] =
    if n < 0 || n > ComputeFibonacciUseCase.MaxIndex then
      DomainError
        .InvalidFibonacciIndex(n, ComputeFibonacciUseCase.MaxIndex)
        .raiseError[F, FibonacciResult]
    else
      FibonacciResult(n, Fibonacci.of(n)).pure[F]
