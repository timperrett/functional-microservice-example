package example

import org.http4s.server.blaze.BlazeBuilder
import java.io.File
import knobs._

object Main {
  def main(args: Array[String]): Unit = {

    val cfg = knobs.loadImmutable(Required(
      FileResource(new File(absConfigFile)) or
      ClassPathResource(configFile)) :: Nil).run

    val config = ToDoConfig(
      maximumToDos = cfg.require[Int]("todo.maximum-item-count"),
      repository = new InMemoryRepository
    )

    val todo = new ToDo

    // Init db
    todo.initTable().run(config).run

    BlazeBuilder.bindHttp(8084)
      .mountService(ToDoService.service(todo)(config), "/")
      .run
      .awaitShutdown()
  }
}
