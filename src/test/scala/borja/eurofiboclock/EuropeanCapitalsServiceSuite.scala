package borja.eurofiboclock

import cats.effect.IO
import munit.CatsEffectSuite

import borja.eurofiboclock.application.service.EuropeanCapitalsService
import borja.eurofiboclock.domain.error.DomainError
import borja.eurofiboclock.infrastructure.adapter.out.capital.StaticEuropeanCapitalRepository

final class EuropeanCapitalsServiceSuite extends CatsEffectSuite:

  private val service = new EuropeanCapitalsService[IO](StaticEuropeanCapitalRepository[IO])

  test("lists the five supported capitals") {
    service.listAll.map { all =>
      assertEquals(all.size, 5)
      assertEquals(all.map(_.country).toSet, Set("France", "Germany", "Spain", "Italy", "Portugal"))
    }
  }

  test("looks up a country case-insensitively") {
    service.findByCountry("gErMaNy").map(c => assertEquals(c.capital, "Berlin"))
  }

  test("unknown country raises a domain error") {
    service.findByCountry("Narnia").attempt.map {
      case Left(_: DomainError.UnknownCountry) => ()
      case other                              => fail(s"expected UnknownCountry, got $other")
    }
  }
