
val monixVersion = "3.3.0"

fork := true

lazy val commonSettings = Seq(
  scalaVersion := "3.0.0-M3",
  libraryDependencies ++= Seq(
    ("io.monix" %% "monix" % monixVersion).withDottyCompat(scalaVersion.value),
    "org.scalatest" %% "scalatest" % "3.2.3" % Test,
    "org.scalatest" %% "scalatest-funsuite" % "3.2.3" % Test
  )
)

lazy val `monix-task-exercises` = (project in file("monix-task-exercises"))
  .settings(
    name := "monix-task-exercises"
  )
  .settings(commonSettings)

lazy val `monix-task-solutions` = (project in file("monix-task-solutions"))
  .settings(
    name := "monix-task-solutions",
  )
  .settings(commonSettings)

lazy val `monix-task-app` = (project in file("monix-task-app"))
  .settings(
    name := "monix-task-app",
    libraryDependencies ++= Seq(
      "com.softwaremill.sttp.tapir" %% "tapir-akka-http-server" % "0.17.0",
      "com.softwaremill.sttp.tapir" %% "tapir-json-circe" % "0.17.0",
      "com.typesafe.akka" %% "akka-testkit" % "2.6.10" % Test,
      "com.typesafe.akka" %% "akka-http-testkit" % "10.2.1" % Test,
      "de.heikoseeberger" %% "akka-http-circe" % "1.33.0" % Test
    ).map(_.withDottyCompat(scalaVersion.value))
  )
  .settings(commonSettings)

lazy val `monix-task-app-solutions` = (project in file("monix-task-app-solutions"))
  .settings(
    name := "monix-task-app-solutions",
    libraryDependencies ++= Seq(
      "com.softwaremill.sttp.tapir" %% "tapir-akka-http-server" % "0.17.0",
      "com.softwaremill.sttp.tapir" %% "tapir-json-circe" % "0.17.0",
      "com.typesafe.akka" %% "akka-testkit" % "2.6.10" % Test,
      "com.typesafe.akka" %% "akka-http-testkit" % "10.2.1" % Test,
      "de.heikoseeberger" %% "akka-http-circe" % "1.33.0" % Test,
    ).map(_.withDottyCompat(scalaVersion.value))
  )
  .settings(commonSettings)