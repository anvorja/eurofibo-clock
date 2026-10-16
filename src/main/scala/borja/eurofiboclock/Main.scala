package borja.eurofiboclock

import cats.effect.{IO, IOApp}

import borja.eurofiboclock.application.service.{
  ColombiaTimeService,
  EuroFiboService,
  EuropeanCapitalsService,
  FibonacciService
}
import borja.eurofiboclock.infrastructure.adapter.in.rest.HttpApi
import borja.eurofiboclock.infrastructure.adapter.out.capital.StaticEuropeanCapitalRepository
import borja.eurofiboclock.infrastructure.adapter.out.time.SystemClockAdapter
import borja.eurofiboclock.infrastructure.config.{CorsConfig, HttpServer}

/** Composition root.
  *
  * This is the ONLY place where the concrete adapters are chosen and wired into
  * the application services. Everything else in the codebase depends on ports,
  * not implementations.
  */
object Main extends IOApp.Simple:

  def run: IO[Unit] =
    // --- driven adapters (outbound) ---
    val clock      = SystemClockAdapter[IO]
    val repository = StaticEuropeanCapitalRepository[IO]

    // --- application services (use case implementations) ---
    val timeUseCase      = ColombiaTimeService[IO](clock)
    val fibonacciUseCase = new FibonacciService[IO]
    val capitalsUseCase  = EuropeanCapitalsService[IO](repository)
    val euroFiboUseCase  = new EuroFiboService[IO](clock, repository)

    // --- driving adapter (inbound) ---
    val api = new HttpApi[IO](timeUseCase, fibonacciUseCase, capitalsUseCase, euroFiboUseCase)

    HttpServer
      .resource[IO](api.httpApp, HttpServer.hostFromEnv, HttpServer.portFromEnv, CorsConfig.fromEnv)
      .use { server =>
        IO.println(s"EuroFibo Clock listening on http://${server.address}") *> IO.never
      }
