# ZIO 2 + Scala 3 project

This project uses ZIO 2 + Scala 3 web service with a public APIs served on port 1337. The architecture is 3 layered - Domain, Implementation and API layers. The Domain layer should be used for business logic only, the Implementation should hold the actual implementation (e.g. code that interacts with Postgres or Kafka - not in this project) and the API layer is responsible for http communication with the outside world.

## Notable dependencies

- zio-http
- zio-json
- tapir (Open API + Swagger)

## SBT Plugins

- sbt-dependency-updates (check for new versions of the project dependencies)
- sbt-dotenv (load configuration from a `.env` file automatically when running `sbt run`)
- sbt-native-packager (package the compiled code / add required scripts to run it)
- sbt-scalafmt (code formatter)
- sbt-tpolecat (good scala compiler options defaults)
- sbt-wartremover (linter)

## How to run it

Start the application with `sbt run`. You might need to adjust the configuration of the project using a `.env` file (see the `.env.example`) or editing the `src/main/resource/application.conf`.

## Example usage

Open up http://localhost:1337/docs and exercise the API with the given Swagger UI.

## Thoughts

1. This project does not fit well as a standalone web service, it should probably be a utility library that is used as compile time dependency from other projects. There is no need to do any http round trips for almost linear calculation of a working schedule. There is no database or other effectful computations, so making a web service only for small data conversion is wasteful.

2. The project is based on my own template project for microservices with ZIO and Scala 3 open sourced at https://github.com/lachezar/zio-scala-3-project . Unfortunately due to the small scope of the assignment, the architecture and all the carefully thought out dependencies can't really shine 🥲

3. I've decided to support arbitrary long opening intervals by splitting intervals longer than 24 hours into subintervals. For example opening at 10 AM on Monday and closing at 5 PM on Tuesday will be represented as two intervals - "Monday 10 AM - 12 AM" and "Tuesday 12 AM - 5 PM". In this way the application supports arbitrary long working schedule. Note that opening and closing within 24 hours would not split the work interval into subintervals (e.g. opening at 10 AM on Monday and closing at 1 AM on Tuesday will still be shown as "Monday 10 AM - 1 AM").