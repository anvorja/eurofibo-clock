package borja.eurofiboclock.domain

import munit.FunSuite

import borja.eurofiboclock.domain.model.Fibonacci

/** Unit tests for the pure domain function [[Fibonacci]] (tail recursion with
  * accumulators).
  */
final class FibonacciSuite extends FunSuite:

  test("known small values (spec: of(0)=0, of(1)=1, of(5)=5, of(10)=55)") {
    val expected = List(0, 1, 1, 2, 3, 5, 8, 13, 21, 34, 55).map(BigInt(_))
    assertEquals((0 to 10).map(Fibonacci.of).toList, expected)
  }

  test("index 100 does not overflow a 64-bit integer") {
    assertEquals(Fibonacci.of(100), BigInt("354224848179261915075"))
  }

  test("satisfies the recurrence of(n) == of(n-1) + of(n-2)") {
    (2 to 40).foreach { n =>
      assertEquals(Fibonacci.of(n), Fibonacci.of(n - 1) + Fibonacci.of(n - 2), s"n = $n")
    }
  }

  test("is non-decreasing over the non-negative range") {
    (1 to 50).foreach { n =>
      assert(Fibonacci.of(n) >= Fibonacci.of(n - 1), s"n = $n")
    }
  }

  test("deep index does not blow the stack (tail-recursive) and stays consistent") {
    val big = Fibonacci.of(10000)
    assert(big > 0)
    assertEquals(big.toString.length, 2090) // fib(10000) has 2090 digits
    assertEquals(big, Fibonacci.of(9999) + Fibonacci.of(9998))
  }

  test("negative index is rejected") {
    intercept[IllegalArgumentException](Fibonacci.of(-1))
  }
