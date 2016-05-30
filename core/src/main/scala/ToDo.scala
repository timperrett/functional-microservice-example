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
  createdAt: Long)

case class ToDoForm(content: String)

class ToDo {
  type TConfig = ToDoConfig[Task]
  type ToDoK[A] = Kleisli[Task, TConfig, A]
  // @todo type ToDoKP[A] = Kleisli[Process[Task, ?], TConfig, A]

  protected def config: ToDoK[TConfig] =
    Kleisli.ask[Task, TConfig]

  protected val currentTimeMillis: ToDoK[Long] =
    Task.delay(System.currentTimeMillis).liftKleisli

  protected val randomUUID: ToDoK[UUID] =
    Task.delay(UUID.randomUUID).liftKleisli

  def create(content: String): ToDoK[Item.Id] = {
     for {
      a <- config
      u <- randomUUID
      t <- currentTimeMillis
      i  = Item(u, content, t)
      b <- a.repository.create(i).liftKleisli
    } yield b
  }

  def list: ToDoK[List[Item]] =
    for {
      a <- config
      b <- a.repository.list.liftKleisli
    } yield b

  def selectItem(id: Item.Id): ToDoK[Option[Item]] =
    for {
      a <- config
      b <- a.repository.selectItem(id).liftKleisli
    } yield b

  def initTable(): ToDoK[Int] =
    for {
      a <- config
      b <- a.repository.initTable().liftKleisli
    } yield b
}

