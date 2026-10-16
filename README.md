# EuroFibo Clock

Servicio HTTP en Scala 3 con **arquitectura hexagonal (puertos y adaptadores)**.

Casos de uso base: hora local de Colombia, cálculo de Fibonacci y capitales de
países europeos. Sobre ellos se monta **EuroFibo Clock**: un mismo dominio con
**tres estrategias de negocio** (A1, A2, A3, patrón *Strategy*) que combinan
Fibonacci y la hora de Colombia para elegir una capital europea.

- **Runtime HTTP:** http4s + Ember + cats-effect
- **Puerto de escucha:** `http://127.0.0.1:8484`
- **Build:** sbt 2 / Scala 3.9

## Configuración (variables de entorno)

| Variable               | Por defecto | Descripción                                                                                                   |
| ---------------------- | ----------- | ------------------------------------------------------------------------------------------------------------- |
| `HOST`                 | `127.0.0.1` | Interfaz de escucha (la imagen Docker usa `0.0.0.0`)                                                          |
| `PORT`                 | `8484`      | Puerto (Render lo inyecta)                                                                                    |
| `CORS_ALLOWED_ORIGINS` | vacío       | Orígenes permitidos, separados por coma (`https://mi-sitio.netlify.app`). Vacío = solo mismo origen. Sin comodín |

CORS solo permite `GET`, `HEAD` y `OPTIONS`, sin credenciales. En Render, define `CORS_ALLOWED_ORIGINS` con el dominio de Netlify.

## Arquitectura hexagonal

```
                          ┌──────────────────── infrastructure ─────────────────────┐
   HTTP (curl, navegador) │  adapter.in.rest                                         │
        ──────────────▶   │    TimeController · FibonacciController · CapitalController│
                          │        │  (driving adapters)                             │
                          └────────┼────────────────────────────────────────────────┘
                                   ▼  depende de ↓
        ┌──────────────────── domain.port.in  (driving ports / casos de uso) ────────┐
        │  GetColombiaTimeUseCase · ComputeFibonacciUseCase · GetEuropeanCapitalsUC  │
        └───────────────────────────────────┬───────────────────────────────────────┘
                                            ▼  implementado por
        ┌──────────────────── application.service ──────────────────────────────────┐
        │  ColombiaTimeService · FibonacciService · EuropeanCapitalsService         │
        └───────────────────────────────────┬───────────────────────────────────────┘
                                            ▼  depende de ↓
        ┌──────────────────── domain.port.out  (driven ports) ──────────────────────┐
        │  ClockPort · EuropeanCapitalRepository                                    │
        └───────────────────────────────────┬───────────────────────────────────────┘
                                            ▼  implementado por
                          ┌──────── infrastructure.adapter.out (driven adapters) ────┐
                          │  SystemClockAdapter · StaticEuropeanCapitalRepository    │
                          └─────────────────────────────────────────────────────────┘

   Composition root: borja.eurofiboclock.Main  (único punto donde se eligen los adaptadores)
```

Reglas que se respetan:

- El **dominio** (`domain/`) no importa http4s, circe, cats-effect ni `sbt`. Solo
  `java.time` y tipos propios.
- Las **dependencias apuntan siempre hacia adentro**: infraestructura → puertos →
  aplicación → puertos de salida. El núcleo no conoce a sus adaptadores.
- La aplicación se programa contra **traits `F[_]` (tagless final)**; el efecto
  concreto (`cats.effect.IO`) solo se fija en `Main`.
- Los errores de negocio viven en `domain.error.DomainError` y el adaptador REST
  los traduce a HTTP 400 en `HttpApi`.

### Mapa de paquetes

| Paquete                                              | Rol hexagonal                                                                                                                                                         |
| ---------------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `borja.eurofiboclock.domain.model`                   | Value objects del núcleo (`ColombiaTime`, `Fibonacci`, `EuropeanCapital`, `CapitalCatalogue`, `WallClock`, `CapitalSelection`, `TemporalPassport`, `EuropeanJourney`) |
| `borja.eurofiboclock.domain.algorithm`               | *Strategy* EuroFibo: `CapitalSelectionAlgorithm`, `SelectionOutcome`, `AlgorithmId`; `impl/` con A1, A2, A3                                                           |
| `borja.eurofiboclock.domain.error`                   | Errores de negocio                                                                                                                                                    |
| `borja.eurofiboclock.domain.port.in`                 | Puertos de entrada (`…UseCase`, `RunCapitalAlgorithmUseCase`)                                                                                                         |
| `borja.eurofiboclock.domain.port.out`                | Puertos de salida (`ClockPort`, `EuropeanCapitalRepository`)                                                                                                          |
| `borja.eurofiboclock.application.service`            | Implementación de los casos de uso (`EuroFiboService` orquesta la Strategy)                                                                                           |
| `borja.eurofiboclock.infrastructure.adapter.in.rest` | Adaptador de entrada HTTP (controllers, DTOs, codecs, `OpenApiRoutes`)                                                                                                |
| `borja.eurofiboclock.infrastructure.adapter.out.*`   | Adaptadores de salida (reloj del sistema, catálogos estáticos de 5 y 10 capitales)                                                                                    |
| `borja.eurofiboclock.infrastructure.config`          | Arranque del servidor Ember                                                                                                                                           |
| `borja.eurofiboclock.Main`                           | Composition root / wiring                                                                                                                                             |

