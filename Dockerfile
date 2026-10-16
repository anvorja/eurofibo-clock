# syntax=docker/dockerfile:1

# ---- build: sbt (version fijada por project/build.properties) -> fat jar
FROM eclipse-temurin:21-jdk AS build
ARG SBT_VERSION=2.0.8
RUN apt-get update && apt-get install -y --no-install-recommends curl unzip ca-certificates \
    && rm -rf /var/lib/apt/lists/* \
    && curl -fsSL "https://github.com/sbt/sbt/releases/download/v${SBT_VERSION}/sbt-${SBT_VERSION}.zip" -o /tmp/sbt.zip \
    && unzip -q /tmp/sbt.zip -d /opt && rm /tmp/sbt.zip
ENV PATH="/opt/sbt/bin:${PATH}"
WORKDIR /app
# Dependencias primero: se cachean mientras build.sbt y los plugins no cambien
COPY project/build.properties project/plugins.sbt ./project/
COPY build.sbt ./
RUN sbt -batch update
COPY src ./src
RUN sbt -batch assembly \
    && cp "$(find target -name eurofibo-clock.jar | head -n 1)" /app/eurofibo-clock.jar

# ---- runtime: JRE mínimo, usuario sin privilegios. La configuración llega por variables de entorno.
FROM eclipse-temurin:21-jre-alpine AS runtime
RUN addgroup -S app && adduser -S app -G app
WORKDIR /app
COPY --from=build --chown=app:app /app/eurofibo-clock.jar ./eurofibo-clock.jar
USER app

# HOST/PORT los lee Main. Render inyecta PORT; en local el valor por defecto es 8484.
ENV HOST=0.0.0.0 PORT=8484
EXPOSE 8484
HEALTHCHECK --interval=30s --timeout=5s --start-period=20s --retries=3 \
  CMD wget -qO- "http://127.0.0.1:${PORT}/api/v1/time/colombia" >/dev/null || exit 1

ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "eurofibo-clock.jar"]
