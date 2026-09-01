package borja.eurofiboclock.domain.algorithm.impl

import borja.eurofiboclock.domain.algorithm.*
import borja.eurofiboclock.domain.model.{CapitalCatalogue, CapitalSelection, Fibonacci}

/** **A1 - Fibonacci Colombian Capital Selector.**
  *
  * A circular selector driven by a division between the Colombian temporal factor
  * and the Fibonacci factor.
  *
  * Spec note: the PDF's worked example for A1 assumes `fibonacci(5) -> 2`, which
  * contradicts its own tail-recursion definition (and A2/A3, which use
  * `fibonacci(5) = 5`). This implementation applies `fibonacci(n)` consistently,
  * so A1 shares the exact same Fibonacci meaning as A2 and A3.
  */
final class FibonacciColombianCapitalSelector(catalogue: CapitalCatalogue)
    extends CapitalSelectionAlgorithm:

  val descriptor: AlgorithmDescriptor = AlgorithmDescriptor(
    id = AlgorithmId.A1,
    name = AlgorithmId.A1.displayName,
    businessQuestion =
      "¿Cuál es la capital europea del momento, combinando Fibonacci y la hora de " +
        "Colombia mediante una división circular?",
    catalogueSize = catalogue.size
  )

  def select(input: SelectionInput): SelectionOutcome =
    val fibonacciValue  = Fibonacci.of(input.n)
    val fibonacciFactor = BigInt(input.n) + fibonacciValue
    val temporalFactor  = input.clock.hours + input.clock.minutes + input.clock.seconds

    // n == 0 makes fibonacciFactor 0; fall back to a divisor of 1 so the pipeline
    // stays total (the spec is silent on this edge).
    val divisor  = if fibonacciFactor.signum == 0 then BigInt(1) else fibonacciFactor
    val rawIndex = BigInt(temporalFactor) / divisor

    SelectionOutcome.Selected(
      CapitalSelection(
        fibonacciInput = input.n,
        fibonacciValue = fibonacciValue,
        fibonacciFactor = fibonacciFactor,
        colombianTime = input.clock.label,
        temporalFactor = temporalFactor,
        rawIndex = rawIndex,
        capitalIndex = catalogue.indexOf(rawIndex.toLong),
        selected = catalogue.at(rawIndex.toLong)
      )
    )