## Cómo ejecutar

```bash
sbt run
# -> EuroFibo Clock listening on http://127.0.0.1:8484
```

Tests:

```bash
sbt test
```

## Endpoints

| Método y ruta                           | Descripción                                                            |
| --------------------------------------- | ---------------------------------------------------------------------- |
| `GET /`                                 | Índice con la lista de endpoints                                       |
| `GET /healthz`                          | Health check: `{"status":"ok","service":"eurofibo-clock"}`          |
| `GET /api/v1/time/colombia`             | Hora local actual en `America/Bogota`                                  |
| `GET /api/v1/fibonacci/{n}`             | Fibonacci de `n` (0 ≤ n ≤ 10000), `n` como segmento                    |
| `GET /api/v1/fibonacci?n={n}`           | Igual, con `n` como query param                                        |
| `GET /api/v1/capitals/europe`           | Las 5 capitales europeas soportadas                                    |
| `GET /api/v1/capitals/europe/{country}` | Capital de un país (case-insensitive)                                  |
| `GET /api/v1/eurofibo/algorithms`       | Metadatos de las estrategias A1, A2, A3                                |
| `GET /api/v1/eurofibo/{algorithm}/{n}`  | Ejecuta una estrategia (`algorithm` ∈ `a1\|a2\|a3`) para el índice `n` |
| `GET /docs`                             | Swagger UI (interactivo, "Try it out")                                 |
| `GET /openapi.yaml`                     | Documento OpenAPI 3.0 en crudo                                         |
| `GET /docs/assets/...`                  | Recursos estáticos de Swagger UI                                       |

## EuroFibo Clock — modelo de negocio

Un **único dominio**, tres **estrategias** intercambiables (`CapitalSelectionAlgorithm`).
Fibonacci se calcula siempre por **recursión de cola con acumuladores**
(`domain.model.Fibonacci`), comportamiento iterativo: `fib(0)=0`, `fib(1)=1`,
`fib(5)=5`, `fib(10)=55`.

`EuroFiboService` orquesta: valida `n ∈ [0, 10000]`, lee el instante del `ClockPort`,
lo proyecta a `America/Bogota` (`WallClock` = horas/minutos/segundos) y ejecuta la
estrategia elegida con el catálogo que le corresponde.

### A1 — Fibonacci Colombian Capital Selector  (catálogo de 5)

```
fibFactor    = n + fib(n)
tempFactor   = horas + minutos + segundos
rawIndex     = tempFactor / fibFactor              (división entera; divisor→1 si es 0)
capitalIndex = rawIndex % 5                         (recorrido circular)
```

### A2 — Fibonacci Temporal Passport  (catálogo de 5)

```
fibCode      = n + fib(n)
ETS          = horas * fibCode + minutos + segundos      (European Temporal Score)
capitalIndex = ETS % 5
passportCode = "FIB-<n>-COL-<HHMMSS>-<ETS>-<CAPITAL>"
```

### A3 — Viajero Temporal Europeo  (catálogo **extendido de 10**)

Catálogo: `Paris, Berlin, Madrid, Rome, Lisbon, Vienna, Prague, Warsaw, Budapest, Athens`.

```
startIndex            = fib(n) % 10
temporalDisplacement  = horas * minutos + segundos
effectiveDisplacement = temporalDisplacement % 10
destinationIndex      = (startIndex + effectiveDisplacement) % 10
path                  = recorrido circular de startCapital a destinationCapital
```

### Ejemplos de llamada

```bash
$ curl -s http://127.0.0.1:8484/api/v1/eurofibo/a2/5      # la hora es la real del servidor
{"algorithm":"a2","fibonacciInput":5,"fibonacciValue":"5","fibonacciCode":"10",
 "colombianTime":"02:53:22","temporalScore":"95","capitalIndex":0,
 "destination":{"country":"France","capital":"Paris"},
 "passportCode":"FIB-5-COL-025322-95-PARIS"}

$ curl -s http://127.0.0.1:8484/api/v1/eurofibo/a9/5      # 400 invalid-request
$ curl -s http://127.0.0.1:8484/api/v1/eurofibo/a2/-3     # 400 domain-error
```

