package example

import scalaz.Monad

abstract class Repository[M[_] : Monad] {
  def list: M[List[Item]]
  def create(item: Item): M[Item.Id]
}

import scalaz.concurrent.Task
import java.util.concurrent.atomic.AtomicReference

class InMemoryRepository extends Repository[Task]{
  private val store = new AtomicReference(List.empty[Item])

  def list: Task[List[Item]] =
    Task(store.get)

  def create(item: Item): Task[Item.Id] =
    Task(store.update(_ :+ item)).map(_ => item.id)
}
