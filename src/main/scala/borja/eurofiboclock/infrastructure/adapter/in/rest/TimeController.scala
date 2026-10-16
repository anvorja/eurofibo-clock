package borja.eurofiboclock.infrastructure.adapter.in.rest

import cats.effect.Concurrent
import cats.syntax.all.*
import org.http4s.HttpRoutes
import org.http4s.circe.CirceEntityEncoder.given
import org.http4s.dsl.Http4sDsl

import borja.eurofiboclock.domain.port.in.GetColombiaTimeUseCase
import borja.eurofiboclock.infrastructure.adapter.in.rest.JsonCodecs.given
import borja.eurofiboclock.infrastructure.adapter.in.rest.dto.ColombiaTimeResponse

/** Inbound REST adapter for the "local time in Colombia" use case. */
final class TimeController[F[_]: Concurrent](useCase: GetColombiaTimeUseCase[F]) extends Http4sDsl[F]:

  val routes: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root / "api" / "v1" / "time" / "colombia" =>
      useCase.currentTime.flatMap(t => Ok(ColombiaTimeResponse.from(t)))
  }
