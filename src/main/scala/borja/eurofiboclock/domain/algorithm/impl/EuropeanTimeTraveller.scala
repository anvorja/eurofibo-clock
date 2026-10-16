package borja.eurofiboclock.domain.algorithm.impl

import borja.eurofiboclock.domain.algorithm.*
import borja.eurofiboclock.domain.model.{CapitalCatalogue, EuropeanJourney, Fibonacci}

/** **A3 - Viajero Temporal Europeo.**
  *
  * A travel oracle over the 10-entry extended catalogue: Fibonacci fixes the
  * starting capital, the Colombian time fixes how many positions the traveller
  * advances around the circular list.
  */
final class EuropeanTimeTraveller(catalogue: CapitalCatalogue) extends CapitalSelectionAlgorithm:

  val descriptor: AlgorithmDescriptor = AlgorithmDescriptor(
    id = AlgorithmId.A3,
    name = AlgorithmId.A3.displayName,
    businessQuestion =
      "¿Desde qué capital comienza tu viaje europeo según Fibonacci y cuántas " +
        "ciudades debes avanzar según la hora actual de Colombia?",
    catalogueSize = catalogue.size
  )

  def select(input: SelectionInput): SelectionOutcome =
    val c              = input.clock
    val fibonacciValue = Fibonacci.of(input.n)

    // Paso 1 - Fibonacci determina el punto de partida.
    val startIndex = fibonacciValue.mod(BigInt(catalogue.size)).toInt

    // Paso 2 - la hora colombiana determina el desplazamiento.
    val temporalDisplacement  = c.hours.toLong * c.minutes + c.seconds
    val effectiveDisplacement = Math.floorMod(temporalDisplacement, catalogue.size.toLong).toInt

    // Paso 3 - recorrer Europa de forma circular.
    val destinationIndex = catalogue.indexOf(startIndex.toLong + effectiveDisplacement)

    SelectionOutcome.Journey(
      EuropeanJourney(
        fibonacciInput = input.n,
        fibonacciValue = fibonacciValue,
        startIndex = startIndex,
        startCapital = catalogue.at(startIndex.toLong),
        colombianTime = c.label,
        temporalDisplacement = temporalDisplacement,
        effectiveDisplacement = effectiveDisplacement,
        destinationIndex = destinationIndex,
        destinationCapital = catalogue.at(destinationIndex.toLong),
        path = catalogue.walk(startIndex, effectiveDisplacement)
      )
    )
