package example

import java.util.UUID
import scalaz.{\/,-\/,\/-,Kleisli}
import scalaz.concurrent.Task
import scalaz.syntax.kleisli._

object Item {
  type Id = UUID
}
case class Item(
  id: Item.Id,
  content: String,
  createdAt: Long = System.currentTimeMillis)

class ToDo {
  type TConfig = ToDoConfig[Task]
  type ToDoK[A] = Kleisli[Task, TConfig, A]

  protected def config: ToDoK[TConfig] =
    Kleisli.ask[Task, TConfig]

  def create(content: String): ToDoK[Item.Id] = {
     for {
      a <- config
      i  = Item(UUID.randomUUID, content)
      b <- a.repository.create(i).liftKleisli
    } yield b
  }

  def list: ToDoK[List[Item]] =
    for {
      a <- config
      b <- a.repository.list.liftKleisli
    } yield b
}
