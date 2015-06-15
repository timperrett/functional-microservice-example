package example

import scalaz.concurrent.Task

case class Config[M[_]](
  maximumToDos: Int,
  repository: Repository[M]
)
