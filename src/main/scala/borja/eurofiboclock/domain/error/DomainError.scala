package borja.eurofiboclock.domain.error

/** Errors that belong to the business rules of the hexagon.
  *
  * They extend [[RuntimeException]] so they can travel through a `MonadThrow`
  * effect channel, but the inbound adapters are expected to translate them into
  * transport-specific responses (e.g. HTTP 400) instead of leaking stack traces.
  */
sealed abstract class DomainError(message: String) extends RuntimeException(message):
  def describe: String = message

object DomainError:

  final case class InvalidFibonacciIndex(n: Int, max: Int)
      extends DomainError(s"Fibonacci index must be an integer in [0, $max], but was $n")

  final case class UnknownCountry(country: String, known: List[String])
      extends DomainError(
        s"'$country' is not one of the supported European countries: ${known.mkString(", ")}"
      )
