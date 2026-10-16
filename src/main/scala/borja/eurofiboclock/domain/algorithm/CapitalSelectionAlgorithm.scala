package borja.eurofiboclock.domain.algorithm

import borja.eurofiboclock.domain.model.WallClock

/** Input every strategy receives: the user's Fibonacci index and the current
  * Colombia wall-clock time.
  */
final case class SelectionInput(n: Int, clock: WallClock)

/** The Strategy of the EuroFibo Clock domain.
  *
  * One domain, many business rules. Each implementation is pure: given the same
  * [[SelectionInput]] it always produces the same [[SelectionOutcome]]. The
  * catalogue it works against is injected at construction time.
  */
trait CapitalSelectionAlgorithm:
  def descriptor: AlgorithmDescriptor
  def select(input: SelectionInput): SelectionOutcome
