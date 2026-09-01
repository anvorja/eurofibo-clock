package borja.eurofiboclock.infrastructure.adapter.in.rest

import cats.effect.Concurrent
import cats.syntax.all.*
import org.http4s.HttpRoutes
import org.http4s.circe.CirceEntityEncoder.given
import org.http4s.dsl.Http4sDsl

import borja.eurofiboclock.domain.port.in.ComputeFibonacciUseCase
import borja.eurofiboclock.infrastructure.adapter.in.rest.JsonCodecs.given
import borja.eurofiboclock.infrastructure.adapter.in.rest.dto.{ErrorResponse, FibonacciResponse}

/** Inbound REST adapter for the Fibonacci use case.
  *
  * Accepts the index either as a path segment (`/fibonacci/10`) or as a query
  * parameter (`/fibonacci?n=10`). A non-numeric value is a transport error (400)
  * and never reaches the domain; an out-of-range value is a domain error and is
  * translated centrally in [[HttpApi]].
  */
final class FibonacciController[F[_]: Concurrent](useCase: ComputeFibonacciUseCase[F]) extends Http4sDsl[F]:

  private object NQueryParam extends QueryParamDecoderMatcher[String]("n")

  private def run(raw: String): F[org.http4s.Response[F]] =
    raw.trim.toIntOption match
      case Some(n) => useCase.compute(n).flatMap(r => Ok(FibonacciResponse.from(r)))
      case None =>
        BadRequest(ErrorResponse("invalid-request", s"'$raw' is not a valid integer index"))

  val routes: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root / "api" / "v1" / "fibonacci" / rawN            => run(rawN)
    case GET -> Root / "api" / "v1" / "fibonacci" :? NQueryParam(n) => run(n)
  }
