package borja.eurofiboclock.infrastructure.adapter.in.rest

import cats.effect.Concurrent
import cats.syntax.all.*
import org.http4s.HttpRoutes
import org.http4s.circe.CirceEntityEncoder.given
import org.http4s.dsl.Http4sDsl

import borja.eurofiboclock.domain.port.in.GetEuropeanCapitalsUseCase
import borja.eurofiboclock.infrastructure.adapter.in.rest.JsonCodecs.given
import borja.eurofiboclock.infrastructure.adapter.in.rest.dto.{CapitalResponse, CapitalsResponse}

/** Inbound REST adapter for the "capitals of European countries" use case. */
final class CapitalController[F[_]: Concurrent](useCase: GetEuropeanCapitalsUseCase[F]) extends Http4sDsl[F]:

  val routes: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root / "api" / "v1" / "capitals" / "europe" =>
      useCase.listAll.flatMap(cs => Ok(CapitalsResponse.from(cs)))

    case GET -> Root / "api" / "v1" / "capitals" / "europe" / country =>
      useCase.findByCountry(country).flatMap(c => Ok(CapitalResponse.from(c)))
  }
