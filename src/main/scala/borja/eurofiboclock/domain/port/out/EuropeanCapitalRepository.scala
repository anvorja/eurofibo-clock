package borja.eurofiboclock.domain.port.out

import borja.eurofiboclock.domain.model.CapitalCatalogue

/** Driven port: source of the European capital catalogues.
  *
  *   - [[coreCatalogue]]     -> the 5 capitals used by A1 and A2
  *   - [[extendedCatalogue]] -> the 10 capitals used by A3
  *
  * The current adapter is a static in-memory list, but the port would let us
  * swap in a database or an external API without touching the domain.
  */
trait EuropeanCapitalRepository[F[_]]:
  def coreCatalogue: F[CapitalCatalogue]
  def extendedCatalogue: F[CapitalCatalogue]
