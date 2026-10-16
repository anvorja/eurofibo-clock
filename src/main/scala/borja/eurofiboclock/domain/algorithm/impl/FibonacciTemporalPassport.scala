package borja.eurofiboclock.domain.algorithm.impl

import java.util.Locale

import borja.eurofiboclock.domain.algorithm.*
import borja.eurofiboclock.domain.model.{CapitalCatalogue, Fibonacci, TemporalPassport}

/** **A2 - Fibonacci Temporal Passport.**
  *
  * Builds a symbolic "European temporal passport": Fibonacci is a multiplier of
  * the Colombian hour, minutes are added as temporal distance and seconds as a
  * variation factor, producing the European Temporal Score (ETS) that indexes the
  * catalogue.
  */
final class FibonacciTemporalPassport(catalogue: CapitalCatalogue)
    extends CapitalSelectionAlgorithm:

  val descriptor: AlgorithmDescriptor = AlgorithmDescriptor(
    id = AlgorithmId.A2,
    name = AlgorithmId.A2.displayName,
    businessQuestion =
      "¿Qué país europeo te corresponde visitar simbólicamente, según un pasaporte " +
        "temporal donde Fibonacci multiplica la hora colombiana?",
    catalogueSize = catalogue.size
  )

  def select(input: SelectionInput): SelectionOutcome =
    val c              = input.clock
    val fibonacciValue = Fibonacci.of(input.n)
    val fibonacciCode  = BigInt(input.n) + fibonacciValue

    // hora -> coordenada de salida, minuto -> distancia temporal, segundo -> factor de variación
    val temporalScore = BigInt(c.hours) * fibonacciCode + c.minutes + c.seconds

    val capitalIndex = temporalScore.mod(BigInt(catalogue.size)).toInt
    val destination  = catalogue.at(capitalIndex.toLong)

    val passportCode =
      s"FIB-${input.n}-COL-${c.compact}-$temporalScore-${destination.capital.toUpperCase(Locale.ROOT)}"

    SelectionOutcome.Passport(
      TemporalPassport(
        fibonacciInput = input.n,
        fibonacciValue = fibonacciValue,
        fibonacciCode = fibonacciCode,
        colombianTime = c.label,
        temporalScore = temporalScore,
        capitalIndex = capitalIndex,
        destination = destination,
        passportCode = passportCode
      )
    )
