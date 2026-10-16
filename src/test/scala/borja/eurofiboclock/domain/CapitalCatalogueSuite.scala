package borja.eurofiboclock.domain

import munit.FunSuite

import borja.eurofiboclock.domain.model.{CapitalCatalogue, EuropeanCapital}

/** Unit tests for the circular catalogue [[CapitalCatalogue]]. */
final class CapitalCatalogueSuite extends FunSuite:

  private val a = EuropeanCapital("A-land", "A")
  private val b = EuropeanCapital("B-land", "B")
  private val c = EuropeanCapital("C-land", "C")
  private val catalogue = CapitalCatalogue(Vector(a, b, c))

  test("size reflects the backing vector") {
    assertEquals(catalogue.size, 3)
  }

  test("at returns entries in order within range") {
    assertEquals(catalogue.at(0), a)
    assertEquals(catalogue.at(1), b)
    assertEquals(catalogue.at(2), c)
  }

  test("at wraps around for indices >= size") {
    assertEquals(catalogue.at(3), a)
    assertEquals(catalogue.at(4), b)
    assertEquals(catalogue.at(7), b)
    assertEquals(catalogue.at(1_000_000L), catalogue.at(1_000_000L % 3))
  }

  test("at wraps around for negative indices (Euclidean modulo)") {
    assertEquals(catalogue.at(-1), c)
    assertEquals(catalogue.at(-2), b)
    assertEquals(catalogue.at(-3), a)
    assertEquals(catalogue.at(-4), c)
  }

  test("indexOf normalises to [0, size)") {
    assertEquals(catalogue.indexOf(0), 0)
    assertEquals(catalogue.indexOf(7), 1)
    assertEquals(catalogue.indexOf(-1), 2)
    (-20L to 20L).foreach { i =>
      val idx = catalogue.indexOf(i)
      assert(idx >= 0 && idx < 3, s"i = $i -> $idx")
      assertEquals(catalogue.capitals(idx), catalogue.at(i))
    }
  }

  test("walk is inclusive on both ends (steps + 1 entries)") {
    assertEquals(catalogue.walk(0, 0), List(a))
    assertEquals(catalogue.walk(0, 2), List(a, b, c))
  }

  test("walk wraps circularly") {
    assertEquals(catalogue.walk(1, 3), List(b, c, a, b))
    assertEquals(catalogue.walk(2, 4), List(c, a, b, c, a))
  }

  test("walk starts at `from` and ends at from + steps") {
    val path = catalogue.walk(2, 5)
    assertEquals(path.head, catalogue.at(2))
    assertEquals(path.last, catalogue.at(2L + 5))
    assertEquals(path.size, 6)
  }

  test("a single-entry catalogue always resolves to that entry") {
    val one = CapitalCatalogue(Vector(a))
    assertEquals(one.at(0), a)
    assertEquals(one.at(-99), a)
    assertEquals(one.at(99), a)
    assertEquals(one.walk(0, 3), List(a, a, a, a))
  }

  test("an empty catalogue is rejected at construction") {
    intercept[IllegalArgumentException](CapitalCatalogue(Vector.empty))
  }
