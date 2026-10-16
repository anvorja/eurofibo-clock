package borja.eurofiboclock.domain.algorithm

import munit.FunSuite

import borja.eurofiboclock.domain.algorithm.impl.FibonacciTemporalPassport
import borja.eurofiboclock.domain.model.{CapitalCatalogue, EuropeanCapital, WallClock}

/** Unit tests for strategy **A2 - Fibonacci Temporal Passport**. */
final class FibonacciTemporalPassportSuite extends FunSuite:

  private val catalogue = CapitalCatalogue(
    Vector(
      EuropeanCapital("France", "Paris"),
      EuropeanCapital("Germany", "Berlin"),
      EuropeanCapital("Spain", "Madrid"),
      EuropeanCapital("Italy", "Rome"),
      EuropeanCapital("Portugal", "Lisbon")
    )
  )
  private val a2 = new FibonacciTemporalPassport(catalogue)

  private def passport(n: Int, clock: WallClock) =
    a2.select(SelectionInput(n, clock)) match
      case SelectionOutcome.Passport(p) => p
      case other                        => fail(s"expected Passport, got $other")

  test("descriptor identifies A2 and its 5-entry catalogue") {
    assertEquals(a2.descriptor.id, AlgorithmId.A2)
    assertEquals(a2.descriptor.catalogueSize, 5)
  }

  test("reproduces the spec example exactly (n = 5, 13:40:34)") {
    // fib(5)=5 ; code = 5+5 = 10 ; ETS = 13*10 + 40 + 34 = 204 ; 204 % 5 = 4 -> Lisbon
    val p = passport(5, WallClock(13, 40, 34))
    assertEquals(p.fibonacciValue, BigInt(5))
    assertEquals(p.fibonacciCode, BigInt(10))
    assertEquals(p.temporalScore, BigInt(204))
    assertEquals(p.capitalIndex, 4)
    assertEquals(p.destination, EuropeanCapital("Portugal", "Lisbon"))
    assertEquals(p.passportCode, "FIB-5-COL-134034-204-LISBON")
  }

  test("n = 0 keeps the ETS = minutes + seconds (hours * 0)") {
    // code = 0 ; ETS = 13*0 + 40 + 34 = 74 ; 74 % 5 = 4 -> Lisbon
    val p = passport(0, WallClock(13, 40, 34))
    assertEquals(p.fibonacciCode, BigInt(0))
    assertEquals(p.temporalScore, BigInt(74))
    assertEquals(p.passportCode, "FIB-0-COL-134034-74-LISBON")
  }

  test("passport code zero-pads the time and upper-cases the capital") {
    // compact "020304" ; ETS = 2*10 + 3 + 4 = 27 ; 27 % 5 = 2 -> Madrid
    val p = passport(5, WallClock(2, 3, 4))
    assertEquals(p.temporalScore, BigInt(27))
    assertEquals(p.destination.capital, "Madrid")
    assertEquals(p.passportCode, "FIB-5-COL-020304-27-MADRID")
    assert(p.passportCode.endsWith("-MADRID"))
  }

  test("larger index: n = 10 at 02:53:34") {
    // fib(10)=55 ; code = 65 ; ETS = 2*65 + 53 + 34 = 217 ; 217 % 5 = 2 -> Madrid
    val p = passport(10, WallClock(2, 53, 34))
    assertEquals(p.fibonacciValue, BigInt(55))
    assertEquals(p.temporalScore, BigInt(217))
    assertEquals(p.passportCode, "FIB-10-COL-025334-217-MADRID")
  }

  test("is a pure function: same input, same outcome") {
    val in = SelectionInput(6, WallClock(21, 7, 8))
    assertEquals(a2.select(in), a2.select(in))
  }
