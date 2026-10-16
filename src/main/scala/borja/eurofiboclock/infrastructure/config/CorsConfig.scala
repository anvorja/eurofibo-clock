package borja.eurofiboclock.infrastructure.config

import cats.Applicative
import org.http4s.{HttpApp, Method}
import org.http4s.headers.Origin
import org.http4s.server.middleware.CORS

import scala.concurrent.duration.*

/** Driving-side CORS policy, restricted to an explicit allow-list of origins.
  *
  * The list comes from `CORS_ALLOWED_ORIGINS` (comma-separated, e.g. `https://my-site.netlify.app`).
  * Empty or unset means no origin is allowed: the API stays same-origin only. There is no wildcard.
  */
final case class CorsConfig(allowedOrigins: Set[String]):

  /** Wraps `app` with the CORS policy, or returns it untouched when no origin is allowed. */
  def apply[F[_]: Applicative](app: HttpApp[F]): HttpApp[F] =
    if allowedOrigins.isEmpty then app
    else
      CORS.policy
        .withAllowOriginHost(host => allowedOrigins.contains(CorsConfig.normalize(CorsConfig.render(host))))
        .withAllowMethodsIn(Set(Method.GET, Method.HEAD, Method.OPTIONS))
        .withAllowCredentials(false)
        .withMaxAge(1.day)
        .apply(app)

object CorsConfig:

  val EnvVar = "CORS_ALLOWED_ORIGINS"

  /** Origins compare without a trailing slash and case-insensitively (scheme and host are). */
  def normalize(origin: String): String = origin.trim.stripSuffix("/").toLowerCase

  /** `scheme://host[:port]`, the same shape the browser sends in the Origin header. */
  def render(h: Origin.Host): String = s"${h.scheme.value}://${h.host.value}${h.port.fold("")(p => s":$p")}"

  def parse(raw: String): CorsConfig =
    CorsConfig(raw.split(',').iterator.map(normalize).filter(_.nonEmpty).toSet)

  def fromEnv: CorsConfig = parse(sys.env.getOrElse(EnvVar, ""))
