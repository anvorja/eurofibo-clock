package borja.eurofiboclock.application.service

import cats.MonadThrow
import cats.syntax.flatMap.*
import cats.syntax.functor.*

import borja.eurofiboclock.domain.error.DomainError
import borja.eurofiboclock.domain.model.EuropeanCapital
import borja.eurofiboclock.domain.port.in.GetEuropeanCapitalsUseCase
import borja.eurofiboclock.domain.port.out.EuropeanCapitalRepository

/** Application service implementing [[GetEuropeanCapitalsUseCase]].
  *
  * The catalogue itself comes from the [[EuropeanCapitalRepository]] driven port
  * (the 5-entry core catalogue); this service adds the lookup / not-found
  * business behaviour.
  */
final class EuropeanCapitalsService[F[_]: MonadThrow](repository: EuropeanCapitalRepository[F])
    extends GetEuropeanCapitalsUseCase[F]:

  def listAll: F[List[EuropeanCapital]] =
    repository.coreCatalogue.map(_.capitals.toList)

  def findByCountry(country: String): F[EuropeanCapital] =
    val wanted = country.trim.toLowerCase
    listAll.flatMap { all =>
      all.find(_.country.toLowerCase == wanted) match
        case Some(found) => MonadThrow[F].pure(found)
        case None =>
          MonadThrow[F].raiseError(DomainError.UnknownCountry(country, all.map(_.country)))
    }
