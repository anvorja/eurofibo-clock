package borja.eurofiboclock.application.service

import cats.MonadThrow
import cats.syntax.all.*

import borja.eurofiboclock.domain.algorithm.*
import borja.eurofiboclock.domain.algorithm.impl.{
  EuropeanTimeTraveller,
  FibonacciColombianCapitalSelector,
  FibonacciTemporalPassport
}
import borja.eurofiboclock.domain.error.DomainError
import borja.eurofiboclock.domain.model.WallClock
import borja.eurofiboclock.domain.port.in.{ComputeFibonacciUseCase, RunCapitalAlgorithmUseCase}
import borja.eurofiboclock.domain.port.out.{ClockPort, EuropeanCapitalRepository}

/** Application service implementing [[RunCapitalAlgorithmUseCase]].
  *
  * It orchestrates only: validate `n`, read the current instant from the
  * [[ClockPort]], pick the requested strategy (wired with the catalogue it needs
  * from the [[EuropeanCapitalRepository]]) and run it. All the arithmetic lives in
  * the pure domain algorithms.
  */
final class EuroFiboService[F[_]: MonadThrow](
    clock: ClockPort[F],
    repository: EuropeanCapitalRepository[F]
) extends RunCapitalAlgorithmUseCase[F]:

  def descriptors: F[List[AlgorithmDescriptor]] =
    allAlgorithms.map(_.map(_.descriptor))

  def run(algorithm: AlgorithmId, n: Int): F[SelectionOutcome] =
    for
      _       <- validateIndex(n)
      instant <- clock.now
      chosen  <- algorithmFor(algorithm)
    yield chosen.select(SelectionInput(n, WallClock.fromInstant(instant)))

  private def validateIndex(n: Int): F[Unit] =
    if n < 0 || n > ComputeFibonacciUseCase.MaxIndex then
      MonadThrow[F].raiseError(
        DomainError.InvalidFibonacciIndex(n, ComputeFibonacciUseCase.MaxIndex)
      )
    else MonadThrow[F].unit

  private def allAlgorithms: F[List[CapitalSelectionAlgorithm]] =
    (repository.coreCatalogue, repository.extendedCatalogue).mapN { (core, extended) =>
      List(
        FibonacciColombianCapitalSelector(core),
        FibonacciTemporalPassport(core),
        EuropeanTimeTraveller(extended)
      )
    }

  private def algorithmFor(id: AlgorithmId): F[CapitalSelectionAlgorithm] = id match
    case AlgorithmId.A1 => repository.coreCatalogue.map(FibonacciColombianCapitalSelector(_))
    case AlgorithmId.A2 => repository.coreCatalogue.map(FibonacciTemporalPassport(_))
    case AlgorithmId.A3 => repository.extendedCatalogue.map(EuropeanTimeTraveller(_))
