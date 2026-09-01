package borja.eurofiboclock.domain.algorithm

/** The catalogue of selection strategies that share the EuroFibo Clock domain.
  *
  * New rules (A4, A5, ...) are added here plus one implementation; nothing else
  * in the hexagon needs to change.
  */
enum AlgorithmId(val slug: String, val displayName: String):
  case A1 extends AlgorithmId("a1", "Fibonacci Colombian Capital Selector")
  case A2 extends AlgorithmId("a2", "Fibonacci Temporal Passport")
  case A3 extends AlgorithmId("a3", "Viajero Temporal Europeo")

object AlgorithmId:
  def fromSlug(raw: String): Option[AlgorithmId] =
    val s = raw.trim.toLowerCase
    values.find(_.slug == s)
