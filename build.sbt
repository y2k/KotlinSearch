lazy val root = (project in file(".")).
  settings(
    name := "kotlin-search",
    description := "Kotlin Search",
    version := "0.1",

    scalaVersion := "0.5.0-RC1"
  )

// https://mvnrepository.com/artifact/com.github.pengrad/java-telegram-bot-api
libraryDependencies += "com.github.pengrad" % "java-telegram-bot-api" % "3.5.0"