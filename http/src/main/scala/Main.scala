package example

import org.http4s.server.blaze.BlazeBuilder
import java.io.File
import knobs._

object Main {
  def main(args: Array[String]): Unit = {

    val cfg = knobs.loadImmutable(Required(
      FileResource(new File("/etc/todo/service.cfg")) or
      ClassPathResource("todo.cfg")) :: Nil).run

    val config = ToDoConfig(
      maximumToDos = cfg.require[Int]("todo.maximum-item-count"),
      repository = new InMemoryRepository
    )

    val todo = new ToDo

    BlazeBuilder.bindHttp(8084)
      .mountService(ToDoService.service(todo)(config), "/")
      .run
      .awaitShutdown()
  }
}
