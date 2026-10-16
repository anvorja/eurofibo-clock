scalaVersion := "3.9.0"

val http4sVersion = "0.23.36"
val circeVersion  = "0.14.16"

lazy val root = rootProject
  .settings(
    name := "eurofibo-clock",
    idePackagePrefix := Some("borja.eurofiboclock"),
    Compile / run / mainClass := Some("borja.eurofiboclock.Main"),
    run / fork := true,
    // Fat jar para la imagen Docker: `sbt assembly` -> target/**/eurofibo-clock.jar
    assembly / assemblyJarName := "eurofibo-clock.jar",
    assembly / mainClass := Some("borja.eurofiboclock.Main"),
    assembly / assemblyMergeStrategy := {
      case "module-info.class"                       => MergeStrategy.discard
      case PathList("META-INF", "versions", _*)      => MergeStrategy.discard
      case PathList("META-INF", "services", _*)      => MergeStrategy.concat
      case PathList("META-INF", "MANIFEST.MF")       => MergeStrategy.discard
      case PathList("META-INF", x) if x.toLowerCase.endsWith(".sf") || x.toLowerCase.endsWith(".dsa") || x.toLowerCase.endsWith(".rsa") =>
        MergeStrategy.discard
      case _                                         => MergeStrategy.first
    },
    libraryDependencies ++= Seq(
      "org.http4s"    %% "http4s-ember-server" % http4sVersion,
      "org.http4s"    %% "http4s-dsl"          % http4sVersion,
      "org.http4s"    %% "http4s-circe"        % http4sVersion,
      "io.circe"      %% "circe-core"          % circeVersion,
      "org.webjars"    % "swagger-ui"          % "5.17.14",
      "org.typelevel" %% "log4cats-slf4j"      % "2.7.0",
      "org.slf4j"      % "slf4j-simple"        % "2.0.16",
      "org.scalameta" %% "munit"               % "1.0.4" % Test,
      "org.typelevel" %% "munit-cats-effect"   % "2.0.0" % Test
    )
  )
