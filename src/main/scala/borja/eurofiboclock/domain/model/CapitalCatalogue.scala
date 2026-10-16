package borja.eurofiboclock.domain.model

/** An ordered, non-empty catalogue of European capitals that is walked
  * **circularly**: any integer index (even negative, even far larger than the
  * catalogue) maps back into range.
  *
  * A1 and A2 operate on the 5-entry core catalogue; A3 on the 10-entry extended
  * one. The circular behaviour is the same in every algorithm, so it lives here.
  */
final case class CapitalCatalogue(capitals: Vector[EuropeanCapital]):
  require(capitals.nonEmpty, "a capital catalogue cannot be empty")

  val size: Int = capitals.size

  /** Normalises `index` into `[0, size)` (Euclidean modulo) and returns that entry. */
  def at(index: Long): EuropeanCapital = capitals(indexOf(index))

  /** The normalised position `index` resolves to. */
  def indexOf(index: Long): Int = Math.floorMod(index, size.toLong).toInt

  /** The `steps`-long circular walk starting at `from` (inclusive on both ends). */
  def walk(from: Int, steps: Int): List[EuropeanCapital] =
    (0 to steps).map(offset => at(from.toLong + offset)).toList
