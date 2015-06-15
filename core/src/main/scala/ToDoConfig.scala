package example

case class ToDoConfig[M[_]](
  maximumToDos: Int,
  repository: Repository[M]
)
