package example

import org.http4s.server.blaze.BlazeBuilder

object Main extends App {
  BlazeBuilder.bindHttp(8080)
    .mountService(ToDoService.service, "/")
    .run
    .awaitShutdown()
}
