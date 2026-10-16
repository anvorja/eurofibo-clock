package borja.eurofiboclock.domain.algorithm

/** Human-facing metadata about a selection strategy. */
final case class AlgorithmDescriptor(
    id: AlgorithmId,
    name: String,
    businessQuestion: String,
    catalogueSize: Int
)