## Documentación OpenAPI / Swagger

- **Contrato:** `src/main/resources/openapi/openapi.yaml` (OpenAPI 3.0.3), servido tal
  cual en `GET /openapi.yaml`.
- **UI:** `GET /docs` sirve Swagger UI apuntando a ese documento. Los recursos
  estáticos (`swagger-ui-bundle.js`, `swagger-ui.css`, …) salen del jar
  `org.webjars:swagger-ui`, así que la documentación **funciona sin conexión**.
- El adaptador vive en `OpenApiRoutes`, dentro del adaptador de entrada REST: la
  documentación es una preocupación de transporte y no toca dominio ni servicios.

```bash
sbt run
xdg-open http://127.0.0.1:8484/docs      # navegador
curl -s  http://127.0.0.1:8484/openapi.yaml
```

> Nota: si cambias la versión del webjar en `build.sbt`, actualiza también
> `OpenApiRoutes.SwaggerUiVersion`.

### Ejemplos

```bash
$ curl -s http://127.0.0.1:8484/api/v1/time/colombia
{"zone":"America/Bogota","date":"2026-08-31","time":"16:36:50.302",
 "utcOffset":"-05:00","isoDateTime":"2026-08-31T16:36:50.302-05:00","epochSecond":1788212210}

$ curl -s http://127.0.0.1:8484/api/v1/fibonacci/90
{"n":90,"value":"2880067194370816120","digits":19}

$ curl -s http://127.0.0.1:8484/api/v1/capitals/europe
{"count":5,"capitals":[
  {"country":"France","capital":"Paris"},
  {"country":"Germany","capital":"Berlin"},
  {"country":"Spain","capital":"Madrid"},
  {"country":"Italy","capital":"Rome"},
  {"country":"Portugal","capital":"Lisbon"}]}

$ curl -s http://127.0.0.1:8484/api/v1/fibonacci/-3      # 400
{"error":"domain-error","message":"Fibonacci index must be an integer in [0, 10000], but was -3"}

$ curl -s http://127.0.0.1:8484/api/v1/capitals/europe/narnia   # 400
{"error":"domain-error","message":"'narnia' is not one of the supported European countries: France, Germany, Spain, Italy, Portugal"}
```

## Por qué se puede probar el núcleo sin levantar HTTP

`ColombiaTimeServiceSuite` inyecta un `ClockPort` fijo (instante congelado) y
verifica la proyección a `America/Bogota` sin depender de la hora real ni de un
socket. Lo mismo para Fibonacci y capitales: los tests usan los servicios
directamente contra puertos, sin http4s de por medio.

### Tests del dominio (puros, sin efectos ni sockets)

En `src/test/scala/borja/app/domain/`:

| Suite                                                     | Cubre                                                                                                                                            |
| --------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------ |
| `FibonacciSuite`                                          | `Fibonacci.of`: valores conocidos, recurrencia, no-overflow (`of(100)`), profundidad `of(10000)` sin *stack overflow*, índice negativo rechazado |
| `CapitalCatalogueSuite`                                   | Indexación circular (`at`/`indexOf`/`walk`) con índices negativos y grandes, catálogo de 1 elemento, catálogo vacío rechazado                    |
| `WallClockSuite`                                          | `label`/`compact` con *zero-padding*, `fromInstant` proyectando a `America/Bogota` (incluye cruce de medianoche)                                 |
| `ColombiaTimeSuite`                                       | `ColombiaTime.fromZoned` y `Zone`                                                                                                                |
| `AlgorithmIdSuite`                                        | `fromSlug` (case-insensitive, *trim*, desconocido), *round-trip* de slugs                                                                        |
| `domain/algorithm/FibonacciColombianCapitalSelectorSuite` | A1: pipeline completo, `n=0` sin división por cero, factores extremos, pureza                                                                    |
| `domain/algorithm/FibonacciTemporalPassportSuite`         | A2: `passportCode`, `n=0`, `n=10`, pureza                                                                                                        |
| `domain/algorithm/EuropeanTimeTravellerSuite`             | `path` circular con *wrap-around*, desplazamiento 0, `n=0`, pureza                                                                               |

### Tests de aplicación (puertos con dobles)

`ColombiaTimeServiceSuite`, `FibonacciServiceSuite`, `EuropeanCapitalsServiceSuite`
y `EuroFiboServiceSuite` ejercen los servicios contra `ClockPort` fijo y
repositorio estático, sin http4s de por medio.

**62 tests** en total (`sbt testOnly borja.eurofiboclock.*`).
