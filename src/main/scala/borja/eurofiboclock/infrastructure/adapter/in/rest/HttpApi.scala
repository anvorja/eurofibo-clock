package borja.eurofiboclock.infrastructure.adapter.in.rest

import cats.data.Kleisli
import cats.effect.Async
import cats.syntax.all.*
import io.circe.Json
import org.http4s.circe.CirceEntityEncoder.given
import org.http4s.dsl.Http4sDsl
import org.http4s.{HttpApp, HttpRoutes, Request, Response}

import borja.eurofiboclock.domain.error.DomainError
import borja.eurofiboclock.domain.port.in.{
  ComputeFibonacciUseCase,
  GetColombiaTimeUseCase,
  GetEuropeanCapitalsUseCase,
  RunCapitalAlgorithmUseCase
}
import borja.eurofiboclock.infrastructure.adapter.in.rest.JsonCodecs.given
import borja.eurofiboclock.infrastructure.adapter.in.rest.dto.ErrorResponse

/** Composition of every inbound REST adapter into a single [[HttpApp]].
  *
  * It also owns the two cross-cutting concerns of the driving side:
  *   - a small index document at `GET /`
  *   - translating exceptions that bubble out of the hexagon into HTTP status
  *     codes ([[DomainError]] -> 400, anything else -> 500).
  */
final class HttpApi[F[_]: Async](
    timeUseCase: GetColombiaTimeUseCase[F],
    fibonacciUseCase: ComputeFibonacciUseCase[F],
    capitalsUseCase: GetEuropeanCapitalsUseCase[F],
    euroFiboUseCase: RunCapitalAlgorithmUseCase[F]
) extends Http4sDsl[F]:

  private val ServiceName = "eurofibo-clock"

  private val indexRoutes: HttpRoutes[F] = HttpRoutes.of[F] {
    // Liveness probe (Render health check): no use case involved, so it never depends on domain logic.
    case GET -> Root / "healthz" =>
      Ok(Json.obj("status" -> Json.fromString("ok"), "service" -> Json.fromString(ServiceName)))
    case GET -> Root =>
      Ok(
        Json.obj(
          "service" -> Json.fromString(ServiceName),
          "endpoints" -> Json.arr(
            Json.fromString("GET /api/v1/time/colombia"),
            Json.fromString("GET /api/v1/fibonacci/{n}"),
            Json.fromString("GET /api/v1/fibonacci?n={n}"),
            Json.fromString("GET /api/v1/capitals/europe"),
            Json.fromString("GET /api/v1/capitals/europe/{country}"),
            Json.fromString("GET /api/v1/eurofibo/algorithms"),
            Json.fromString("GET /api/v1/eurofibo/{algorithm}/{n}"),
            Json.fromString("GET /healthz"),
            Json.fromString("GET /docs"),
            Json.fromString("GET /openapi.yaml")
          )
        )
      )
  }

  private val routes: HttpRoutes[F] =
    indexRoutes <+>
      new TimeController[F](timeUseCase).routes <+>
      new FibonacciController[F](fibonacciUseCase).routes <+>
      new CapitalController[F](capitalsUseCase).routes <+>
      new EuroFiboController[F](euroFiboUseCase).routes <+>
      new OpenApiRoutes[F].routes

  private def onError(t: Throwable): F[Response[F]] =
    t match
      case d: DomainError =>
        BadRequest(ErrorResponse("domain-error", d.describe))
      case other =>
        InternalServerError(
          ErrorResponse("internal-error", Option(other.getMessage).getOrElse(other.toString))
        )

  val httpApp: HttpApp[F] =
    val base = routes.orNotFound
    Kleisli((req: Request[F]) => base.run(req).handleErrorWith(onError))
