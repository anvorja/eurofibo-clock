package borja.eurofiboclock.infrastructure.adapter.out.capital

import cats.Applicative
import cats.syntax.applicative.*

import borja.eurofiboclock.domain.model.{CapitalCatalogue, EuropeanCapital}
import borja.eurofiboclock.domain.port.out.EuropeanCapitalRepository

/** Driven adapter: fixed, in-memory capital catalogues.
  *
  * Swapping this for a JDBC or HTTP-backed repository would not touch the
  * application services or the domain.
  */
final class StaticEuropeanCapitalRepository[F[_]: Applicative] extends EuropeanCapitalRepository[F]:
  def coreCatalogue: F[CapitalCatalogue]     = StaticEuropeanCapitalRepository.Core.pure[F]
  def extendedCatalogue: F[CapitalCatalogue] = StaticEuropeanCapitalRepository.Extended.pure[F]

object StaticEuropeanCapitalRepository:

  /** A1 / A2 catalogue - 5 capitals, in the spec's order. */
  val Core: CapitalCatalogue = CapitalCatalogue(
    Vector(
      EuropeanCapital("France", "Paris"),
      EuropeanCapital("Germany", "Berlin"),
      EuropeanCapital("Spain", "Madrid"),
      EuropeanCapital("Italy", "Rome"),
      EuropeanCapital("Portugal", "Lisbon")
    )
  )

  /** A3 catalogue - 10 capitals, in the spec's order (index 9 = Greece / Athens). */
  val Extended: CapitalCatalogue = CapitalCatalogue(
    Vector(
      EuropeanCapital("France", "Paris"),
      EuropeanCapital("Germany", "Berlin"),
      EuropeanCapital("Spain", "Madrid"),
      EuropeanCapital("Italy", "Rome"),
      EuropeanCapital("Portugal", "Lisbon"),
      EuropeanCapital("Austria", "Vienna"),
      EuropeanCapital("Czech Republic", "Prague"),
      EuropeanCapital("Poland", "Warsaw"),
      EuropeanCapital("Hungary", "Budapest"),
      EuropeanCapital("Greece", "Athens")
    )
  )

  def apply[F[_]: Applicative]: StaticEuropeanCapitalRepository[F] =
    new StaticEuropeanCapitalRepository[F]
