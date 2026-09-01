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

  def resource[F[_]: Async: Network](
      httpApp: HttpApp[F],
      bindHost: Host = DefaultHost,
      bindPort: Port = DefaultPort
  ): Resource[F, Server] =
    val logged = Logger.httpApp[F](logHeaders = true, logBody = false)(httpApp)
    EmberServerBuilder
      .default[F]
      .withHost(bindHost)
      .withPort(bindPort)
      .withHttpApp(logged)
      .build
