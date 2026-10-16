package borja.eurofiboclock.domain.algorithm

import munit.FunSuite

import borja.eurofiboclock.domain.algorithm.impl.EuropeanTimeTraveller
import borja.eurofiboclock.domain.model.{CapitalCatalogue, EuropeanCapital, WallClock}

/** Unit tests for strategy **A3 - Viajero Temporal Europeo** (extended 10-entry
  * catalogue).
  */
final class EuropeanTimeTravellerSuite extends FunSuite:

  private val catalogue = CapitalCatalogue(
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
  private val a3 = new EuropeanTimeTraveller(catalogue)

  private def journey(n: Int, clock: WallClock) =
    a3.select(SelectionInput(n, clock)) match
      case SelectionOutcome.Journey(j) => j
      case other                       => fail(s"expected Journey, got $other")

  test("descriptor identifies A3 and its 10-entry catalogue") {
    assertEquals(a3.descriptor.id, AlgorithmId.A3)
    assertEquals(a3.descriptor.catalogueSize, 10)
  }

  test("reproduces the spec example exactly (n = 5, 13:40:34)") {
    // fib(5)=5 -> start 5 (Vienna) ; 13*40 + 34 = 554 ; 554 % 10 = 4 ; (5+4) % 10 = 9 -> Athens
    val j = journey(5, WallClock(13, 40, 34))
    assertEquals(j.startIndex, 5)
    assertEquals(j.startCapital, EuropeanCapital("Austria", "Vienna"))
    assertEquals(j.temporalDisplacement, 554L)
    assertEquals(j.effectiveDisplacement, 4)
    assertEquals(j.destinationIndex, 9)
    assertEquals(j.destinationCapital, EuropeanCapital("Greece", "Athens"))
    assertEquals(
      j.path.map(_.capital),
      List("Vienna", "Prague", "Warsaw", "Budapest", "Athens")
    )
  }

  test("path is inclusive: length = effectiveDisplacement + 1, ends match start/destination") {
    val j = journey(5, WallClock(13, 40, 34))
    assertEquals(j.path.size, j.effectiveDisplacement + 1)
    assertEquals(j.path.head, j.startCapital)
    assertEquals(j.path.last, j.destinationCapital)
  }

  test("the walk wraps around the end of the catalogue (n = 5, 02:53:22)") {
    // start 5 ; 2*53 + 22 = 128 ; 128 % 10 = 8 ; (5+8) % 10 = 3 -> Rome
    val j = journey(5, WallClock(2, 53, 22))
    assertEquals(j.startCapital.capital, "Vienna")
    assertEquals(j.effectiveDisplacement, 8)
    assertEquals(j.destinationIndex, 3)
    assertEquals(j.destinationCapital.capital, "Rome")
    assertEquals(
      j.path.map(_.capital),
      List("Vienna", "Prague", "Warsaw", "Budapest", "Athens", "Paris", "Berlin", "Madrid", "Rome")
    )
  }

  test("n = 0 starts the journey at index 0 (Paris)") {
    // fib(0)=0 -> start 0 ; 13*40 + 34 = 554 ; 554 % 10 = 4 ; (0+4) % 10 = 4 -> Lisbon
    val j = journey(0, WallClock(13, 40, 34))
    assertEquals(j.startIndex, 0)
    assertEquals(j.startCapital.capital, "Paris")
    assertEquals(j.destinationCapital.capital, "Lisbon")
    assertEquals(j.path.map(_.capital), List("Paris", "Berlin", "Madrid", "Rome", "Lisbon"))
  }

  test("Fibonacci start index also wraps: n = 7 -> fib(7) = 13 -> index 3 (Rome)") {
    val j = journey(7, WallClock(1, 0, 0))
    assertEquals(j.fibonacciValue, BigInt(13))
    assertEquals(j.startIndex, 3)
    assertEquals(j.startCapital.capital, "Rome")
  }

  test("zero displacement leaves the traveller in place with a single-entry path") {
    // 1*0 + 0 = 0 displacement
    val j = journey(5, WallClock(1, 0, 0))
    assertEquals(j.effectiveDisplacement, 0)
    assertEquals(j.destinationIndex, j.startIndex)
    assertEquals(j.path, List(j.startCapital))
  }

  test("is a pure function: same input, same outcome") {
    val in = SelectionInput(9, WallClock(18, 22, 5))
    assertEquals(a3.select(in), a3.select(in))
  }
