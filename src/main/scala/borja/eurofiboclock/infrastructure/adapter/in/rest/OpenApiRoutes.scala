package borja.eurofiboclock.infrastructure.adapter.in.rest

import cats.effect.Async
import cats.syntax.all.*
import org.http4s.headers.`Content-Type`
import org.http4s.server.staticcontent.resourceServiceBuilder
import org.http4s.{HttpRoutes, MediaType}
import org.http4s.dsl.Http4sDsl

import java.nio.charset.StandardCharsets

/** Inbound adapter that publishes the API contract.
  *
  *   - `GET /openapi.yaml` -> the raw OpenAPI 3.0 document (a classpath resource)
  *   - `GET /docs`         -> Swagger UI, wired to the document above
  *   - `GET /docs/assets/...` -> Swagger UI static assets, served from the
  *     `org.webjars:swagger-ui` jar so the whole thing works offline
  *
  * Documentation is a transport concern, so it lives here in the driving adapter
  * and never touches the domain or the application services.
  */
final class OpenApiRoutes[F[_]: Async] extends Http4sDsl[F]:

  private val specYaml: String =
    val stream = getClass.getResourceAsStream(OpenApiRoutes.SpecResource)
    require(stream != null, s"${OpenApiRoutes.SpecResource} not found on the classpath")
    try new String(stream.readAllBytes(), StandardCharsets.UTF_8)
    finally stream.close()

  private val documentRoutes: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root / "openapi.yaml" =>
      Ok(specYaml, `Content-Type`(OpenApiRoutes.YamlMediaType))

    case GET -> Root / "docs" =>
      Ok(OpenApiRoutes.swaggerUiHtml, `Content-Type`(MediaType.text.html))

    case GET -> Root / "docs" / "" =>
      Ok(OpenApiRoutes.swaggerUiHtml, `Content-Type`(MediaType.text.html))
  }

  private val assetRoutes: HttpRoutes[F] =
    resourceServiceBuilder[F](OpenApiRoutes.WebjarBase)
      .withPathPrefix("/docs/assets")
      .toRoutes

  val routes: HttpRoutes[F] = documentRoutes <+> assetRoutes

object OpenApiRoutes:

  /** Keep in sync with the `org.webjars:swagger-ui` version in build.sbt. */
  private val SwaggerUiVersion = "5.17.14"

  private val SpecResource = "/openapi/openapi.yaml"
  private val WebjarBase   = s"/META-INF/resources/webjars/swagger-ui/$SwaggerUiVersion"

  private val YamlMediaType: MediaType = MediaType.unsafeParse("application/yaml")

  private val swaggerUiHtml: String =
    """<!DOCTYPE html>
      |<html lang="es">
      |<head>
      |  <meta charset="UTF-8">
      |  <title>EuroFibo Clock API - Swagger UI</title>
      |  <link rel="stylesheet" href="/docs/assets/swagger-ui.css">
      |  <style>body { margin: 0; background: #fafafa; }</style>
      |</head>
      |<body>
      |  <div id="swagger-ui"></div>
      |  <script src="/docs/assets/swagger-ui-bundle.js"></script>
      |  <script src="/docs/assets/swagger-ui-standalone-preset.js"></script>
      |  <script>
      |    window.ui = SwaggerUIBundle({
      |      url: '/openapi.yaml',
      |      dom_id: '#swagger-ui',
      |      deepLinking: true,
      |      presets: [SwaggerUIBundle.presets.apis, SwaggerUIStandalonePreset],
      |      layout: 'StandaloneLayout'
      |    });
      |  </script>
      |</body>
      |</html>
      |""".stripMargin
