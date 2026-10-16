package borja.eurofiboclock.infrastructure.adapter.in.rest

import cats.effect.Concurrent
import cats.syntax.all.*
import org.http4s.circe.CirceEntityEncoder.given
import org.http4s.dsl.Http4sDsl
import org.http4s.{HttpRoutes, Response}

import borja.eurofiboclock.domain.algorithm.{AlgorithmId, SelectionOutcome}
import borja.eurofiboclock.domain.port.in.RunCapitalAlgorithmUseCase
import borja.eurofiboclock.infrastructure.adapter.in.rest.JsonCodecs.given
import borja.eurofiboclock.infrastructure.adapter.in.rest.dto.*

/** Inbound REST adapter for the EuroFibo Clock capital-selection strategies.
  *
  *   - `GET /api/v1/eurofibo/algorithms`      -> metadata for A1, A2, A3
  *   - `GET /api/v1/eurofibo/{algorithm}/{n}` -> run one strategy (`algorithm` in a1|a2|a3)
  *
  * A bad `algorithm` slug or a non-numeric `n` is a transport error (400); an
  * out-of-range `n` is a domain error, translated centrally in [[HttpApi]].
  */
final class EuroFiboController[F[_]: Concurrent](useCase: RunCapitalAlgorithmUseCase[F])
    extends Http4sDsl[F]:

  private def render(outcome: SelectionOutcome): F[Response[F]] = outcome match
    case SelectionOutcome.Selected(v) => Ok(CapitalSelectionResponse.from(v))
    case SelectionOutcome.Passport(v) => Ok(TemporalPassportResponse.from(v))
    case SelectionOutcome.Journey(v)  => Ok(EuropeanJourneyResponse.from(v))

  val routes: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root / "api" / "v1" / "eurofibo" / "algorithms" =>
      useCase.descriptors.flatMap(ds => Ok(AlgorithmsResponse.from(ds)))

    case GET -> Root / "api" / "v1" / "eurofibo" / algorithm / rawN =>
      AlgorithmId.fromSlug(algorithm) match
        case None =>
          BadRequest(
            ErrorResponse("invalid-request", s"unknown algorithm '$algorithm' (use a1, a2 or a3)")
          )
        case Some(id) =>
          rawN.trim.toIntOption match
            case None =>
              BadRequest(ErrorResponse("invalid-request", s"'$rawN' is not a valid integer index"))
            case Some(n) =>
              useCase.run(id, n).flatMap(render)
  }
