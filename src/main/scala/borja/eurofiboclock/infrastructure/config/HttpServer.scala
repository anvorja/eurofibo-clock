package borja.eurofiboclock.infrastructure.config

import cats.effect.{Async, Resource}
import com.comcast.ip4s.{Host, Port, host, port}
import fs2.io.net.Network
import org.http4s.HttpApp
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.Server
import org.http4s.server.middleware.Logger

/** Driving-side infrastructure: binds an [[HttpApp]] to a TCP socket with Ember. */
object HttpServer:

  val DefaultHost: Host = host"127.0.0.1"
  val DefaultPort: Port = port"8484"

  /** HOST / PORT from the environment (Render injects PORT; the container sets HOST=0.0.0.0). */
  def hostFromEnv: Host = sys.env.get("HOST").flatMap(Host.fromString).getOrElse(DefaultHost)
  def portFromEnv: Port = sys.env.get("PORT").flatMap(Port.fromString).getOrElse(DefaultPort)

  def resource[F[_]: Async: Network](
      httpApp: HttpApp[F],
      bindHost: Host = DefaultHost,
      bindPort: Port = DefaultPort,
      cors: CorsConfig = CorsConfig(Set.empty)
  ): Resource[F, Server] =
    val logged = Logger.httpApp[F](logHeaders = true, logBody = false)(cors(httpApp))
    EmberServerBuilder
      .default[F]
      .withHost(bindHost)
      .withPort(bindPort)
      .withHttpApp(logged)
      .build
