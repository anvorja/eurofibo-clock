package borja.eurofiboclock

import cats.effect.IO
import cats.syntax.all.*
import munit.CatsEffectSuite

import borja.eurofiboclock.application.service.FibonacciService
import borja.eurofiboclock.domain.error.DomainError
import borja.eurofiboclock.domain.port.in.ComputeFibonacciUseCase

final class FibonacciServiceSuite extends CatsEffectSuite:

  private val service = new FibonacciService[IO]

  test("computes known small values") {
    val expected = List(0, 1, 1, 2, 3, 5, 8, 13, 21, 34, 55).map(BigInt(_))
    (0 to 10).toList.zip(expected).traverse_ { case (n, exp) =>
      service.compute(n).map(r => assertEquals(r.value, exp))
    }
  }

  test("index 100 does not overflow") {
    service.compute(100).map(r => assertEquals(r.value, BigInt("354224848179261915075")))
  }

  test("negative index raises a domain error") {
    service.compute(-1).attempt.map {
      case Left(_: DomainError.InvalidFibonacciIndex) => ()
      case other                                      => fail(s"expected InvalidFibonacciIndex, got $other")
    }
  }

  test("index above the max raises a domain error") {
    service.compute(ComputeFibonacciUseCase.MaxIndex + 1).attempt.map {
      case Left(_: DomainError.InvalidFibonacciIndex) => ()
      case other                                      => fail(s"expected InvalidFibonacciIndex, got $other")
    }
  }
