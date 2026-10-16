package borja.eurofiboclock.infrastructure

import cats.effect.IO
import io.circe.Json
import munit.CatsEffectSuite
import org.http4s.circe.CirceEntityDecoder.given
import org.http4s.{Method, Request, Status, Uri}

import borja.eurofiboclock.application.service.{
  ColombiaTimeService,
  EuroFiboService,
  EuropeanCapitalsService,
  FibonacciService
}
import borja.eurofiboclock.infrastructure.adapter.in.rest.HttpApi
import borja.eurofiboclock.infrastructure.adapter.out.capital.StaticEuropeanCapitalRepository
import borja.eurofiboclock.infrastructure.adapter.out.time.SystemClockAdapter

final class HealthCheckSuite extends CatsEffectSuite:

  private val api =
    val clock = SystemClockAdapter[IO]
    val repo  = StaticEuropeanCapitalRepository[IO]
    new HttpApi[IO](
      ColombiaTimeService[IO](clock),
      new FibonacciService[IO],
      EuropeanCapitalsService[IO](repo),
      new EuroFiboService[IO](clock, repo)
    ).httpApp

  test("GET /healthz answers 200 with status and service name") {
    api.run(Request[IO](Method.GET, Uri.unsafeFromString("/healthz"))).flatMap { r =>
      r.as[Json].map { body =>
        assertEquals(r.status, Status.Ok)
        assertEquals(body.hcursor.get[String]("status"), Right("ok"))
        assertEquals(body.hcursor.get[String]("service"), Right("eurofibo-clock"))
      }
    }
  }
