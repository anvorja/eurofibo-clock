package borja.eurofiboclock.domain.algorithm

import munit.FunSuite

import borja.eurofiboclock.domain.algorithm.impl.FibonacciColombianCapitalSelector
import borja.eurofiboclock.domain.model.{CapitalCatalogue, EuropeanCapital, WallClock}

/** Unit tests for strategy **A1 - Fibonacci Colombian Capital Selector**. */
final class FibonacciColombianCapitalSelectorSuite extends FunSuite:

  private val catalogue = CapitalCatalogue(
    Vector(
      EuropeanCapital("France", "Paris"),
      EuropeanCapital("Germany", "Berlin"),
      EuropeanCapital("Spain", "Madrid"),
      EuropeanCapital("Italy", "Rome"),
      EuropeanCapital("Portugal", "Lisbon")
    )
  )
  private val a1 = new FibonacciColombianCapitalSelector(catalogue)

  private def selected(n: Int, clock: WallClock) =
    a1.select(SelectionInput(n, clock)) match
      case SelectionOutcome.Selected(s) => s
      case other                        => fail(s"expected Selected, got $other")

  test("descriptor identifies A1 and its 5-entry catalogue") {
    assertEquals(a1.descriptor.id, AlgorithmId.A1)
    assertEquals(a1.descriptor.name, AlgorithmId.A1.displayName)
    assertEquals(a1.descriptor.catalogueSize, 5)
    assert(a1.descriptor.businessQuestion.nonEmpty)
  }

  test("n = 5 at 13:40:34 runs the full pipeline deterministically") {
    // fib(5)=5 -> factor 10 ; 13+40+34 = 87 ; 87 / 10 = 8 ; 8 % 5 = 3 -> Rome
    val s = selected(5, WallClock(13, 40, 34))
    assertEquals(s.fibonacciInput, 5)
    assertEquals(s.fibonacciValue, BigInt(5))
    assertEquals(s.fibonacciFactor, BigInt(10))
    assertEquals(s.colombianTime, "13:40:34")
    assertEquals(s.temporalFactor, 87)
    assertEquals(s.rawIndex, BigInt(8))
    assertEquals(s.capitalIndex, 3)
    assertEquals(s.selected, EuropeanCapital("Italy", "Rome"))
  }

  test("n = 0 makes the Fibonacci factor 0 and falls back to divisor 1 (no ArithmeticException)") {
    val s = selected(0, WallClock(13, 40, 34))
    assertEquals(s.fibonacciFactor, BigInt(0))
    assertEquals(s.rawIndex, BigInt(87)) // 87 / 1
    assertEquals(s.capitalIndex, 2)      // 87 % 5
    assertEquals(s.selected.capital, "Madrid")
  }

  test("temporal factor 0 (midnight) yields rawIndex 0 -> first capital") {
    val s = selected(5, WallClock(0, 0, 0))
    assertEquals(s.temporalFactor, 0)
    assertEquals(s.rawIndex, BigInt(0))
    assertEquals(s.capitalIndex, 0)
    assertEquals(s.selected.capital, "Paris")
  }

  test("when the Fibonacci factor dwarfs the temporal factor, rawIndex collapses to 0") {
    // fib(20) = 6765 -> factor 6785 ; 23+59+59 = 141 ; 141 / 6785 = 0
    val s = selected(20, WallClock(23, 59, 59))
    assertEquals(s.fibonacciValue, BigInt(6765))
    assertEquals(s.rawIndex, BigInt(0))
    assertEquals(s.selected.capital, "Paris")
  }

  test("is a pure function: same input, same outcome") {
    val in = SelectionInput(7, WallClock(9, 15, 42))
    assertEquals(a1.select(in), a1.select(in))
  }
