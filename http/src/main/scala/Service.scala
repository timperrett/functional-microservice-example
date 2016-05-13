package example

import io.circe.{Decoder, Encoder}
import io.circe.generic.auto._
import org.http4s._
import org.http4s.dsl._

object ToDoService {

  import scala.concurrent.ExecutionContext

  implicit def circeJsonDecoder[A](implicit decoder: Decoder[A]) = org.http4s.circe.jsonOf[A]

  implicit def circeJsonEncoder[A](implicit encoder: Encoder[A]) = org.http4s.circe.jsonEncoderOf[A]

  def service(todo: ToDo)(config: todo.TConfig)(implicit executionContext: ExecutionContext = ExecutionContext.global) =
    HttpService {
      case req@GET -> Root =>
        Ok("Hello, World")

      case GET -> Root / "list" =>
        Ok(todo.list(config).run)

      case req@POST -> Root / "create" =>
        req.decode[ToDoForm] { data => Ok(todo.create(data.content)(config).run)
        }
    }
}
