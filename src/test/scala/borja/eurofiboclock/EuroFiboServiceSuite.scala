package borja.eurofiboclock

import cats.effect.IO
import munit.CatsEffectSuite

import java.time.Instant

import borja.eurofiboclock.application.service.EuroFiboService
import borja.eurofiboclock.domain.algorithm.{AlgorithmId, SelectionOutcome}
import borja.eurofiboclock.domain.error.DomainError
import borja.eurofiboclock.domain.port.out.ClockPort
import borja.eurofiboclock.infrastructure.adapter.out.capital.StaticEuropeanCapitalRepository

final class EuroFiboServiceSuite extends CatsEffectSuite:

  // 18:40:34Z == 13:40:34 in America/Bogota (UTC-5, no DST)
  private def fixedClock(iso: String): ClockPort[IO] =
    new ClockPort[IO]:
      def now: IO[Instant] = IO.pure(Instant.parse(iso))

  private def service(iso: String = "2026-09-01T18:40:34Z"): EuroFiboService[IO] =
    new EuroFiboService[IO](fixedClock(iso), StaticEuropeanCapitalRepository[IO])

  test("run A2 goes through clock + repository and yields the spec passport") {
    service().run(AlgorithmId.A2, 5).map {
      case SelectionOutcome.Passport(p) =>
        assertEquals(p.passportCode, "FIB-5-COL-134034-204-LISBON")
        assertEquals(p.destination.capital, "Lisbon")
      case other => fail(s"expected Passport, got $other")
    }
  }

  test("run A3 yields the spec journey ending in Athens") {
    service().run(AlgorithmId.A3, 5).map {
      case SelectionOutcome.Journey(j) =>
        assertEquals(j.startCapital.capital, "Vienna")
        assertEquals(j.destinationCapital.capital, "Athens")
      case other => fail(s"expected Journey, got $other")
    }
  }

  test("descriptors lists the three strategies with their catalogue sizes") {
    service().descriptors.map { ds =>
      assertEquals(ds.map(_.id), List(AlgorithmId.A1, AlgorithmId.A2, AlgorithmId.A3))
      assertEquals(ds.map(_.catalogueSize), List(5, 5, 10))
    }
  }

  test("an out-of-range index raises a domain error before touching the clock") {
    service().run(AlgorithmId.A1, -1).attempt.map {
      case Left(_: DomainError.InvalidFibonacciIndex) => ()
      case other => fail(s"expected InvalidFibonacciIndex, got $other")
    }
  }
