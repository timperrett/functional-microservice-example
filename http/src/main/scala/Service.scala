package example

import org.http4s._
import org.http4s.dsl._
import org.http4s.server._
import scalaz.concurrent.Task

object ToDoService {
  import scala.concurrent.ExecutionContext

  def service(todo: ToDo)(config: todo.TConfig)(implicit executionContext: ExecutionContext = ExecutionContext.global) =
    HttpService {
      case req @ GET -> Root =>
        Ok(Task.now("foo"))

      case GET -> Root / "list" =>
        Ok("testing")

      case GET -> Root / "create" =>
        Ok("testing")

    }
}
