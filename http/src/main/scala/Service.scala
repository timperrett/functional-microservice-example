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
        Ok("Hello, World")

      case GET -> Root / "list" => Ok("not implemented")
        todo.list(config).map(Ok(_))

      case req @ POST -> Root / "create" =>
        req.decode[UrlForm] { data =>
          data.values.get("content").flatMap(_.headOption) match {
            case Some(a) => todo.create(a)(config).map(Ok(_))
            case None    => Task.now(BadRequest("dsfds"))
          }
        }

    }
}
