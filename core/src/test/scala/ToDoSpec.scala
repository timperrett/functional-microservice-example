package example

import org.scalatest.{FlatSpec,Matchers}

class ToDoSpec extends FlatSpec with Matchers {

  val config = Config(
    maximumToDos = 10,
    repository = new InMemoryRepository)

  val todo = new ToDo

  it must "foo" in {
    todo.list(config).run should equal (Nil)
  }
}
