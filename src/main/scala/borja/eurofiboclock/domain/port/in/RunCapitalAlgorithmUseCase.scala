package borja.eurofiboclock.domain.port.in

import borja.eurofiboclock.domain.algorithm.{AlgorithmDescriptor, AlgorithmId, SelectionOutcome}

/** Driving port of the EuroFibo Clock: run one capital-selection strategy.
  *
  * Implementations raise
  * [[borja.eurofiboclock.domain.error.DomainError.InvalidFibonacciIndex]] through `F` when
  * `n` is out of range.
  */
trait RunCapitalAlgorithmUseCase[F[_]]:

  /** Metadata for every registered strategy (A1, A2, A3, ...). */
  def descriptors: F[List[AlgorithmDescriptor]]

  /** Run `algorithm` for Fibonacci index `n` against the current Colombia time. */
  def run(algorithm: AlgorithmId, n: Int): F[SelectionOutcome]
