package borja.eurofiboclock.domain.port.in

import borja.eurofiboclock.domain.model.EuropeanCapital

/** Driving port: query the catalogue of European capitals. */
trait GetEuropeanCapitalsUseCase[F[_]]:

  /** All supported country/capital pairs. */
  def listAll: F[List[EuropeanCapital]]

  /** Look up a single country by name (case-insensitive).
    *
    * Raises [[borja.eurofiboclock.domain.error.DomainError.UnknownCountry]] through `F`
    * when the country is not part of the catalogue.
    */
  def findByCountry(country: String): F[EuropeanCapital]
