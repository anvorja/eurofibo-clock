package borja.eurofiboclock.infrastructure

import cats.effect.IO
import munit.CatsEffectSuite
import org.http4s.dsl.io.*
import org.http4s.headers.Origin
import org.http4s.{Header, HttpApp, Method, Request, Status, Uri}
import org.typelevel.ci.*

import borja.eurofiboclock.infrastructure.config.CorsConfig

final class CorsConfigSuite extends CatsEffectSuite:

  private val app: HttpApp[IO] = HttpApp.liftF(Ok("ok"))
  private val Allowed          = "https://eurofibo.netlify.app"
  private val guarded          = CorsConfig.parse(Allowed)(app)

  private def get(origin: String, method: Method = Method.GET): Request[IO] =
    Request[IO](method, Uri.unsafeFromString("/api/v1/time/colombia"))
      .putHeaders(Header.Raw(ci"Origin", origin))

  private def allowOrigin(r: org.http4s.Response[IO]): Option[String] =
    r.headers.get(ci"Access-Control-Allow-Origin").map(_.head.value)

  test("parse: trims, lowercases, drops empties and trailing slashes") {
    assertEquals(
      CorsConfig.parse(" https://A.netlify.app/ , ,https://b.netlify.app").allowedOrigins,
      Set("https://a.netlify.app", "https://b.netlify.app")
    )
    assertEquals(CorsConfig.parse("").allowedOrigins, Set.empty[String])
  }

  test("an allowed origin gets its own origin echoed back, never a wildcard") {
    guarded.run(get(Allowed)).map { r =>
      assertEquals(r.status, Status.Ok)
      assertEquals(allowOrigin(r), Some(Allowed))
    }
  }

  test("another origin receives no CORS headers") {
    guarded.run(get("https://evil.example")).map(r => assertEquals(allowOrigin(r), None))
  }

  test("the preflight of an allowed origin succeeds and only advertises GET/HEAD/OPTIONS") {
    val preflight = get(Allowed, Method.OPTIONS).putHeaders(Header.Raw(ci"Access-Control-Request-Method", "GET"))
    guarded.run(preflight).map { r =>
      assertEquals(allowOrigin(r), Some(Allowed))
      assert(!r.headers.get(ci"Access-Control-Allow-Methods").exists(_.exists(_.value.contains("POST"))))
    }
  }

  test("with no configured origin the app is untouched: same-origin only") {
    CorsConfig.parse("")(app).run(get(Allowed)).map(r => assertEquals(allowOrigin(r), None))
  }
