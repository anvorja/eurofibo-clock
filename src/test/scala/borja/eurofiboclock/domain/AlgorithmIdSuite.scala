package borja.eurofiboclock.domain

import munit.FunSuite

import borja.eurofiboclock.domain.algorithm.AlgorithmId

/** Unit tests for [[AlgorithmId]] and its slug parsing. */
final class AlgorithmIdSuite extends FunSuite:

  test("there are exactly three registered strategies") {
    assertEquals(AlgorithmId.values.toList, List(AlgorithmId.A1, AlgorithmId.A2, AlgorithmId.A3))
  }

  test("each id carries its slug and display name") {
    assertEquals(AlgorithmId.A1.slug, "a1")
    assertEquals(AlgorithmId.A2.slug, "a2")
    assertEquals(AlgorithmId.A3.slug, "a3")
    assertEquals(AlgorithmId.A1.displayName, "Fibonacci Colombian Capital Selector")
    assertEquals(AlgorithmId.A2.displayName, "Fibonacci Temporal Passport")
    assertEquals(AlgorithmId.A3.displayName, "Viajero Temporal Europeo")
  }

  test("fromSlug resolves known slugs") {
    assertEquals(AlgorithmId.fromSlug("a1"), Some(AlgorithmId.A1))
    assertEquals(AlgorithmId.fromSlug("a2"), Some(AlgorithmId.A2))
    assertEquals(AlgorithmId.fromSlug("a3"), Some(AlgorithmId.A3))
  }

  test("fromSlug is case-insensitive and trims surrounding whitespace") {
    assertEquals(AlgorithmId.fromSlug("A2"), Some(AlgorithmId.A2))
    assertEquals(AlgorithmId.fromSlug("  a3 "), Some(AlgorithmId.A3))
    assertEquals(AlgorithmId.fromSlug("\tA1\n"), Some(AlgorithmId.A1))
  }

  test("fromSlug rejects unknown or empty input") {
    assertEquals(AlgorithmId.fromSlug("a9"), None)
    assertEquals(AlgorithmId.fromSlug(""), None)
    assertEquals(AlgorithmId.fromSlug("fibonacci"), None)
  }

  test("slug round-trips through fromSlug for every id") {
    AlgorithmId.values.foreach { id =>
      assertEquals(AlgorithmId.fromSlug(id.slug), Some(id))
    }
  }
