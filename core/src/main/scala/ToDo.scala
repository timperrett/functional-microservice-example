package example

import java.util.UUID
import scalaz.{\/,-\/,\/-,Kleisli}
import scalaz.concurrent.Task
import scalaz.syntax.kleisli._

object Item {
  type Id = UUID
}
case class Item(id: Item.Id, content: String, createdAt: Long)

class ToDo {
  type ToDoK[A] = Kleisli[Task, Config[Task], A]
  type ToDoConfig = Config[Task]

  protected def config: ToDoK[ToDoConfig] =
    Kleisli.ask[Task, ToDoConfig]

  def create(content: String): ToDoK[Item.Id] = {
     for {
      a <- config
      i  = Item(UUID.randomUUID, content, System.currentTimeMillis)
      b <- a.repository.create(i).liftKleisli
    } yield b
  }

  def list: ToDoK[List[Item]] =
    for {
      a <- config
      b <- a.repository.list.liftKleisli
    } yield b
}
